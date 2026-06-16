package com.dianping.cat.core.report.daily.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class DailyReportDO {
	private Long id;

	private String name;

	private String ip;

	private String domain;

	private Date period;

	private Integer type;

	private Date createTime;
}
