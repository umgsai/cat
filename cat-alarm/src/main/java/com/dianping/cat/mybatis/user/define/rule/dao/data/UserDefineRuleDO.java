package com.dianping.cat.mybatis.user.define.rule.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class UserDefineRuleDO {
	private Long id;

	private String content;

	private Date createTime;

	private Date updateTime;

	private Long maxId;

	private Long keyId;

	public Date getCreationDate() {
		return createTime;
	}

	public void setCreationDate(Date creationDate) {
		createTime = creationDate;
	}

	public void setId(Integer id) {
		this.id = id == null ? null : id.longValue();
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setKeyId(Integer keyId) {
		this.keyId = keyId == null ? null : keyId.longValue();
	}

	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}

	public void setMaxId(Integer maxId) {
		this.maxId = maxId == null ? null : maxId.longValue();
	}

	public void setMaxId(Long maxId) {
		this.maxId = maxId;
	}
}
