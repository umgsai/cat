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

import com.dianping.cat.mybatis.data.MonthlyReportContentDO;
import com.dianping.cat.mybatis.mapper.MonthlyReportContentMapper;

@Component("monthlyReportContentRepository")
public class MonthlyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyReportContentRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public MonthlyReportContentDO createLocal() {
		return new MonthlyReportContentDO();
	}

	public int deleteByPK(MonthlyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getReportId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for MonthlyReportContent.", e);
		}
	}

	public List<MonthlyReportContentDO> findOverloadReport(long startId) {
		MonthlyReportContentMapper mapper = springMapper();
		MonthlyReportContentDO record = new MonthlyReportContentDO();

		record.setStartId(startId);
		try {
			return mapper.findOverloadReport(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findOverloadReport for MonthlyReportContent.", e);
		}
	}

	public MonthlyReportContentDO findByPK(long keyReportId) {
		MonthlyReportContentMapper mapper = springMapper();

		try {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for MonthlyReportContent.", e);
		}
	}

	public int insert(MonthlyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for MonthlyReportContent.", e);
		}
	}

	public int updateByPK(MonthlyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for MonthlyReportContent.", e);
		}
	}

	private MonthlyReportContentMapper springMapper() {
		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for MonthlyReportContentMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("MonthlyReportContentRepository is using Spring managed MonthlyReportContentMapper.");
		}
		return sqlSessionTemplate.getMapper(MonthlyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for MonthlyReportContentMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private MonthlyReportContentDO requireFound(MonthlyReportContentDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No MonthlyReportContent found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
