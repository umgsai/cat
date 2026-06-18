package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.HourlyReportContentDO;

public interface HourlyReportContentMapper {
	int deleteByPrimaryKey(@Param("reportId") Long reportId);

	HourlyReportContentDO findByPrimaryKey(@Param("reportId") Long reportId);

	int insert(HourlyReportContentDO record);

	List<HourlyReportContentDO> queryAll();

	int updateByPrimaryKey(HourlyReportContentDO record);

	List<HourlyReportContentDO> findOverloadReport(@Param("record") HourlyReportContentDO record);
}
