package com.dianping.cat.core.mybatis.weekly.report.content.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class WeeklyReportContentDO {
	private Integer reportId;

	private byte[] content;

	private Date period;

	private Date creationDate;

	private Double contentLength;

	private Integer keyReportId;

	private Double capacity;

	private Integer startId;
}
