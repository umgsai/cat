package com.dianping.cat.core.mybatis.repository.alert;

import com.dianping.cat.alarm.Alert;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.alert.dao.AlertMapper;
import com.dianping.cat.core.mybatis.generated.alert.dao.data.AlertDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class AlertRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlertMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return AlertMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public Alert createLocal() {
		return new Alert();
	}

	public int deleteByPK(Alert proto) throws DalException {
		try (SqlSession session = openSession()) {
			AlertMapper mapper = session.getMapper(AlertMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Alert.", e);
		}
	}

	public List<Alert> queryAlertsByTimeDomain(java.util.Date startTime, java.util.Date endTime, String domain, Readset<Alert> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlertMapper mapper = session.getMapper(AlertMapper.class);
			AlertDO record = new AlertDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			record.setDomain(domain);
			return mapper.queryAlertsByTimeDomain(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing queryAlertsByTimeDomain for Alert.", e);
		}
	}

	public List<Alert> queryAlertsByTimeDomainCategories(java.util.Date startTime, java.util.Date endTime, String domain, String[] categories, Readset<Alert> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlertMapper mapper = session.getMapper(AlertMapper.class);
			AlertDO record = new AlertDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			record.setDomain(domain);
			record.setCategories(categories);
			return mapper.queryAlertsByTimeDomainCategories(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing queryAlertsByTimeDomainCategories for Alert.", e);
		}
	}

	public List<Alert> queryAlertsByTimeCategoryDomain(java.util.Date startTime, java.util.Date endTime, String category, String domain, Readset<Alert> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlertMapper mapper = session.getMapper(AlertMapper.class);
			AlertDO record = new AlertDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			record.setCategory(category);
			record.setDomain(domain);
			return mapper.queryAlertsByTimeCategoryDomain(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing queryAlertsByTimeCategoryDomain for Alert.", e);
		}
	}

	public List<Alert> queryAlertsByTimeCategory(java.util.Date startTime, java.util.Date endTime, String category, Readset<Alert> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlertMapper mapper = session.getMapper(AlertMapper.class);
			AlertDO record = new AlertDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			record.setCategory(category);
			return mapper.queryAlertsByTimeCategory(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing queryAlertsByTimeCategory for Alert.", e);
		}
	}

	public Alert findByPK(int keyId, Readset<Alert> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlertMapper mapper = session.getMapper(AlertMapper.class);
			AlertDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Alert.", e);
		}
	}

	public int insert(Alert proto) throws DalException {
		try (SqlSession session = openSession()) {
			AlertMapper mapper = session.getMapper(AlertMapper.class);
			AlertDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Alert.", e);
		}
	}

	public int updateByPK(Alert proto, Updateset<Alert> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			AlertMapper mapper = session.getMapper(AlertMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Alert.", e);
		}
	}

	private Alert requireFound(AlertDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Alert found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private Alert toModel(AlertDO record) {
		Alert model = new Alert();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getAlertTime() != null) {
			model.setAlertTime(record.getAlertTime());
		}
		if (record.getCategory() != null) {
			model.setCategory(record.getCategory());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getMetric() != null) {
			model.setMetric(record.getMetric());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		model.afterLoad();
		return model;
	}

	private AlertDO toRecord(Alert model) {
		AlertDO record = new AlertDO();

		record.setId(model.getId());
		record.setDomain(model.getDomain());
		record.setAlertTime(model.getAlertTime());
		record.setCategory(model.getCategory());
		record.setType(model.getType());
		record.setContent(model.getContent());
		record.setMetric(model.getMetric());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		record.setStartTime(model.getStartTime());
		record.setEndTime(model.getEndTime());
		record.setCategories(model.getCategories());
		return record;
	}
}
