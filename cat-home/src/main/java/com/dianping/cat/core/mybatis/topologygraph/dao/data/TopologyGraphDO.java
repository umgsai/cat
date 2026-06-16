package com.dianping.cat.core.mybatis.topologygraph.dao.data;

import java.util.Date;

import lombok.Data;

@Data
public class TopologyGraphDO {
	private Integer id;

	private String ip;

	private Date period;

	private Integer type;

	private byte[] content;

	private Date creationDate;

	private Integer keyId;
}
