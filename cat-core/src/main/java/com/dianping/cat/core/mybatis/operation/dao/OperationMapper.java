package com.dianping.cat.core.mybatis.operation.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.operation.dao.data.OperationDO;

public interface OperationMapper {
	int deleteByPrimaryKey(@Param("id") Long id);

	OperationDO findByPrimaryKey(@Param("id") Long id);

	int insert(OperationDO record);

	List<OperationDO> queryAll();

	int updateByPrimaryKey(OperationDO record);
}
