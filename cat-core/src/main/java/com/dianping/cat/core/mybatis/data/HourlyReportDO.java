package com.dianping.cat.core.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class HourlyReportDO {
	private Long id;

	private Integer type;

	private String name;

	private String ip;

	private String domain;

	private Date period;

	private Date createTime;

	private Long keyId;
}
