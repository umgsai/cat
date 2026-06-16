package com.dianping.cat.core.mybatis.baseline.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.baseline.dao.data.BaselineDO;

public interface BaselineMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	BaselineDO findByPrimaryKey(@Param("id") Integer id);

	int insert(BaselineDO record);

	List<BaselineDO> queryAll();

	int updateByPrimaryKey(BaselineDO record);

	List<BaselineDO> findByReportNameKeyTime(@Param("record") BaselineDO record);
}
