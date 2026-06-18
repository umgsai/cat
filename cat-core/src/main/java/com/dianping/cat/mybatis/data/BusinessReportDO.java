package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class BusinessReportDO {
	private Long id;

	private Integer type;

	private String name;

	private String ip;

	private String productLine;

	private Date period;

	private byte[] content;

	private Date createTime;

	private Date updateTime;
}
