package com.dianping.cat.mybatis.user.define.rule.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.user.define.rule.dao.data.UserDefineRuleDO;

public interface UserDefineRuleMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	UserDefineRuleDO findByPrimaryKey(@Param("id") Long id);

	int insert(UserDefineRuleDO record);

	List<UserDefineRuleDO> queryAll();

	int updateByPrimaryKey(UserDefineRuleDO record);

	List<UserDefineRuleDO> findMaxId(@Param("record") UserDefineRuleDO record);
}
