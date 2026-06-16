package com.dianping.cat.core.mybatis.user.define.rule.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.user.define.rule.dao.data.UserDefineRuleDO;

public interface UserDefineRuleMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	UserDefineRuleDO findByPrimaryKey(@Param("id") Integer id);

	int insert(UserDefineRuleDO record);

	List<UserDefineRuleDO> queryAll();

	int updateByPrimaryKey(UserDefineRuleDO record);

	List<UserDefineRuleDO> findMaxId(@Param("record") UserDefineRuleDO record);
}
