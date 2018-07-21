package com.example.demo.es.vo;

import com.example.demo.es.annotation.EsEntityType;
import com.example.demo.es.annotation.EsOperateType;

/**
 * EsIndexType注解的数据
 * @author yangkebiao
 *
 */
public class EsIndexTypeData {

	private EsEntityType entityType;					//实体类型
	private EsOperateType operateType;			//操作类型
	private Object id;									//获取主键
	
	public EsEntityType getEntityType() {
		return entityType;
	}
	public void setEntityType(EsEntityType entityType) {
		this.entityType = entityType;
	}
	public EsOperateType getOperateType() {
		return operateType;
	}
	public void setOperateType(EsOperateType operateType) {
		this.operateType = operateType;
	}
	public Object getId() {
		return id;
	}
	public void setId(Object id) {
		this.id = id;
	}
	public EsIndexTypeData(EsEntityType entityType, EsOperateType operateType, Object id) {
		super();
		this.entityType = entityType;
		this.operateType = operateType;
		this.id = id;
	}
	public EsIndexTypeData() {
		super();
	}
	
	
	
	
	
}
