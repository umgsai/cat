package com.dianping.cat.core.mybatis.weeklyreport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.weeklyreport.dao.data.WeeklyreportDO;

public interface WeeklyreportMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	WeeklyreportDO findByPrimaryKey(@Param("id") Long id);

	int insert(WeeklyreportDO record);

	List<WeeklyreportDO> queryAll();

	int updateByPrimaryKey(WeeklyreportDO record);

	List<WeeklyreportDO> findReportByDomainNamePeriod(@Param("record") WeeklyreportDO record);

	int deleteReportByDomainNamePeriod(@Param("record") WeeklyreportDO record);
}
