package com.dianping.cat.core.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class MonthlyReportContentDO {
	private Long reportId;

	private byte[] content;

	private Date period;

	private Date createTime;

	private Date updateTime;

	private Double contentLength;

	private Long keyReportId;

	private Double capacity;

	private Long startId;
}
