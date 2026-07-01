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

import com.dianping.cat.mybatis.data.WeeklyReportContentDO;
import com.dianping.cat.mybatis.mapper.WeeklyReportContentMapper;

@Component("weeklyReportContentRepository")
public class WeeklyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyReportContentRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public WeeklyReportContentDO createLocal() {
		return new WeeklyReportContentDO();
	}

	public int deleteByPK(WeeklyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getReportId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for WeeklyReportContent.", e);
		}
	}

	public List<WeeklyReportContentDO> findOverloadReport(long startId) {
		WeeklyReportContentMapper mapper = springMapper();
		WeeklyReportContentDO record = new WeeklyReportContentDO();

		record.setStartId(startId);
		try {
			return mapper.findOverloadReport(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findOverloadReport for WeeklyReportContent.", e);
		}
	}

	public WeeklyReportContentDO findByPK(long keyReportId) {
		WeeklyReportContentMapper mapper = springMapper();

		try {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for WeeklyReportContent.", e);
		}
	}

	public int insert(WeeklyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for WeeklyReportContent.", e);
		}
	}

	public int updateByPK(WeeklyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for WeeklyReportContent.", e);
		}
	}

	private WeeklyReportContentMapper springMapper() {
		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for WeeklyReportContentMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("WeeklyReportContentRepository is using Spring managed WeeklyReportContentMapper.");
		}
		return sqlSessionTemplate.getMapper(WeeklyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for WeeklyReportContentMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private WeeklyReportContentDO requireFound(WeeklyReportContentDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No WeeklyReportContent found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
