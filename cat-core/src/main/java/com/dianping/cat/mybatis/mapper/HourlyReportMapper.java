package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.HourlyReportDO;

public interface HourlyReportMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	HourlyReportDO findByPrimaryKey(@Param("id") Long id);

	int insert(HourlyReportDO record);

	List<HourlyReportDO> queryAll();

	int updateByPrimaryKey(HourlyReportDO record);

	List<HourlyReportDO> findAllByDomainNamePeriod(@Param("record") HourlyReportDO record);

	List<HourlyReportDO> findAllByPeriodName(@Param("record") HourlyReportDO record);
}
