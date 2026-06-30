package com.dianping.cat.mybatis;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.data.TaskDO;
import com.dianping.cat.mybatis.mapper.TaskMapper;

@Component("taskRepository")
public class TaskRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public TaskDO createLocal() {
		return new TaskDO();
	}

	public int deleteByPK(TaskDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getId()));
	}

	public TaskDO findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public TaskDO findByPK(long keyId) {
		TaskMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public TaskDO findByStatusConsumer(int status, String consumer) {
		TaskMapper mapper = springMapper();
		TaskDO record = new TaskDO();

		record.setStatus(status);
		record.setConsumer(consumer);
		TaskDO result = mapper.findByStatusConsumer(record).stream().findFirst().orElse(null);

		return result;
	}

	public int insert(TaskDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		int count = transactionTemplate.execute(status -> springMapper().insert(proto));

		return count;
	}

	public int updateByPK(TaskDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
	}

	public int updateTodoToDoing(TaskDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateTodoToDoing(proto));
	}

	public int updateDoingToDone(TaskDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateDoingToDone(proto));
	}

	public int updateFailureToDone(TaskDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateFailureToDone(proto));
	}

	public int updateStatusToTodo(TaskDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateStatusToTodo(proto));
	}

	public int updateDoingToFail(TaskDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateDoingToFail(proto));
	}

	private TaskMapper springMapper() {
		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for TaskMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("TaskRepository is using Spring managed TaskMapper.");
		}
		return sqlSessionTemplate.getMapper(TaskMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for TaskMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private TaskDO requireFound(TaskDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Task found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
