package com.dianping.cat.core.mybatis.config.modification.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class ConfigModificationDO {
	private Integer id;

	private String userName;

	private String accountName;

	private String actionName;

	private String argument;

	private Date date;

	private Date creationDate;

	private Integer keyId;
}
