package com.dianping.cat.mybatis.data;

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

	public void setId(Integer id) {
		this.id = id == null ? null : id.longValue();
	}

	public void setId(Long id) {
		this.id = id;
	}
}
