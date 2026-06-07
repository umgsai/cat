package com.dianping.cat.core.mybatis.repository.monthreport;

import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.monthreport.dao.MonthreportMapper;
import com.dianping.cat.core.mybatis.generated.monthreport.dao.data.MonthreportDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class MonthlyReportRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/MonthreportMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return MonthreportMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public MonthlyReport createLocal() {
		return new MonthlyReport();
	}

	public int deleteByPK(MonthlyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			MonthreportMapper mapper = session.getMapper(MonthreportMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for MonthlyReport.", e);
		}
	}

	public int deleteReportByDomainNamePeriod(MonthlyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			MonthreportMapper mapper = session.getMapper(MonthreportMapper.class);
			int count = mapper.deleteReportByDomainNamePeriod(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteReportByDomainNamePeriod for MonthlyReport.", e);
		}
	}

	public MonthlyReport findByPK(int keyId, Readset<MonthlyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MonthreportMapper mapper = session.getMapper(MonthreportMapper.class);
			MonthreportDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for MonthlyReport.", e);
		}
	}

	public MonthlyReport findReportByDomainNamePeriod(java.util.Date period, String domain, String name, Readset<MonthlyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MonthreportMapper mapper = session.getMapper(MonthreportMapper.class);
			MonthreportDO record = new MonthreportDO();
			record.setPeriod(period);
			record.setDomain(domain);
			record.setName(name);
			MonthreportDO result = mapper.findReportByDomainNamePeriod(record).stream().findFirst().orElse(null);
			return requireFound(result, "findReportByDomainNamePeriod", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findReportByDomainNamePeriod for MonthlyReport.", e);
		}
	}

	public int insert(MonthlyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			MonthreportMapper mapper = session.getMapper(MonthreportMapper.class);
			MonthreportDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for MonthlyReport.", e);
		}
	}

	public int updateByPK(MonthlyReport proto, Updateset<MonthlyReport> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			MonthreportMapper mapper = session.getMapper(MonthreportMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for MonthlyReport.", e);
		}
	}

	private MonthlyReport requireFound(MonthreportDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No MonthlyReport found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private MonthlyReport toModel(MonthreportDO record) {
		MonthlyReport model = new MonthlyReport();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getName() != null) {
			model.setName(record.getName());
		}
		if (record.getIp() != null) {
			model.setIp(record.getIp());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getPeriod() != null) {
			model.setPeriod(record.getPeriod());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		model.afterLoad();
		return model;
	}

	private MonthreportDO toRecord(MonthlyReport model) {
		MonthreportDO record = new MonthreportDO();

		record.setId(model.getId());
		record.setName(model.getName());
		record.setIp(model.getIp());
		record.setDomain(model.getDomain());
		record.setPeriod(model.getPeriod());
		record.setType(model.getType());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
