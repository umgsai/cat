package com.dianping.cat.core.mybatis.generated.operation.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.generated.operation.dao.data.OperationDO;

public interface OperationMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	OperationDO findByPrimaryKey(@Param("id") Integer id);

	int insert(OperationDO record);

	List<OperationDO> queryAll();

	int updateByPrimaryKey(OperationDO record);
}
