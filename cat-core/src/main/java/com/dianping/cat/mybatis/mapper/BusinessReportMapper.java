package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.BusinessReportDO;

public interface BusinessReportMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	BusinessReportDO findByPrimaryKey(@Param("id") Long id);

	int insert(BusinessReportDO record);

	List<BusinessReportDO> queryAll();

	int updateByPrimaryKey(BusinessReportDO record);
}
