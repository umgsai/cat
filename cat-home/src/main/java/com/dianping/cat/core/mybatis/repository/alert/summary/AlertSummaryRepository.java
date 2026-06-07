package com.dianping.cat.core.mybatis.repository.alert.summary;

import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.alert.summary.dao.AlertSummaryMapper;
import com.dianping.cat.core.mybatis.generated.alert.summary.dao.data.AlertSummaryDO;
import com.dianping.cat.home.dal.report.AlertSummary;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class AlertSummaryRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlertSummaryMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return AlertSummaryMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public AlertSummary createLocal() {
		return new AlertSummary();
	}

	public int deleteByPK(AlertSummary proto) throws DalException {
		try (SqlSession session = openSession()) {
			AlertSummaryMapper mapper = session.getMapper(AlertSummaryMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for AlertSummary.", e);
		}
	}

	public AlertSummary findByPK(int keyId, Readset<AlertSummary> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlertSummaryMapper mapper = session.getMapper(AlertSummaryMapper.class);
			AlertSummaryDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for AlertSummary.", e);
		}
	}

	public int insert(AlertSummary proto) throws DalException {
		try (SqlSession session = openSession()) {
			AlertSummaryMapper mapper = session.getMapper(AlertSummaryMapper.class);
			AlertSummaryDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for AlertSummary.", e);
		}
	}

	public int updateByPK(AlertSummary proto, Updateset<AlertSummary> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			AlertSummaryMapper mapper = session.getMapper(AlertSummaryMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for AlertSummary.", e);
		}
	}

	private AlertSummary requireFound(AlertSummaryDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No AlertSummary found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private AlertSummary toModel(AlertSummaryDO record) {
		AlertSummary model = new AlertSummary();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getAlertTime() != null) {
			model.setAlertTime(record.getAlertTime());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		model.afterLoad();
		return model;
	}

	private AlertSummaryDO toRecord(AlertSummary model) {
		AlertSummaryDO record = new AlertSummaryDO();

		record.setId(model.getId());
		record.setDomain(model.getDomain());
		record.setAlertTime(model.getAlertTime());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
