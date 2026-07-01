package com.dianping.cat.mybatis;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.mapper.MonthReportMapper;
import com.dianping.cat.mybatis.data.MonthReportDO;

@Component("monthlyReportRepository")
public class MonthlyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyReportRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public MonthReportDO createLocal() {
		return new MonthReportDO();
	}

	public int deleteByPK(MonthReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getId()));
	}

	public int deleteReportByDomainNamePeriod(MonthReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteReportByDomainNamePeriod(proto));
	}

	public MonthReportDO findByPK(long keyId) {
		MonthReportMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public MonthReportDO findReportByDomainNamePeriod(java.util.Date period, String domain, String name) {
		MonthReportDO record = new MonthReportDO();
		MonthReportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setDomain(domain);
		record.setName(name);
		MonthReportDO result = mapper.findReportByDomainNamePeriod(record).stream().findFirst().orElse(null);

		return requireFound(result, "findReportByDomainNamePeriod", record.toString());
	}

	public int insert(MonthReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().insert(proto));
	}

	public int updateByPK(MonthReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
	}

	private MonthReportMapper springMapper() {
		SqlSessionTemplate template = sqlSessionTemplate;

		if (template == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for MonthreportMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("MonthlyReportRepository is using Spring managed MonthreportMapper.");
		}

		return template.getMapper(MonthReportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for MonthreportMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private MonthReportDO requireFound(MonthReportDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No MonthlyReport found by " + field + "(" + value + ").", 1);
		}

		return record;
	}

}
