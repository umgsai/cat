package com.dianping.cat.core.mybatis.generated.alteration.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class AlterationDO {
	private Integer id;

	private String type;

	private String title;

	private String domain;

	private String hostname;

	private String ip;

	private Date date;

	private String user;

	private String altGroup;

	private String content;

	private String url;

	private Integer status;

	private Date creationDate;

	private Integer keyId;

	private Date startTime;

	private Date endTime;

	private String[] types;
}
