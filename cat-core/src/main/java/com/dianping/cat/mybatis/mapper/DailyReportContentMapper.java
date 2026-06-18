package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.DailyReportContentDO;

public interface DailyReportContentMapper {
	int deleteByPrimaryKey(@Param("reportId") Long reportId);

	DailyReportContentDO findByPrimaryKey(@Param("reportId") Long reportId);

	int insert(DailyReportContentDO record);

	List<DailyReportContentDO> queryAll();

	int updateByPrimaryKey(DailyReportContentDO record);

	List<DailyReportContentDO> findOverloadReport(@Param("record") DailyReportContentDO record);
}
