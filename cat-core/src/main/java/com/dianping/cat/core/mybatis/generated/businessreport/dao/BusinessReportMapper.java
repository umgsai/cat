package com.dianping.cat.core.mybatis.generated.businessreport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.businessreport.dao.data.BusinessReportDO;

public interface BusinessReportMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	BusinessReportDO findByPrimaryKey(@Param("id") Integer id);

	int insert(BusinessReportDO record);

	List<BusinessReportDO> queryAll();

	int updateByPrimaryKey(BusinessReportDO record);
}
