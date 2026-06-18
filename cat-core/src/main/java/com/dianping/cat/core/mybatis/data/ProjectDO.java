package com.dianping.cat.core.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class ProjectDO {
	private Long id;

	private String domain;

	private String cmdbDomain;

	private Integer level;

	private String bu;

	private String cmdbProductline;

	private String owner;

	private String email;

	private String phone;

	private Date createTime;

	private Date updateTime;

	private Long keyId;
}
