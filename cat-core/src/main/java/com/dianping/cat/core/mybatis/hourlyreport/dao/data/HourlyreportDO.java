package com.dianping.cat.core.mybatis.hourlyreport.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class HourlyreportDO {
	private Long id;

	private Integer type;

	private String name;

	private String ip;

	private String domain;

	private Date period;

	private Date createTime;

	private Long keyId;
}
