package com.dianping.cat.mybatis;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.data.HourlyReportDO;
import com.dianping.cat.mybatis.mapper.HourlyReportMapper;

@Component("hourlyReportRepository")
public class HourlyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(HourlyReportRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public HourlyReportDO createLocal() {
		return new HourlyReportDO();
	}

	public int deleteByPK(HourlyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getId()));
	}

	public List<HourlyReportDO> findAllByDomainNamePeriod(java.util.Date period, String domain, String name) {
		HourlyReportDO record = new HourlyReportDO();
		HourlyReportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setDomain(domain);
		record.setName(name);
		return mapper.findAllByDomainNamePeriod(record);
	}

	public List<HourlyReportDO> findAllByPeriodName(java.util.Date period, String name) {
		HourlyReportDO record = new HourlyReportDO();
		HourlyReportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setName(name);
		return mapper.findAllByPeriodName(record);
	}

	public HourlyReportDO findByPK(long keyId) {
		HourlyReportMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public int insert(HourlyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().insert(proto));
	}

	public int updateByPK(HourlyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
	}

	private HourlyReportMapper springMapper() {
		SqlSessionTemplate template = sqlSessionTemplate;

		if (template == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for HourlyreportMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("HourlyReportRepository is using Spring managed HourlyreportMapper.");
		}

		return template.getMapper(HourlyReportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for HourlyreportMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private HourlyReportDO requireFound(HourlyReportDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No HourlyReport found by " + field + "(" + value + ").", 1);
		}

		return record;
	}

}
