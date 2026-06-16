package com.dianping.cat.core.mybatis.daily.report.content.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class DailyReportContentDO {
	private Long reportId;

	private byte[] content;

	private Date period;

	private Date creationDate;

	private Double contentLength;

	private Long keyReportId;

	private Long startId;

	private Long endId;

	private Double capacity;
}
