package com.dianping.cat.core.mybatis.generated.weeklyreport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.weeklyreport.dao.data.WeeklyreportDO;

public interface WeeklyreportMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	WeeklyreportDO findByPrimaryKey(@Param("id") Integer id);

	int insert(WeeklyreportDO record);

	List<WeeklyreportDO> queryAll();

	int updateByPrimaryKey(WeeklyreportDO record);

	List<WeeklyreportDO> findReportByDomainNamePeriod(@Param("record") WeeklyreportDO record);

	int deleteReportByDomainNamePeriod(@Param("record") WeeklyreportDO record);
}
