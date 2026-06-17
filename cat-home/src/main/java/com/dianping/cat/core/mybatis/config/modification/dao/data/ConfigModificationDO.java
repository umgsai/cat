package com.dianping.cat.core.mybatis.config.modification.dao.data;

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

	private Long keyId;

	public Date getCreationDate() {
		return createTime;
	}

	public Date getDate() {
		return modifyTime;
	}

	public void setCreationDate(Date creationDate) {
		createTime = creationDate;
	}

	public void setDate(Date date) {
		modifyTime = date;
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
}
