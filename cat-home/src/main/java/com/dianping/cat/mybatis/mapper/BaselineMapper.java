package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.BaselineDO;

public interface BaselineMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	BaselineDO findByPrimaryKey(@Param("id") Long id);

	int insert(BaselineDO record);

	List<BaselineDO> queryAll();

	int updateByPrimaryKey(BaselineDO record);

	List<BaselineDO> findByReportNameKeyTime(@Param("record") BaselineDO record);
}
