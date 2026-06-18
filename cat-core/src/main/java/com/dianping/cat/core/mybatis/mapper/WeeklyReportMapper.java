package com.dianping.cat.core.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.data.WeeklyReportDO;

public interface WeeklyReportMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	WeeklyReportDO findByPrimaryKey(@Param("id") Long id);

	int insert(WeeklyReportDO record);

	List<WeeklyReportDO> queryAll();

	int updateByPrimaryKey(WeeklyReportDO record);

	List<WeeklyReportDO> findReportByDomainNamePeriod(@Param("record") WeeklyReportDO record);

	int deleteReportByDomainNamePeriod(@Param("record") WeeklyReportDO record);
}
