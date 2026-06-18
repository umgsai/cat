package com.dianping.cat.core.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.data.WeeklyReportContentDO;

public interface WeeklyReportContentMapper {
	int deleteByPrimaryKey(@Param("reportId") Long reportId);

	WeeklyReportContentDO findByPrimaryKey(@Param("reportId") Long reportId);

	int insert(WeeklyReportContentDO record);

	List<WeeklyReportContentDO> queryAll();

	int updateByPrimaryKey(WeeklyReportContentDO record);

	List<WeeklyReportContentDO> findOverloadReport(@Param("record") WeeklyReportContentDO record);
}
