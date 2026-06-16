package com.dianping.cat.core.mybatis.weeklyreport.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class WeeklyreportDO {
	private Long id;

	private String name;

	private String ip;

	private String domain;

	private Date period;

	private Integer type;

	private Date createTime;

	private Long keyId;
}
