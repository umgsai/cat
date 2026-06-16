package com.dianping.cat.core.mybatis.monthly.report.content.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class MonthlyReportContentDO {
	private Integer reportId;

	private byte[] content;

	private Date period;

	private Date creationDate;

	private Double contentLength;

	private Integer keyReportId;

	private Double capacity;

	private Integer startId;
}
