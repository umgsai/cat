package com.dianping.cat.core.mybatis.repository.task;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.core.dal.Task;
import com.dianping.cat.core.mybatis.generated.task.dao.TaskMapper;
import com.dianping.cat.core.mybatis.generated.task.dao.data.TaskDO;
import com.dianping.cat.core.mybatis.repository.SupportingMyBatisRepository;

public class TaskRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/TaskMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Inject
	private DataSourceManager m_dataSourceManager;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public Task createLocal() {
		return new Task();
	}

	public int deleteByPK(Task proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(TaskMapper.class).deleteByPrimaryKey(proto.getKeyId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Task.", e);
		}
	}

	public Task findByPK(int keyId, Readset<Task> readset) throws DalException {
		TaskMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		}

		try (SqlSession session = openSession()) {
			TaskDO record = session.getMapper(TaskMapper.class).findByPrimaryKey(keyId);

			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Task.", e);
		}
	}

	public Task findByStatusConsumer(int status, String consumer, Readset<Task> readset) throws DalException {
		TaskMapper mapper = springMapper();
		TaskDO record = new TaskDO();

		record.setStatus(status);
		record.setConsumer(consumer);
		if (mapper != null) {
			TaskDO result = mapper.findByStatusConsumer(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByStatusConsumer", record.toString());
		}

		try (SqlSession session = openSession()) {
			TaskDO result = session.getMapper(TaskMapper.class).findByStatusConsumer(record).stream().findFirst()
					.orElse(null);

			return requireFound(result, "findByStatusConsumer", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByStatusConsumer for Task.", e);
		}
	}

	public int insert(Task proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			TaskDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper().insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		}

		try (SqlSession session = openSession()) {
			TaskDO record = toRecord(proto);
			int count = session.getMapper(TaskMapper.class).insert(record);

			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Task.", e);
		}
	}

	public int updateByPK(Task proto, Updateset<Task> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(TaskMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Task.", e);
		}
	}

	public int updateTodoToDoing(Task proto, Updateset<Task> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateTodoToDoing(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(TaskMapper.class).updateTodoToDoing(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateTodoToDoing for Task.", e);
		}
	}

	public int updateDoingToDone(Task proto, Updateset<Task> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateDoingToDone(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(TaskMapper.class).updateDoingToDone(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateDoingToDone for Task.", e);
		}
	}

	public int updateFailureToDone(Task proto, Updateset<Task> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateFailureToDone(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(TaskMapper.class).updateFailureToDone(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateFailureToDone for Task.", e);
		}
	}

	public int updateStatusToTodo(Task proto, Updateset<Task> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateStatusToTodo(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(TaskMapper.class).updateStatusToTodo(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateStatusToTodo for Task.", e);
		}
	}

	public int updateDoingToFail(Task proto, Updateset<Task> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateDoingToFail(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(TaskMapper.class).updateDoingToFail(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateDoingToFail for Task.", e);
		}
	}

	private SqlSessionFactory getSqlSessionFactory() {
		SqlSessionFactory sqlSessionFactory = m_sqlSessionFactory;

		if (sqlSessionFactory == null) {
			synchronized (this) {
				sqlSessionFactory = m_sqlSessionFactory;

				if (sqlSessionFactory == null) {
					sqlSessionFactory = SupportingMyBatisRepository.newSqlSessionFactory(m_dataSourceManager,
							TaskMapper.class, MAPPER_RESOURCE);
					m_sqlSessionFactory = sqlSessionFactory;
				}
			}
		}

		return sqlSessionFactory;
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private TaskMapper springMapper() {
		return SupportingMyBatisRepository.springMapper(TaskMapper.class, LOGGER, SPRING_MAPPER_LOGGED,
				"TaskRepository is using Spring managed TaskMapper.");
	}

	private TransactionTemplate springTransactionTemplate() {
		return SupportingMyBatisRepository.springTransactionTemplate();
	}

	private Task requireFound(TaskDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Task found by " + field + "(" + value + ").");
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
