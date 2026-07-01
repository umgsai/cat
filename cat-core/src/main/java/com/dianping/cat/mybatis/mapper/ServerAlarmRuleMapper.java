package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.ServerAlarmRuleDO;

public interface ServerAlarmRuleMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	ServerAlarmRuleDO findByPrimaryKey(@Param("id") Long id);

	int insert(ServerAlarmRuleDO record);

	List<ServerAlarmRuleDO> queryAll();

	int updateByPrimaryKey(ServerAlarmRuleDO record);

	List<ServerAlarmRuleDO> findAll(@Param("record") ServerAlarmRuleDO record);
}
