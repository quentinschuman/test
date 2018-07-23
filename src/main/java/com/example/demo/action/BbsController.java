package com.example.demo.action;

import com.example.demo.common.WebUtils;
import com.example.demo.es.service.EsService;
import com.example.demo.es.vo.IndexObject;
import com.example.demo.service.BbsService;
import com.example.demo.service.BbsUserService;
import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
    @Autowired
    EsService esService;
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

    @RequestMapping("/bbs/index")
    public ModelAndView index(HttpServletRequest request){
        return new ModelAndView("forward:/bbs/index/1.html");
    }

    @RequestMapping("/bbs/index/{p}.html")
    public ModelAndView index(@PathVariable int p,String keyword){
        ModelAndView view = new ModelAndView();
        if (StringUtils.isBlank(keyword)){
            view.setViewName("/index.html");
            PageQuery query = new PageQuery(p,null);
            query = bbsService.getTopics(query);
            view.addObject("topicPage",query);
            view.addObject("pagename","首页综合");
        }else {
            PageQuery<IndexObject> searcherKeywordPage = this.esService.getQueryPage(keyword,p);
            view.setViewName("/lucene/lucene.html");
            view.addObject("searcherPage", searcherKeywordPage);
            view.addObject("pagename", keyword);
            view.addObject("resultnum", searcherKeywordPage.getTotalRow());
        }
        return view;
    }
}
