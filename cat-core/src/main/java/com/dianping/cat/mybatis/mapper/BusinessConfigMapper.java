package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.BusinessConfigDO;

public interface BusinessConfigMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	BusinessConfigDO findByPrimaryKey(@Param("id") Long id);

	int insert(BusinessConfigDO record);

	List<BusinessConfigDO> queryAll();

	int updateByPrimaryKey(BusinessConfigDO record);

	List<BusinessConfigDO> findByNameDomain(@Param("record") BusinessConfigDO record);

	List<BusinessConfigDO> findByName(@Param("record") BusinessConfigDO record);

	int updateBaseConfigByDomain(@Param("record") BusinessConfigDO record);
}
