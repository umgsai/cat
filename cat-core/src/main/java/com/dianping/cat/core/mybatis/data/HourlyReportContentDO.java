package com.dianping.cat.core.mybatis.hourly.report.content.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class HourlyReportContentDO {
	private Long reportId;

	private byte[] content;

	private Date period;

	private Date createTime;

	private Date updateTime;

	private Double contentLength;

	private Long keyReportId;

	private Long startId;

	private Double capacity;
}
