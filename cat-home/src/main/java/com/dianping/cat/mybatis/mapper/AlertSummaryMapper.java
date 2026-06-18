package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.AlertSummaryDO;

public interface AlertSummaryMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	AlertSummaryDO findByPrimaryKey(@Param("id") Long id);

	int insert(AlertSummaryDO record);

	List<AlertSummaryDO> queryAll();

	int updateByPrimaryKey(AlertSummaryDO record);
}
