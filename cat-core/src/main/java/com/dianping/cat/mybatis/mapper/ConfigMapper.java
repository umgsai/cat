package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.ConfigDO;

public interface ConfigMapper {
	int deleteById(@Param("id") Long id);

	ConfigDO findById(@Param("id") Long id);

	ConfigDO findByName(@Param("name") String name);

	List<ConfigDO> queryAll();

	int insert(ConfigDO config);

	int updateById(ConfigDO config);
}
