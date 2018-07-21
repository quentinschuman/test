package com.example.demo.dao;

import com.example.demo.model.BbsUser;
import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.mapper.BaseMapper;

import java.util.List;


public interface BbsUserDao extends BaseMapper<BbsUser> {
	
		
		@SqlStatement(params="max")
		List<BbsUser> getScoreTop(Integer max);
		
		@SqlStatement(params="max")
		List<BbsUser> getLevelTop(Integer max);
}
