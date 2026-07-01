package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class DailyReportContentDO {
	private Long reportId;

	private byte[] content;

	private Date period;

	private Date createTime;

	private Date updateTime;

	private Double contentLength;

	private Long startId;
}
