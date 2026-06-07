package com.dianping.cat.core.mybatis.repository.hourlyreport;

import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.hourlyreport.dao.HourlyreportMapper;
import com.dianping.cat.core.mybatis.generated.hourlyreport.dao.data.HourlyreportDO;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class HourlyReportRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/HourlyreportMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return HourlyreportMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public HourlyReport createLocal() {
		return new HourlyReport();
	}

	public int deleteByPK(HourlyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyreportMapper mapper = session.getMapper(HourlyreportMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for HourlyReport.", e);
		}
	}

	public List<HourlyReport> findAllByDomainNamePeriod(java.util.Date period, String domain, String name,
			Readset<HourlyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyreportMapper mapper = session.getMapper(HourlyreportMapper.class);
			HourlyreportDO record = new HourlyreportDO();
			record.setPeriod(period);
			record.setDomain(domain);
			record.setName(name);
			return mapper.findAllByDomainNamePeriod(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findAllByDomainNamePeriod for HourlyReport.", e);
		}
	}

	public List<HourlyReport> findAllByPeriodName(java.util.Date period, String name, Readset<HourlyReport> readset)
			throws DalException {
		try (SqlSession session = openSession()) {
			HourlyreportMapper mapper = session.getMapper(HourlyreportMapper.class);
			HourlyreportDO record = new HourlyreportDO();
			record.setPeriod(period);
			record.setName(name);
			return mapper.findAllByPeriodName(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findAllByPeriodName for HourlyReport.", e);
		}
	}

	public HourlyReport findByPK(int keyId, Readset<HourlyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyreportMapper mapper = session.getMapper(HourlyreportMapper.class);
			HourlyreportDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for HourlyReport.", e);
		}
	}

	public int insert(HourlyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyreportMapper mapper = session.getMapper(HourlyreportMapper.class);
			HourlyreportDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for HourlyReport.", e);
		}
	}

	public int updateByPK(HourlyReport proto, Updateset<HourlyReport> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyreportMapper mapper = session.getMapper(HourlyreportMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for HourlyReport.", e);
		}
	}

	private HourlyReport requireFound(HourlyreportDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No HourlyReport found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private HourlyReport toModel(HourlyreportDO record) {
		HourlyReport model = new HourlyReport();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
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
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		model.afterLoad();
		return model;
	}

	private HourlyreportDO toRecord(HourlyReport model) {
		HourlyreportDO record = new HourlyreportDO();

		record.setId(model.getId());
		record.setType(model.getType());
		record.setName(model.getName());
		record.setIp(model.getIp());
		record.setDomain(model.getDomain());
		record.setPeriod(model.getPeriod());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
