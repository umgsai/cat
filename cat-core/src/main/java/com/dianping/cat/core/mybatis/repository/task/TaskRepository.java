package com.dianping.cat.core.mybatis.repository.task;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.dal.Task;
import com.dianping.cat.core.mybatis.task.dao.TaskMapper;
import com.dianping.cat.core.mybatis.task.dao.data.TaskDO;

public class TaskRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public Task createLocal() {
		return new Task();
	}

	public int deleteByPK(Task proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
	}

	public Task findByPK(int keyId) {
		TaskMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public Task findByStatusConsumer(int status, String consumer) {
		TaskMapper mapper = springMapper();
		TaskDO record = new TaskDO();

		record.setStatus(status);
		record.setConsumer(consumer);
		TaskDO result = mapper.findByStatusConsumer(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByStatusConsumer", record.toString());
	}

	public int insert(Task proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		TaskDO record = toRecord(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(record));

		proto.setId(record.getId());
		proto.setKeyId(record.getId());
		return count;
	}

	public int updateByPK(Task proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
	}

	public int updateTodoToDoing(Task proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateTodoToDoing(toRecord(proto)));
	}

	public int updateDoingToDone(Task proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateDoingToDone(toRecord(proto)));
	}

	public int updateFailureToDone(Task proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateFailureToDone(toRecord(proto)));
	}

	public int updateStatusToTodo(Task proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateStatusToTodo(toRecord(proto)));
	}

	public int updateDoingToFail(Task proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateDoingToFail(toRecord(proto)));
	}

	private TaskMapper springMapper() {
		if (m_sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for TaskMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("TaskRepository is using Spring managed TaskMapper.");
		}
		return m_sqlSessionTemplate.getMapper(TaskMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for TaskMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private Task requireFound(TaskDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Task found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private Task toModel(TaskDO record) {
		Task model = new Task();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getProducer() != null) {
			model.setProducer(record.getProducer());
		}
		if (record.getConsumer() != null) {
			model.setConsumer(record.getConsumer());
		}
		if (record.getFailureCount() != null) {
			model.setFailureCount(record.getFailureCount());
		}
		if (record.getReportName() != null) {
			model.setReportName(record.getReportName());
		}
		if (record.getReportDomain() != null) {
			model.setReportDomain(record.getReportDomain());
		}
		if (record.getReportPeriod() != null) {
			model.setReportPeriod(record.getReportPeriod());
		}
		if (record.getStatus() != null) {
			model.setStatus(record.getStatus());
		}
		if (record.getTaskType() != null) {
			model.setTaskType(record.getTaskType());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getStartDate() != null) {
			model.setStartDate(record.getStartDate());
		}
		if (record.getEndDate() != null) {
			model.setEndDate(record.getEndDate());
		}
		if (record.getCount() != null) {
			model.setCount(record.getCount());
		}
		model.afterLoad();
		return model;
	}

	private TaskDO toRecord(Task model) {
		TaskDO record = new TaskDO();

		record.setId(model.getId());
		record.setProducer(model.getProducer());
		record.setConsumer(model.getConsumer());
		record.setFailureCount(model.getFailureCount());
		record.setReportName(model.getReportName());
		record.setReportDomain(model.getReportDomain());
		record.setReportPeriod(model.getReportPeriod());
		record.setStatus(model.getStatus());
		record.setTaskType(model.getTaskType());
		record.setCreationDate(model.getCreationDate());
		record.setStartDate(model.getStartDate());
		record.setEndDate(model.getEndDate());
		record.setKeyId(model.getKeyId());
		record.setStartLimit(model.getStartLimit());
		record.setEndLimit(model.getEndLimit());
		return record;
	}
}
