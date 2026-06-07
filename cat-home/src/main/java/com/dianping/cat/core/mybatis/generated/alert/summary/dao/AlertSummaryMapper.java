package com.dianping.cat.core.mybatis.generated.alert.summary.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.alert.summary.dao.data.AlertSummaryDO;

public interface AlertSummaryMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	AlertSummaryDO findByPrimaryKey(@Param("id") Integer id);

	int insert(AlertSummaryDO record);

	List<AlertSummaryDO> queryAll();

	int updateByPrimaryKey(AlertSummaryDO record);
}
