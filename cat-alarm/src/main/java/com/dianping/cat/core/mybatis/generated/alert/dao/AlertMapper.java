package com.dianping.cat.core.mybatis.generated.alert.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.alert.dao.data.AlertDO;

public interface AlertMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	AlertDO findByPrimaryKey(@Param("id") Integer id);

	int insert(AlertDO record);

	List<AlertDO> queryAll();

	int updateByPrimaryKey(AlertDO record);

	List<AlertDO> queryAlertsByTimeDomain(@Param("record") AlertDO record);

	List<AlertDO> queryAlertsByTimeDomainCategories(@Param("record") AlertDO record);

	List<AlertDO> queryAlertsByTimeCategoryDomain(@Param("record") AlertDO record);

	List<AlertDO> queryAlertsByTimeCategory(@Param("record") AlertDO record);
}
