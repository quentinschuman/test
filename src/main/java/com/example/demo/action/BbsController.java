package com.example.demo.action;

import com.example.demo.common.WebUtils;
import com.example.demo.service.BbsService;
import com.example.demo.service.BbsUserService;
import org.beetl.sql.core.SQLManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * Created by qianshu on 2018/7/21.
 */
@Controller
public class BbsController {
    @Autowired
    SQLManager sql;
    @Autowired
    BbsUserService bbsUserService;
    @Autowired
    BbsService bbsService;
    @Autowired
    WebUtils webUtils;
//    @Autowired
//    EsService esService;
    @Autowired
    private CacheManager cacheManager;

    static String filePath = null;
    static {
        filePath = System.getProperty("user.dir");
        File file = new File("upload",filePath);
        if (!file.exists())
            file.mkdirs();
    }

    @RequestMapping("/bbs/share")
    public ModelAndView share(HttpServletRequest request){
        return new ModelAndView("forward:/bbs/topic/module/1-1.html");
    }
}
