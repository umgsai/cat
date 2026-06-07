package com.dianping.cat.core.mybatis.generated.business.config.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.business.config.dao.data.BusinessConfigDO;

public interface BusinessConfigMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	BusinessConfigDO findByPrimaryKey(@Param("id") Integer id);

	int insert(BusinessConfigDO record);

	List<BusinessConfigDO> queryAll();

	int updateByPrimaryKey(BusinessConfigDO record);

	List<BusinessConfigDO> findByNameDomain(@Param("record") BusinessConfigDO record);

	List<BusinessConfigDO> findByName(@Param("record") BusinessConfigDO record);

	int updateBaseConfigByDomain(@Param("record") BusinessConfigDO record);
}
