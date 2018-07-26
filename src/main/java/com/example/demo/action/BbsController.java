package com.example.demo.action;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.WebUtils;
import com.example.demo.es.annotation.EsEntityType;
import com.example.demo.es.annotation.EsIndexType;
import com.example.demo.es.annotation.EsIndexs;
import com.example.demo.es.annotation.EsOperateType;
import com.example.demo.es.service.EsService;
import com.example.demo.es.vo.IndexObject;
import com.example.demo.model.*;
import com.example.demo.service.BbsService;
import com.example.demo.service.BbsUserService;
import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

    @RequestMapping("/bbs/myMessage.html")
    public ModelAndView myPage(HttpServletRequest request,HttpServletResponse response){
        ModelAndView view = new ModelAndView();
        view.setViewName("/message.html");
        BbsUser user = webUtils.currentUser(request,response);
        List<BbsTopic> list = bbsService.getMyTopics(user.getId());
        view.addObject("list",list);
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

    @RequestMapping("/bbs/topic/add.html")
    public ModelAndView addTopic(ModelAndView view){
        view.setViewName("/post.html");
        return view;
    }

    /**
     * 文章发布改为Ajax方式提交更友好
     * @param topic
     * @param post
     * @param title
     * @param postContent
     * @param request
     * @param response
     * @return
     */
    @ResponseBody
    @PostMapping("/bbs/topic/save")
    @EsIndexs({
            @EsIndexType(entityType = EsEntityType.BbsTopic,operateType = EsOperateType.ADD,key = "tid"),
            @EsIndexType(entityType = EsEntityType.BbsPost,operateType = EsOperateType.ADD,key = "pid")
    })
    public JSONObject saveTopic(BbsTopic topic, BbsPost post,String title,String postContent,HttpServletRequest request,HttpServletResponse response){
        BbsUser user = webUtils.currentUser(request, response);
//      Date lastPostTime = bbsService.getLatestPost(user.getId());
//		long now = System.currentTimeMillis();
//		long temp = lastPostTime.getTime();
//		if(now-temp<1000*10){
//			//10秒之内的提交都不处理
//			throw new RuntimeException("提交太快，处理不了，上次提交是 "+lastPostTime);
//		}
        JSONObject result = new JSONObject();
        result.put("err",1);
        if (user == null){
            result.put("msg", "请先登录后再继续！");
        }else if (title.length() < 5 || postContent.length() < 10){
            result.put("msg", "标题或内容太短！");
        }else {
            topic.setIsNice(0);
            topic.setIsUp(0);
            topic.setPv(1);
            topic.setPostCount(1);
            topic.setReplyCount(0);
            post.setHasReply(0);
            topic.setContent(title);
            post.setContent(postContent);
            bbsService.saveTopic(topic, post, user);

            result.put("err",0);
            result.put("tid",topic.getId());
            result.put("pid",post.getId());
            result.put("msg","/bbs/topic"+topic.getId()+"-1.html");
        }
        return result;
    }

    @ResponseBody
    @PostMapping("/bbs/post/save")
    @EsIndexType(entityType = EsEntityType.BbsPost,operateType = EsOperateType.ADD)
    public JSONObject savePost(BbsPost post,HttpServletRequest request,HttpServletResponse response){
        JSONObject result = new JSONObject();
        result.put("err",1);
        if (post.getContent().length()<5){
            result.put("msg", "内容太短，请重新编辑！");
        }else {
            post.setHasReply(0);
            post.setCreateTime(new Date());
            BbsUser user = webUtils.currentUser(request, response);
            bbsService.savePost(post,user);
            BbsTopic topic = bbsService.getTopic(post.getTopicId());
            int totalPost = topic.getPostCount()+1;
            topic.setPostCount(totalPost);
            bbsService.updateTopic(topic);
            bbsService.notifyParticipant(topic.getId(),user.getId());
            int pageSize = (int)PageQuery.DEFAULT_PAGE_SIZE;
            int page = (totalPost/pageSize)+(totalPost%pageSize==0?0:1);
            result.put("msg","/bbs/topic/"+post.getTopicId()+"-"+page+".html");
            result.put("err",0);
            result.put("id",post.getId());
        }
        return result;
    }

    @ResponseBody
    @PostMapping("/bbs/reply/save")
    @EsIndexType(entityType= EsEntityType.BbsReply ,operateType = EsOperateType.ADD)
    public JSONObject saveReply(BbsReply reply, HttpServletRequest request, HttpServletResponse response){
        JSONObject result = new JSONObject();
        result.put("err", 1);
        BbsUser user = webUtils.currentUser(request, response);
        if(user==null){
            result.put("msg", "未登录用户！");
        }else if(reply.getContent().length()<2){
            result.put("msg", "回复内容太短，请修改!");
        }else{
            reply.setUserId(user.getId());
            reply.setPostId(reply.getPostId());
            reply.setCreateTime(new Date());
            bbsService.saveReply(reply);
            reply.set("bbsUser", user);
            reply.setUser(user);
            result.put("msg", "评论成功！");
            result.put("err", 0);

            BbsTopic topic = bbsService.getTopic(reply.getTopicId());
            bbsService.notifyParticipant(reply.getTopicId(),user.getId());
            result.put("id",reply.getId());
        }
        return result;
    }
}
