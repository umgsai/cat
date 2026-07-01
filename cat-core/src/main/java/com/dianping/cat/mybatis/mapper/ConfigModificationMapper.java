package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.ConfigModificationDO;

public interface ConfigModificationMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	ConfigModificationDO findByPrimaryKey(@Param("id") Long id);

	int insert(ConfigModificationDO record);

	List<ConfigModificationDO> queryAll();

	int updateByPrimaryKey(ConfigModificationDO record);
}
