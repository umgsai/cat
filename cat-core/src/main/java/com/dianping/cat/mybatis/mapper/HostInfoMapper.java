package com.dianping.cat.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.mybatis.data.HostInfoDO;

public interface HostInfoMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	HostInfoDO findByPrimaryKey(@Param("id") Long id);

	int insert(HostInfoDO record);

	List<HostInfoDO> queryAll();

	int updateByPrimaryKey(HostInfoDO record);

	List<HostInfoDO> findByIp(@Param("record") HostInfoDO record);

	List<HostInfoDO> findAllIp(@Param("record") HostInfoDO record);
}
