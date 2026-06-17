package com.dianping.cat.core.mybatis.topologygraph.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class TopologyGraphDO {
	private Long id;

	private String ip;

	private Date period;

	private Integer type;

	private byte[] content;

	private Date createTime;

	private Date updateTime;

	private Long keyId;
}
