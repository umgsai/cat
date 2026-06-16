package com.dianping.cat.core.mybatis.business.config.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class BusinessConfigDO {
	private Integer id;

	private String name;

	private String domain;

	private String content;

	private Date updatetime;

	private Integer keyId;
}
