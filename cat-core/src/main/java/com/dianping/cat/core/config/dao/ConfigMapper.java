package com.dianping.cat.core.config.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.config.dao.data.ConfigDO;

public interface ConfigMapper {
	int deleteById(@Param("id") Integer id);

	ConfigDO findById(@Param("id") Integer id);

	ConfigDO findByName(@Param("name") String name);

	List<ConfigDO> queryAll();

	int insert(ConfigDO config);

	int updateById(ConfigDO config);
}
