package com.dianping.cat.core.mybatis.generated.daily.report.content.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.daily.report.content.dao.data.DailyReportContentDO;

public interface DailyReportContentMapper {
	int deleteByPrimaryKey(@Param("reportId") Integer reportId);

	DailyReportContentDO findByPrimaryKey(@Param("reportId") Integer reportId);

	int insert(DailyReportContentDO record);

	List<DailyReportContentDO> queryAll();

	int updateByPrimaryKey(DailyReportContentDO record);

	List<DailyReportContentDO> findOverloadReport(@Param("record") DailyReportContentDO record);
}
