package com.dianping.cat.core.mybatis.hostinfo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.hostinfo.dao.data.HostinfoDO;

public interface HostinfoMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	HostinfoDO findByPrimaryKey(@Param("id") Integer id);

	int insert(HostinfoDO record);

	List<HostinfoDO> queryAll();

	int updateByPrimaryKey(HostinfoDO record);

	List<HostinfoDO> findByIp(@Param("record") HostinfoDO record);

	List<HostinfoDO> findAllIp(@Param("record") HostinfoDO record);
}
