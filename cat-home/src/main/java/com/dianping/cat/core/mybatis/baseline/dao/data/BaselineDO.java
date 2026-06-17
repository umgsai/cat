package com.dianping.cat.core.mybatis.baseline.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class BaselineDO {
	private Long id;

	private String reportName;

	private String indexKey;

	private Date reportPeriod;

	private byte[] data;

	private Date createTime;

	private Date updateTime;

	private Long keyId;

	private double[] dataInDoubleArray;
}
