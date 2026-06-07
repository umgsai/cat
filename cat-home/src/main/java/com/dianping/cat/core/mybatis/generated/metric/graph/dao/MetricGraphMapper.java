package com.dianping.cat.core.mybatis.generated.metric.graph.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.metric.graph.dao.data.MetricGraphDO;

public interface MetricGraphMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	MetricGraphDO findByPrimaryKey(@Param("id") Integer id);

	int insert(MetricGraphDO record);

	List<MetricGraphDO> queryAll();

	int updateByPrimaryKey(MetricGraphDO record);

	List<MetricGraphDO> findByGrapId(@Param("record") MetricGraphDO record);

	List<MetricGraphDO> findLast(@Param("record") MetricGraphDO record);

	int deleteBeforeDate(@Param("record") MetricGraphDO record);
}
