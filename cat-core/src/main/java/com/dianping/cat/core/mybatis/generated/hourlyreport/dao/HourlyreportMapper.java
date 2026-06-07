package com.dianping.cat.core.mybatis.generated.hourlyreport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.hourlyreport.dao.data.HourlyreportDO;

public interface HourlyreportMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	HourlyreportDO findByPrimaryKey(@Param("id") Integer id);

	int insert(HourlyreportDO record);

	List<HourlyreportDO> queryAll();

	int updateByPrimaryKey(HourlyreportDO record);

	List<HourlyreportDO> findAllByDomainNamePeriod(@Param("record") HourlyreportDO record);

	List<HourlyreportDO> findAllByPeriodName(@Param("record") HourlyreportDO record);
}
