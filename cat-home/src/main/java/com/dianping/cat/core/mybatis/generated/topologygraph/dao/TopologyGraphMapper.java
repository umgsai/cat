package com.dianping.cat.core.mybatis.generated.topologygraph.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.topologygraph.dao.data.TopologyGraphDO;

public interface TopologyGraphMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	TopologyGraphDO findByPrimaryKey(@Param("id") Integer id);

	int insert(TopologyGraphDO record);

	List<TopologyGraphDO> queryAll();

	int updateByPrimaryKey(TopologyGraphDO record);

	List<TopologyGraphDO> findByPeriod(@Param("record") TopologyGraphDO record);
}
