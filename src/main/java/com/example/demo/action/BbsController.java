package com.example.demo.action;

import com.example.demo.common.WebUtils;
import com.example.demo.es.annotation.EsEntityType;
import com.example.demo.es.annotation.EsIndexType;
import com.example.demo.es.annotation.EsOperateType;
import com.example.demo.es.service.EsService;
import com.example.demo.es.vo.IndexObject;
import com.example.demo.model.BbsMessage;
import com.example.demo.model.BbsTopic;
import com.example.demo.model.BbsUser;
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
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;

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

    @RequestMapping("/bbs/my/{p}.html")
    public RedirectView openMyTopic(@PathVariable int p, HttpServletRequest request, HttpServletResponse response){
        BbsUser user = webUtils.currentUser(request, response);
        BbsMessage message = bbsService.makeOneBbsMessage(user.getId(),p,0);
        this.bbsService.updateMyTopic(message.getId(),0);
        return new RedirectView(request.getContextPath()+"/bbs/topic/"+p+"-1.html");
    }

    @RequestMapping("/bbs/topic/hot")
    public RedirectView hotTopic(){
        return new RedirectView( "/bbs/topic/hot/1");
    }

    @RequestMapping("/bbs/topic/hot/{p}")
    public ModelAndView hotTopic(@PathVariable int p){
        ModelAndView view = new ModelAndView();
        view.setViewName("/bbs/index.html");
        PageQuery query = new PageQuery(p, null);
        query = bbsService.getHotTopics(query);
        view.addObject("topicPage", query);
        return view;
    }

    @RequestMapping("/bbs/topic/nice")
    public ModelAndView niceTopic(){
        return new ModelAndView( "forward:/bbs/topic/nice/1");
    }

    @RequestMapping("/bbs/topic/nice/{p}")
    public ModelAndView niceTopic(@PathVariable int p, ModelAndView view){
        view.setViewName("/bbs/index.html");
        PageQuery query = new PageQuery(p, null);
        query = bbsService.getNiceTopics(query);
        view.addObject("topicPage", query);
        return view;
    }

    @RequestMapping("/bbs/topic/{id}-{p}.html")
    @EsIndexType(entityType= EsEntityType.BbsTopic ,operateType = EsOperateType.UPDATE)
    public ModelAndView topic(@PathVariable final int id, @PathVariable int p){
        ModelAndView view = new  ModelAndView();
        view.setViewName("/detail.html");
        PageQuery query = new PageQuery(p, new HashMap(){{put("topicId", id);}});
        query = bbsService.getPosts(query);
        view.addObject("postPage", query);

        BbsTopic topic = bbsService.getTopic(id);
        BbsTopic template = new BbsTopic();
        template.setId(id);
        template.setPv(topic.getPv() + 1);
        sql.updateTemplateById(template);
        view.addObject("topic", topic);
        return view;
    }

    @RequestMapping("/bbs/topic/module/{id}-{p}.html")
    public ModelAndView module(@PathVariable final Integer id, @PathVariable Integer p){
        ModelAndView view = new ModelAndView();
        view.setViewName("/index.html");
        PageQuery query = new PageQuery<>(p, new HashMap(){{put("moduleId", id);}});
        query = bbsService.getTopics(query);
        view.addObject("topicPage", query);
        if(query.getList().size() >0){
            BbsTopic bbsTopic = (BbsTopic) query.getList().get(0);
            view.addObject("pagename",bbsTopic.getTails().get("moduleName"));
        }
        return view;
    }
}
