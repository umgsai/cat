package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class ConfigDO {
	private Long id;

	private String name;

	private String content;

	private Date createTime;

	private Date updateTime;

	public void setId(Integer id) {
		this.id = id == null ? null : id.longValue();
	}

	public void setId(Long id) {
		this.id = id;
	}
}
