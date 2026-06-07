package com.dianping.cat.core.mybatis.generated.weekly.report.content.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.weekly.report.content.dao.data.WeeklyReportContentDO;

public interface WeeklyReportContentMapper {
	int deleteByPrimaryKey(@Param("reportId") Integer reportId);

	WeeklyReportContentDO findByPrimaryKey(@Param("reportId") Integer reportId);

	int insert(WeeklyReportContentDO record);

	List<WeeklyReportContentDO> queryAll();

	int updateByPrimaryKey(WeeklyReportContentDO record);

	List<WeeklyReportContentDO> findOverloadReport(@Param("record") WeeklyReportContentDO record);
}
