package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class TaskDO {
	private Long id;

	private String producer;

	private String consumer;

	private Integer failureCount;

	private String reportName;

	private String reportDomain;

	private Date reportPeriod;

	private Integer status;

	private Integer taskType;

	private Date createTime;

	private Date startTime;

	private Date endTime;

	private Date updateTime;

	private Integer count;

	private Long keyId;

	private Integer startLimit;

	private Integer endLimit;
}
