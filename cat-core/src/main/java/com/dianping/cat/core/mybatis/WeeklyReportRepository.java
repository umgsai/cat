package com.dianping.cat.core.mybatis.repository.weeklyreport;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.mybatis.weeklyreport.dao.WeeklyreportMapper;
import com.dianping.cat.core.mybatis.weeklyreport.dao.data.WeeklyreportDO;

public class WeeklyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyReportRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public WeeklyReport createLocal() {
		return new WeeklyReport();
	}

	public int deleteByPK(WeeklyReport proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
	}

	public int deleteReportByDomainNamePeriod(WeeklyReport proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteReportByDomainNamePeriod(toRecord(proto)));
	}

	public WeeklyReport findByPK(long keyId) {
		WeeklyreportMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public WeeklyReport findReportByDomainNamePeriod(java.util.Date period, String domain, String name) {
		WeeklyreportDO record = new WeeklyreportDO();
		WeeklyreportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setDomain(domain);
		record.setName(name);
		WeeklyreportDO result = mapper.findReportByDomainNamePeriod(record).stream().findFirst().orElse(null);

		return requireFound(result, "findReportByDomainNamePeriod", record.toString());
	}

	public int insert(WeeklyReport proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		WeeklyreportDO record = toRecord(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(record));

		proto.setId(record.getId());
		proto.setKeyId(record.getId());
		return count;
	}

	public int updateByPK(WeeklyReport proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
	}

	private WeeklyreportMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for WeeklyreportMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("WeeklyReportRepository is using Spring managed WeeklyreportMapper.");
		}

		return sqlSessionTemplate.getMapper(WeeklyreportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for WeeklyreportMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private WeeklyReport requireFound(WeeklyreportDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No WeeklyReport found by " + field + "(" + value + ").", 1);
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
		if (record.getCreateTime() != null) {
			model.setCreateTime(record.getCreateTime());
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
		record.setCreateTime(model.getCreateTime());
		record.setKeyId(model.getKeyId());
		return record;
	}

}
