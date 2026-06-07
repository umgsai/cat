package com.dianping.cat.core.mybatis.generated.operation.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class OperationDO {
	private Integer id;

	private String user;

	private String module;

	private String operation;

	private Date time;

	private String content;

	private Date creationDate;
}
