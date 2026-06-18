package com.dianping.cat.mybatis.alert.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class AlertDO {
	private Long id;

	private String domain;

	private Date alertTime;

	private String category;

	private String type;

	private String content;

	private String metric;

	private Date createTime;

	private Date updateTime;

	private Long keyId;

	private Date startTime;

	private Date endTime;

	private String[] categories;
}
