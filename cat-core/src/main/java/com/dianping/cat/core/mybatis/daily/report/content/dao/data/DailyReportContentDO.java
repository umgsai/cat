package com.dianping.cat.core.mybatis.daily.report.content.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class DailyReportContentDO {
	private Integer reportId;

	private byte[] content;

	private Date period;

	private Date creationDate;

	private Double contentLength;

	private Integer keyReportId;

	private Integer startId;

	private Integer endId;

	private Double capacity;
}
