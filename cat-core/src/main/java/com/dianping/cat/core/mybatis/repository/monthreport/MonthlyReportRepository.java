package com.dianping.cat.core.mybatis.repository.monthreport;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.mybatis.generated.monthreport.dao.MonthreportMapper;
import com.dianping.cat.core.mybatis.generated.monthreport.dao.data.MonthreportDO;

public class MonthlyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyReportRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public MonthlyReport createLocal() {
		return new MonthlyReport();
	}

	public int deleteByPK(MonthlyReport proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
	}

	public int deleteReportByDomainNamePeriod(MonthlyReport proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteReportByDomainNamePeriod(toRecord(proto)));
	}

	public MonthlyReport findByPK(int keyId, Readset<MonthlyReport> readset) throws DalException {
		MonthreportMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public MonthlyReport findReportByDomainNamePeriod(java.util.Date period, String domain, String name,
			Readset<MonthlyReport> readset) throws DalException {
		MonthreportDO record = new MonthreportDO();
		MonthreportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setDomain(domain);
		record.setName(name);
		MonthreportDO result = mapper.findReportByDomainNamePeriod(record).stream().findFirst().orElse(null);

		return requireFound(result, "findReportByDomainNamePeriod", record.toString());
	}

	public int insert(MonthlyReport proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		MonthreportDO record = toRecord(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(record));

		proto.setId(record.getId());
		proto.setKeyId(record.getId());
		return count;
	}

	public int updateByPK(MonthlyReport proto, Updateset<MonthlyReport> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
	}

	private MonthreportMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for MonthreportMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("MonthlyReportRepository is using Spring managed MonthreportMapper.");
		}

		return sqlSessionTemplate.getMapper(MonthreportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for MonthreportMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
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
