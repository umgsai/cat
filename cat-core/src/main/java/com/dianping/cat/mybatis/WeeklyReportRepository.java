package com.dianping.cat.mybatis;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.data.WeeklyReportDO;
import com.dianping.cat.mybatis.mapper.WeeklyReportMapper;

@Component("weeklyReportRepository")
public class WeeklyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyReportRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public WeeklyReportDO createLocal() {
		return new WeeklyReportDO();
	}

	public int deleteByPK(WeeklyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getId()));
	}

	public int deleteReportByDomainNamePeriod(WeeklyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteReportByDomainNamePeriod(proto));
	}

	public WeeklyReportDO findByPK(long keyId) {
		WeeklyReportMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public WeeklyReportDO findReportByDomainNamePeriod(java.util.Date period, String domain, String name) {
		WeeklyReportDO record = new WeeklyReportDO();
		WeeklyReportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setDomain(domain);
		record.setName(name);
		WeeklyReportDO result = mapper.findReportByDomainNamePeriod(record).stream().findFirst().orElse(null);

		return requireFound(result, "findReportByDomainNamePeriod", record.toString());
	}

	public int insert(WeeklyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().insert(proto));
	}

	public int updateByPK(WeeklyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
	}

	private WeeklyReportMapper springMapper() {
		SqlSessionTemplate template = sqlSessionTemplate;

		if (template == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for WeeklyreportMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("WeeklyReportRepository is using Spring managed WeeklyreportMapper.");
		}

		return template.getMapper(WeeklyReportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for WeeklyreportMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private WeeklyReportDO requireFound(WeeklyReportDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No WeeklyReport found by " + field + "(" + value + ").", 1);
		}

		return record;
	}

}
