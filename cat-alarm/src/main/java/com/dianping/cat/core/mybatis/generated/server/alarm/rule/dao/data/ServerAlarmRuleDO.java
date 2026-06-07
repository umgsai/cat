package com.dianping.cat.core.mybatis.generated.server.alarm.rule.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class ServerAlarmRuleDO {
	private Integer id;

	private String category;

	private String endPoint;

	private String measurement;

	private String tags;

	private String content;

	private String type;

	private String creator;

	private Date creationDate;

	private Date updatetime;

	private Integer keyId;
}
