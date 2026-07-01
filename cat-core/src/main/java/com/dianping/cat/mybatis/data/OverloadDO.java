package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class OverloadDO {
	private Long id;

	private Long reportId;

	private Integer reportType;

	private Double reportSize;

	private Date period;

	private Date createTime;

	private Date updateTime;

	private Long maxId;

	private Long count;

	private Date startTime;

	private Date endTime;

	private Integer type;

	public void setCount(Integer count) {
		this.count = count == null ? null : count.longValue();
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public void setId(Integer id) {
		this.id = id == null ? null : id.longValue();
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setMaxId(Integer maxId) {
		this.maxId = maxId == null ? null : maxId.longValue();
	}

	public void setMaxId(Long maxId) {
		this.maxId = maxId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId == null ? null : reportId.longValue();
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}
}
