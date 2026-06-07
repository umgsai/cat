package com.dianping.cat.core.mybatis.repository.weeklyreport;

import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.weeklyreport.dao.WeeklyreportMapper;
import com.dianping.cat.core.mybatis.generated.weeklyreport.dao.data.WeeklyreportDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class WeeklyReportRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/WeeklyreportMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return WeeklyreportMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public WeeklyReport createLocal() {
		return new WeeklyReport();
	}

	public int deleteByPK(WeeklyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyreportMapper mapper = session.getMapper(WeeklyreportMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for WeeklyReport.", e);
		}
	}

	public int deleteReportByDomainNamePeriod(WeeklyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyreportMapper mapper = session.getMapper(WeeklyreportMapper.class);
			int count = mapper.deleteReportByDomainNamePeriod(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteReportByDomainNamePeriod for WeeklyReport.", e);
		}
	}

	public WeeklyReport findByPK(int keyId, Readset<WeeklyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyreportMapper mapper = session.getMapper(WeeklyreportMapper.class);
			WeeklyreportDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for WeeklyReport.", e);
		}
	}

	public WeeklyReport findReportByDomainNamePeriod(java.util.Date period, String domain, String name, Readset<WeeklyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyreportMapper mapper = session.getMapper(WeeklyreportMapper.class);
			WeeklyreportDO record = new WeeklyreportDO();
			record.setPeriod(period);
			record.setDomain(domain);
			record.setName(name);
			WeeklyreportDO result = mapper.findReportByDomainNamePeriod(record).stream().findFirst().orElse(null);
			return requireFound(result, "findReportByDomainNamePeriod", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findReportByDomainNamePeriod for WeeklyReport.", e);
		}
	}

	public int insert(WeeklyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyreportMapper mapper = session.getMapper(WeeklyreportMapper.class);
			WeeklyreportDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for WeeklyReport.", e);
		}
	}

	public int updateByPK(WeeklyReport proto, Updateset<WeeklyReport> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyreportMapper mapper = session.getMapper(WeeklyreportMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for WeeklyReport.", e);
		}
	}

	private WeeklyReport requireFound(WeeklyreportDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No WeeklyReport found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private WeeklyReport toModel(WeeklyreportDO record) {
		WeeklyReport model = new WeeklyReport();

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

	private WeeklyreportDO toRecord(WeeklyReport model) {
		WeeklyreportDO record = new WeeklyreportDO();

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
