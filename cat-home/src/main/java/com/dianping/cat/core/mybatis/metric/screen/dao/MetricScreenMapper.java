package com.dianping.cat.core.mybatis.metric.screen.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.metric.screen.dao.data.MetricScreenDO;

public interface MetricScreenMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	MetricScreenDO findByPrimaryKey(@Param("id") Long id);

	int insert(MetricScreenDO record);

	List<MetricScreenDO> queryAll();

	int updateByPrimaryKey(MetricScreenDO record);

	List<MetricScreenDO> findAll(@Param("record") MetricScreenDO record);

	List<MetricScreenDO> findByNameGraph(@Param("record") MetricScreenDO record);

	List<MetricScreenDO> findByName(@Param("record") MetricScreenDO record);

	int insertOrUpdateByNameGraph(@Param("record") MetricScreenDO record);

	int deleteByName(@Param("record") MetricScreenDO record);

	int deleteByNameGraph(@Param("record") MetricScreenDO record);
}
