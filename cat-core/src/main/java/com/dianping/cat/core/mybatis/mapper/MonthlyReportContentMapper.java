package com.dianping.cat.core.mybatis.monthly.report.content.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.monthly.report.content.dao.data.MonthlyReportContentDO;

public interface MonthlyReportContentMapper {
	int deleteByPrimaryKey(@Param("reportId") Long reportId);

	MonthlyReportContentDO findByPrimaryKey(@Param("reportId") Long reportId);

	int insert(MonthlyReportContentDO record);

	List<MonthlyReportContentDO> queryAll();

	int updateByPrimaryKey(MonthlyReportContentDO record);

	List<MonthlyReportContentDO> findOverloadReport(@Param("record") MonthlyReportContentDO record);
}
