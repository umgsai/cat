package com.dianping.cat.mybatis.server.alarm.rule.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class ServerAlarmRuleDO {
	private Long id;

	private String category;

	private String endPoint;

	private String measurement;

	private String tags;

	private String content;

	private String type;

	private String creator;

	private Date createTime;

	private Date updateTime;

	private Long keyId;

	public Date getCreationDate() {
		return createTime;
	}

	public Date getUpdatetime() {
		return updateTime;
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

	public void setKeyId(Integer keyId) {
		this.keyId = keyId == null ? null : keyId.longValue();
	}

	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}

	public void setUpdatetime(Date updatetime) {
		updateTime = updatetime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
