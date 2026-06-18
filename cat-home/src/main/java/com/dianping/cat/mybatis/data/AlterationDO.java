package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class AlterationDO {
	private Long id;

	private String type;

	private String title;

	private String domain;

	private String hostname;

	private String ip;

	private Date changeTime;

	private String user;

	private String altGroup;

	private String content;

	private String url;

	private Integer status;

	private Date createTime;

	private Date updateTime;

	private Long keyId;

	private Date startTime;

	private Date endTime;

	private String[] types;
}
