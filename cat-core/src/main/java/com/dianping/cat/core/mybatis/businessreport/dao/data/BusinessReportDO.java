package com.dianping.cat.core.mybatis.businessreport.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class BusinessReportDO {
	private Integer id;

	private Integer type;

	private String name;

	private String ip;

	private String productLine;

	private Date period;

	private byte[] content;

	private Date creationDate;
}
