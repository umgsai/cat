package com.dianping.cat.core.mybatis.repository.task;

import com.dianping.cat.core.dal.Task;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.task.dao.TaskMapper;
import com.dianping.cat.core.mybatis.generated.task.dao.data.TaskDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class TaskRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/TaskMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return TaskMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public Task createLocal() {
		return new Task();
	}

	public int deleteByPK(Task proto) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Task.", e);
		}
	}

	public Task findByPK(int keyId, Readset<Task> readset) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			TaskDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Task.", e);
		}
	}

	public Task findByStatusConsumer(int status, String consumer, Readset<Task> readset) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			TaskDO record = new TaskDO();
			record.setStatus(status);
			record.setConsumer(consumer);
			TaskDO result = mapper.findByStatusConsumer(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByStatusConsumer", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByStatusConsumer for Task.", e);
		}
	}

	public int insert(Task proto) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			TaskDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Task.", e);
		}
	}

	public int updateByPK(Task proto, Updateset<Task> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Task.", e);
		}
	}

	public int updateTodoToDoing(Task proto, Updateset<Task> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			int count = mapper.updateTodoToDoing(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateTodoToDoing for Task.", e);
		}
	}

	public int updateDoingToDone(Task proto, Updateset<Task> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			int count = mapper.updateDoingToDone(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateDoingToDone for Task.", e);
		}
	}

	public int updateFailureToDone(Task proto, Updateset<Task> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			int count = mapper.updateFailureToDone(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateFailureToDone for Task.", e);
		}
	}

	public int updateStatusToTodo(Task proto, Updateset<Task> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			int count = mapper.updateStatusToTodo(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateStatusToTodo for Task.", e);
		}
	}

	public int updateDoingToFail(Task proto, Updateset<Task> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			TaskMapper mapper = session.getMapper(TaskMapper.class);
			int count = mapper.updateDoingToFail(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateDoingToFail for Task.", e);
		}
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
