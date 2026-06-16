package com.dianping.cat.core.mybatis.task.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class TaskDO {
	private Integer id;

	private String producer;

	private String consumer;

	private Integer failureCount;

	private String reportName;

	private String reportDomain;

	private Date reportPeriod;

	private Integer status;

	private Integer taskType;

	private Date creationDate;

	private Date startDate;

	private Date endDate;

	private Integer count;

	private Integer keyId;

	private Integer startLimit;

	private Integer endLimit;
}
