package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class ConfigModificationDO {
	private Long id;

	private String userName;

	private String accountName;

	private String actionName;

	private String argument;

	private Date modifyTime;

	private Date createTime;

	private Date updateTime;

	public void setId(Integer id) {
		this.id = id == null ? null : id.longValue();
	}

	public void setId(Long id) {
		this.id = id;
	}
}
