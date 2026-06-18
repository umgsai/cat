package com.dianping.cat.core.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.data.ProjectDO;

public interface ProjectMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	ProjectDO findByPrimaryKey(@Param("id") Long id);

	int insert(ProjectDO record);

	List<ProjectDO> queryAll();

	int updateByPrimaryKey(ProjectDO record);

	List<ProjectDO> findAll(@Param("record") ProjectDO record);

	List<ProjectDO> findByDomain(@Param("record") ProjectDO record);

	List<ProjectDO> findByCmdbDomain(@Param("record") ProjectDO record);
}
