package com.dianping.cat.mybatis.data;

import java.util.Date;

import lombok.Data;

@Data
public class BusinessConfigDO {
	private Long id;

	private String name;

	private String domain;

	private String content;

	private Date createTime;

	private Date updateTime;

	private Long keyId;

	public Date getUpdatetime() {
		return updateTime;
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

	public void setUpdatetime(Date updatetime) {
		updateTime = updatetime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
