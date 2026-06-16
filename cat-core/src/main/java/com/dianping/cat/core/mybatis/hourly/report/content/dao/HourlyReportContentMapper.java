package com.dianping.cat.core.mybatis.hourly.report.content.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.hourly.report.content.dao.data.HourlyReportContentDO;

public interface HourlyReportContentMapper {
	int deleteByPrimaryKey(@Param("reportId") Integer reportId);

	HourlyReportContentDO findByPrimaryKey(@Param("reportId") Integer reportId);

	int insert(HourlyReportContentDO record);

	List<HourlyReportContentDO> queryAll();

	int updateByPrimaryKey(HourlyReportContentDO record);

	List<HourlyReportContentDO> findOverloadReport(@Param("record") HourlyReportContentDO record);
}
