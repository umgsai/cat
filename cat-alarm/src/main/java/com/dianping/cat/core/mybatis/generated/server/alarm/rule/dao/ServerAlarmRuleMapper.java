package com.dianping.cat.core.mybatis.generated.server.alarm.rule.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.server.alarm.rule.dao.data.ServerAlarmRuleDO;

public interface ServerAlarmRuleMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	ServerAlarmRuleDO findByPrimaryKey(@Param("id") Integer id);

	int insert(ServerAlarmRuleDO record);

	List<ServerAlarmRuleDO> queryAll();

	int updateByPrimaryKey(ServerAlarmRuleDO record);

	List<ServerAlarmRuleDO> findAll(@Param("record") ServerAlarmRuleDO record);
}
