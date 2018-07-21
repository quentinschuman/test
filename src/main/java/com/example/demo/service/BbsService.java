package com.example.demo.service;

import com.example.demo.model.*;
import org.beetl.sql.core.engine.PageQuery;

import java.util.Date;
import java.util.List;

/**
 * Created by qianshu on 2018/7/21.
 */
public interface BbsService {
    BbsTopic getTopic(Integer topicId);
    BbsPost getPost(int postId);
    BbsReply getReply(int replay);

    PageQuery getTopics(PageQuery query);
    List<BbsTopic> getMyTopics(int userId);
    Integer getMyTopicsCount(int userId);

    public void updateMyTopic(int msgId,int status);
    public BbsMessage makeOneBbsMessage(int userId,int topicId,int statu);
    public void notifyParticipant(int topicId,int ownerId);
    PageQuery getHotTopics(PageQuery query);
    PageQuery getNiceTopics(PageQuery query);
    PageQuery getPosts(PageQuery query);
    void saveUser(BbsUser user);
    BbsUser login(BbsUser user);
    void saveTopic(BbsTopic topic, BbsPost post, BbsUser user);
    void savePost(BbsPost post, BbsUser user);
    void saveReply(BbsReply reply);
    void deleteTopic(int id);
    void deletePost(int id);
    void deleteReplay(int id);
    void updateTopic(BbsTopic topic);
    void updatePost(BbsPost post);
    Date getLatestPost(int userId);
    BbsPost getFirstPost(Integer topicId);
}
