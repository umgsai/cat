package com.dianping.cat.core.mybatis.repository.hourlyreport;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.mybatis.generated.hourlyreport.dao.HourlyreportMapper;
import com.dianping.cat.core.mybatis.generated.hourlyreport.dao.data.HourlyreportDO;

public class HourlyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(HourlyReportRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public HourlyReport createLocal() {
		return new HourlyReport();
	}

	public int deleteByPK(HourlyReport proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
	}

	public List<HourlyReport> findAllByDomainNamePeriod(java.util.Date period, String domain, String name,
			Readset<HourlyReport> readset) throws DalException {
		HourlyreportDO record = new HourlyreportDO();
		HourlyreportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setDomain(domain);
		record.setName(name);
		return mapper.findAllByDomainNamePeriod(record).stream().map(this::toModel).collect(Collectors.toList());
	}

	public List<HourlyReport> findAllByPeriodName(java.util.Date period, String name, Readset<HourlyReport> readset)
			throws DalException {
		HourlyreportDO record = new HourlyreportDO();
		HourlyreportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setName(name);
		return mapper.findAllByPeriodName(record).stream().map(this::toModel).collect(Collectors.toList());
	}

	public HourlyReport findByPK(int keyId, Readset<HourlyReport> readset) throws DalException {
		HourlyreportMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public int insert(HourlyReport proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		HourlyreportDO record = toRecord(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(record));

		proto.setId(record.getId());
		proto.setKeyId(record.getId());
		return count;
	}

	public int updateByPK(HourlyReport proto, Updateset<HourlyReport> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
	}

	private HourlyreportMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for HourlyreportMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("HourlyReportRepository is using Spring managed HourlyreportMapper.");
		}

		return sqlSessionTemplate.getMapper(HourlyreportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for HourlyreportMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
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
