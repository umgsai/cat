package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class UserDefineRuleDO {
	private Long id;

	private String content;

	private Date createTime;

	private Date updateTime;

	private Long maxId;

	public void setId(Integer id) {
		this.id = id == null ? null : id.longValue();
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setMaxId(Integer maxId) {
		this.maxId = maxId == null ? null : maxId.longValue();
	}

	public void setMaxId(Long maxId) {
		this.maxId = maxId;
	}
}
