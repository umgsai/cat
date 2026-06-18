package com.dianping.cat.core.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class HostInfoDO {
	private Long id;

	private String ip;

	private String domain;

	private String hostname;

	private Date createTime;

	private Date updateTime;

	private Long keyId;
}
