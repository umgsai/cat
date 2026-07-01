package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class WeeklyReportDO {
	private Long id;

	private String name;

	private String ip;

	private String domain;

	private Date period;

	private Integer type;

	private Date createTime;
}
