package com.dianping.cat.core.config.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class ConfigDO {
	private Long id;

	private String name;

	private String content;

	private Date createTime;

	private Date updateTime;
}
