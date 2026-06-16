package com.dianping.cat.core.mybatis.baseline.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class BaselineDO {
	private Integer id;

	private String reportName;

	private String indexKey;

	private Date reportPeriod;

	private byte[] data;

	private Date creationDate;

	private Integer keyId;

	private double[] dataInDoubleArray;
}
