package com.example.demo.dao;

import com.example.demo.model.BbsTopic;
import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.mapper.BaseMapper;

import java.util.List;

public interface BbsTopicDao extends BaseMapper<BbsTopic> {
	void queryTopic(PageQuery query);
	@SqlStatement(params="userId")
	List<BbsTopic> queryMyMessageTopic(int userId);
	
	@SqlStatement(params="userId")
	Integer queryMyMessageTopicCount(int userId);
	
	@SqlStatement(params="topicId",returnType=Integer.class)
	List<Integer> getParticipantUserId(Integer topicId);
	
	@SqlStatement(params="topicId")
	BbsTopic getTopic(Integer topicId);
	
}
