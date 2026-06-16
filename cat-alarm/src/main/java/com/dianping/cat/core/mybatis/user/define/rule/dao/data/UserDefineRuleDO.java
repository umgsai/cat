package com.dianping.cat.core.mybatis.user.define.rule.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class UserDefineRuleDO {
	private Integer id;

	private String content;

	private Date creationDate;

	private Integer maxId;

	private Integer keyId;
}
