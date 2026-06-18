package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.TopologyGraphDO;

public interface TopologyGraphMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	TopologyGraphDO findByPrimaryKey(@Param("id") Long id);

	int insert(TopologyGraphDO record);

	List<TopologyGraphDO> queryAll();

	int updateByPrimaryKey(TopologyGraphDO record);

	List<TopologyGraphDO> findByPeriod(@Param("record") TopologyGraphDO record);
}
