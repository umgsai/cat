package com.dianping.cat.core.mybatis.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cat.core.mybatis.task.dao.data.TaskDO;

public interface TaskMapper {
	int deleteByPrimaryKey(@Param("id") Integer id);

	TaskDO findByPrimaryKey(@Param("id") Integer id);

	int insert(TaskDO record);

	List<TaskDO> queryAll();

	int updateByPrimaryKey(TaskDO record);

	List<TaskDO> findByStatusConsumer(@Param("record") TaskDO record);

	int updateTodoToDoing(@Param("record") TaskDO record);

	int updateDoingToDone(@Param("record") TaskDO record);

	int updateFailureToDone(@Param("record") TaskDO record);

	int updateStatusToTodo(@Param("record") TaskDO record);

	int updateDoingToFail(@Param("record") TaskDO record);
}
