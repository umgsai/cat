package com.dianping.cat.core.mybatis.generated.hourlyreport.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class HourlyreportDO {
	private Integer id;

	private Integer type;

	private String name;

	private String ip;

	private String domain;

	private Date period;

	private Date creationDate;

	private Integer keyId;
}
