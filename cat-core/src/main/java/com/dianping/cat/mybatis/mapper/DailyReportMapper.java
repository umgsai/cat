package com.dianping.cat.mybatis.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.DailyReportDO;

public interface DailyReportMapper {
	int deleteByDomainNamePeriod(@Param("domain") String domain, @Param("name") String name,
			@Param("period") Date period);

	int deleteById(@Param("id") Long id);

	DailyReportDO findByDomainNamePeriod(@Param("domain") String domain, @Param("name") String name,
			@Param("period") Date period);

	DailyReportDO findById(@Param("id") Long id);

	int insert(DailyReportDO report);

	List<DailyReportDO> queryLatestReportsByDomainName(@Param("domain") String domain, @Param("name") String name,
			@Param("limits") int limits);

	int updateById(DailyReportDO report);
}
