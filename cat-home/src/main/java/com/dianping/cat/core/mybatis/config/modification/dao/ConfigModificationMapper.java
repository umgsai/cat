package com.dianping.cat.core.mybatis.config.modification.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.config.modification.dao.data.ConfigModificationDO;

public interface ConfigModificationMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	ConfigModificationDO findByPrimaryKey(@Param("id") Long id);

	int insert(ConfigModificationDO record);

	List<ConfigModificationDO> queryAll();

	int updateByPrimaryKey(ConfigModificationDO record);
}
