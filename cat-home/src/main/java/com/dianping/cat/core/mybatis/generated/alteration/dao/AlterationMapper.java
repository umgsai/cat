package com.dianping.cat.core.mybatis.generated.alteration.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.alteration.dao.data.AlterationDO;

public interface AlterationMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	AlterationDO findByPrimaryKey(@Param("id") Integer id);

	int insert(AlterationDO record);

	List<AlterationDO> queryAll();

	int updateByPrimaryKey(AlterationDO record);

	List<AlterationDO> findByTypeDruation(@Param("record") AlterationDO record);

	List<AlterationDO> findByDtdh(@Param("record") AlterationDO record);

	List<AlterationDO> findByDtdhTypes(@Param("record") AlterationDO record);

	List<AlterationDO> findByDomainAndTime(@Param("record") AlterationDO record);
}
