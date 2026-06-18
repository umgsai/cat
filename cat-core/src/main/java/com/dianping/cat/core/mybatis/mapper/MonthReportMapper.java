package com.dianping.cat.core.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.data.MonthReportDO;

public interface MonthReportMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	MonthReportDO findByPrimaryKey(@Param("id") Long id);

	int insert(MonthReportDO record);

	List<MonthReportDO> queryAll();

	int updateByPrimaryKey(MonthReportDO record);

	List<MonthReportDO> findReportByDomainNamePeriod(@Param("record") MonthReportDO record);

	int deleteReportByDomainNamePeriod(@Param("record") MonthReportDO record);
}
