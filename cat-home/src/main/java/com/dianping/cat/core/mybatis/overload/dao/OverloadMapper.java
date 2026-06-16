package com.dianping.cat.core.mybatis.overload.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.overload.dao.data.OverloadDO;

public interface OverloadMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	OverloadDO findByPrimaryKey(@Param("id") Integer id);

	int insert(OverloadDO record);

	List<OverloadDO> queryAll();

	int updateByPrimaryKey(OverloadDO record);

	List<OverloadDO> findMaxIdByType(@Param("record") OverloadDO record);

	List<OverloadDO> findCount(@Param("record") OverloadDO record);

	List<OverloadDO> findIdAndSizeByDuration(@Param("record") OverloadDO record);
}
