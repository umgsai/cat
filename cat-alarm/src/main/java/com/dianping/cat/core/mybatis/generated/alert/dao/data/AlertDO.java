package com.dianping.cat.core.mybatis.generated.alert.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class AlertDO {
	private Integer id;

	private String domain;

	private Date alertTime;

	private String category;

	private String type;

	private String content;

	private String metric;

	private Date creationDate;

	private Integer keyId;

	private Date startTime;

	private Date endTime;

	private String[] categories;
}
