package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class AlertSummaryDO {
	private Long id;

	private String domain;

	private Date alertTime;

	private String content;

	private Date createTime;

	private Date updateTime;

	private Long keyId;
}
