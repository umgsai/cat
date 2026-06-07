package com.dianping.cat.core.report.daily.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.report.daily.dao.data.DailyReportDO;

public interface DailyReportMapper {
	int deleteByDomainNamePeriod(@Param("domain") String domain, @Param("name") String name,
			@Param("period") Date period);

	int deleteById(@Param("id") Integer id);

	DailyReportDO findByDomainNamePeriod(@Param("domain") String domain, @Param("name") String name,
			@Param("period") Date period);

	DailyReportDO findById(@Param("id") Integer id);

	int insert(DailyReportDO report);

	List<DailyReportDO> queryLatestReportsByDomainName(@Param("domain") String domain, @Param("name") String name,
			@Param("limits") int limits);

	int updateById(DailyReportDO report);
}
