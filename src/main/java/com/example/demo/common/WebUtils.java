package com.example.demo.common;

import com.example.demo.dao.BbsUserDao;
import com.example.demo.model.BbsUser;
import com.example.demo.util.HashKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by qianshu on 2018/7/20.
 */
@Service
public class WebUtils {
    
    @Autowired
    BbsUserDao userDao;

    /**
     * 密码 md5hex
     * @param password
     * @return
     */
    public static String pwdEncode(String password){
        return HashKit.md5(password);
    }

    /**
     * 返回当前用户
     * @param request
     * @param response
     * @return
     */
    public BbsUser currentUser(HttpServletRequest request, HttpServletResponse response){
        BbsUser user = (BbsUser) request.getSession().getAttribute("user");
        if (user != null)
            return user;
        String cookieKey = Const.USER_COOKIE_KEY;
        //获取cookie信息
        String userCookie = getCookie(request, cookieKey);
        // 1.cookie为空，直接清除
        if (StringUtils.isEmpty(userCookie)) {
            removeCookie(response, cookieKey);
            return null;
        }
        //2.解密cookie
        String cookieInfo = null;
        //cookie 私钥
        String secret = Const.USER_COOKIE_SECRET;
        try {
            cookieInfo = new AESUtils(secret).decryptString(userCookie);
        }catch (RuntimeException e){
            //ignore
        }
        //3.异常或解密问题，直接清楚cookie信息
        if (StringUtils.isEmpty(cookieInfo)){
            removeCookie(response,cookieKey);
            return null;
        }
        String[] userInfo = cookieInfo.split("~");
        //4.规则不匹配
        if (userInfo.length < 4){
            removeCookie(response,cookieKey);
            return null;
        }
        String userId   = userInfo[0];
        String oldTime  = userInfo[1];
        String maxAge   = userInfo[2];
        String password    = userInfo[3];
        // 5.判定时间区间，超时的cookie清理掉
        if (!"-1".equals(maxAge)){
            long now = System.currentTimeMillis();
            long time = Long.parseLong(oldTime) + (Long.parseLong(maxAge) * 1000);
            if (time <= now){
                removeCookie(response,cookieKey);
                return null;
            }
        }
        if (userId == null || "null".equals(userId)){
            removeCookie(response,cookieKey);
            return null;
        }
        if(password == null || "".equals(password.trim())){
            removeCookie(response, cookieKey);
            return null;
        }
        if(!HashKit.md5(user.getPassword()).equals(password)) {
            removeCookie(response, cookieKey);
            return null;
        }

        request.getSession().setAttribute("user", user);
        return user;
    }

    /**
     * 用户登陆状态维持
     * cookie设计为: des(私钥).encode(userId~time~maxAge~password~ip)
     * @param request
     * @param response
     * @param user
     * @param remember
     */
    public static void loginUser(HttpServletRequest request, HttpServletResponse response, BbsUser user, boolean... remember) {

        request.setAttribute("user", user);
        // 获取用户的id、nickName
        String uid     = user.getId()+"";
        String password     = user.getPassword();
        // 当前毫秒数
        long   now      = System.currentTimeMillis();
        // 超时时间
        int    maxAge   = -1;
        if (remember.length > 0 && remember[0]) {
            maxAge      = 60 * 60 * 24 * 30; // 30天
        }
        // 用户id地址
        String ip		= getIP(request);
        // 构造cookie
        StringBuilder cookieBuilder = new StringBuilder()
                .append(uid).append("~")
                .append(now).append("~")
                .append(maxAge).append("~")
                .append(HashKit.md5(password)).append("~")
                .append(ip);

        // cookie 私钥
        String secret = Const.USER_COOKIE_SECRET;
        // 加密cookie
        String userCookie = new AESUtils(secret).encryptString(cookieBuilder.toString());
        String cookieKey = Const.USER_COOKIE_KEY;
        // 设置用户的cookie、 -1 维持成session的状态
        setCookie(response, cookieKey, userCookie, maxAge);
    }

    /**
     * 退出即删除用户信息
     * @param request
     * @param response
     */
    public static void logoutUser(HttpServletRequest request,HttpServletResponse response) {
        request.getSession().removeAttribute("user");
        removeCookie(response, Const.USER_COOKIE_KEY);
    }

    /**
     * 读取cookie
     * @param request
     * @param key
     * @return
     */
    public static String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if(null != cookies){
            for (Cookie cookie : cookies) {
                if (key.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private static void removeCookie(HttpServletResponse response, String key) {
        setCookie(response,key,null,0);
    }

    /**
     * 设置cookie
     * @param response
     * @param name
     * @param value
     * @param maxAgeInseconds
     */
    private static void setCookie(HttpServletResponse response, String name, String value, int maxAgeInseconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeInseconds);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 获取浏览器信息
     * @param request
     * @return
     */
    public static String getUserAgent(HttpServletRequest request){
        return request.getHeader("User-Agent");
    }

    /**
     * 获取ip
     * @param request
     * @return
     */
    public static String getIP(HttpServletRequest request){
        String ip = request.getHeader("X-Requested-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public boolean isAdmin(HttpServletRequest request,HttpServletResponse response){
        BbsUser user = this.currentUser(request,response);
        if (user == null)
            throw new RuntimeException("未登录用户");
        return user.getUserName().equals("admin");
    }
}
