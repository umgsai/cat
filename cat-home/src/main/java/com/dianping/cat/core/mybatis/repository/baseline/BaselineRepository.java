package com.dianping.cat.core.mybatis.repository.baseline;

import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.baseline.dao.BaselineMapper;
import com.dianping.cat.core.mybatis.generated.baseline.dao.data.BaselineDO;
import com.dianping.cat.home.dal.report.Baseline;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class BaselineRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/BaselineMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return BaselineMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public Baseline createLocal() {
		return new Baseline();
	}

	public int deleteByPK(Baseline proto) throws DalException {
		try (SqlSession session = openSession()) {
			BaselineMapper mapper = session.getMapper(BaselineMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Baseline.", e);
		}
	}

	public Baseline findByPK(int keyId, Readset<Baseline> readset) throws DalException {
		try (SqlSession session = openSession()) {
			BaselineMapper mapper = session.getMapper(BaselineMapper.class);
			BaselineDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Baseline.", e);
		}
	}

	public Baseline findByReportNameKeyTime(java.util.Date reportPeriod, String reportName, String indexKey, Readset<Baseline> readset) throws DalException {
		try (SqlSession session = openSession()) {
			BaselineMapper mapper = session.getMapper(BaselineMapper.class);
			BaselineDO record = new BaselineDO();
			record.setReportPeriod(reportPeriod);
			record.setReportName(reportName);
			record.setIndexKey(indexKey);
			BaselineDO result = mapper.findByReportNameKeyTime(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByReportNameKeyTime", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByReportNameKeyTime for Baseline.", e);
		}
	}

	public int insert(Baseline proto) throws DalException {
		try (SqlSession session = openSession()) {
			BaselineMapper mapper = session.getMapper(BaselineMapper.class);
			BaselineDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Baseline.", e);
		}
	}

	public int updateByPK(Baseline proto, Updateset<Baseline> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			BaselineMapper mapper = session.getMapper(BaselineMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Baseline.", e);
		}
	}

	private Baseline requireFound(BaselineDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Baseline found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private Baseline toModel(BaselineDO record) {
		Baseline model = new Baseline();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getReportName() != null) {
			model.setReportName(record.getReportName());
		}
		if (record.getIndexKey() != null) {
			model.setIndexKey(record.getIndexKey());
		}
		if (record.getReportPeriod() != null) {
			model.setReportPeriod(record.getReportPeriod());
		}
		if (record.getData() != null) {
			model.setData(record.getData());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		model.afterLoad();
		return model;
	}

	private BaselineDO toRecord(Baseline model) {
		BaselineDO record = new BaselineDO();

		record.setId(model.getId());
		record.setReportName(model.getReportName());
		record.setIndexKey(model.getIndexKey());
		record.setReportPeriod(model.getReportPeriod());
		record.setData(model.getData());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		record.setDataInDoubleArray(model.getDataInDoubleArray());
		return record;
	}
}
