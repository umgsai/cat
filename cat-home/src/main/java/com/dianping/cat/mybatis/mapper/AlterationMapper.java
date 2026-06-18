package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.AlterationDO;

public interface AlterationMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	AlterationDO findByPrimaryKey(@Param("id") Long id);

	int insert(AlterationDO record);

	List<AlterationDO> queryAll();

	int updateByPrimaryKey(AlterationDO record);

	List<AlterationDO> findByTypeDruation(@Param("record") AlterationDO record);

	List<AlterationDO> findByDtdh(@Param("record") AlterationDO record);

	List<AlterationDO> findByDtdhTypes(@Param("record") AlterationDO record);

	List<AlterationDO> findByDomainAndTime(@Param("record") AlterationDO record);
}
