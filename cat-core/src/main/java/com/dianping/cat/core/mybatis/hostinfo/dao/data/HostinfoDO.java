package com.dianping.cat.core.mybatis.hostinfo.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class HostinfoDO {
	private Integer id;

	private String ip;

	private String domain;

	private String hostname;

	private Date creationDate;

	private Date lastModifiedDate;

	private Integer keyId;
}
