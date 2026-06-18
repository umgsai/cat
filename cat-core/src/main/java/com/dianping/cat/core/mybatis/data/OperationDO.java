package com.dianping.cat.core.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class OperationDO {
	private Long id;

	private String user;

	private String module;

	private String operation;

	private Date operationTime;

	private String content;

	private Date createTime;

	private Date updateTime;

	private Long keyId;

	public Date getCreationDate() {
		return createTime;
	}

	public Date getTime() {
		return operationTime;
	}

	public void setCreationDate(Date creationDate) {
		createTime = creationDate;
	}

	public void setId(Integer id) {
		this.id = id == null ? null : id.longValue();
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTime(Date time) {
		operationTime = time;
	}
}
