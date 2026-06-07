package com.dianping.cat.core.mybatis.generated.alert.summary.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class AlertSummaryDO {
	private Integer id;

	private String domain;

	private Date alertTime;

	private String content;

	private Date creationDate;

	private Integer keyId;
}
