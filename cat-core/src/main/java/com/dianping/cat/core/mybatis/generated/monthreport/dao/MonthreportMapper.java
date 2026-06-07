package com.dianping.cat.core.mybatis.generated.monthreport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.monthreport.dao.data.MonthreportDO;

public interface MonthreportMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	MonthreportDO findByPrimaryKey(@Param("id") Integer id);

	int insert(MonthreportDO record);

	List<MonthreportDO> queryAll();

	int updateByPrimaryKey(MonthreportDO record);

	List<MonthreportDO> findReportByDomainNamePeriod(@Param("record") MonthreportDO record);

	int deleteReportByDomainNamePeriod(@Param("record") MonthreportDO record);
}
