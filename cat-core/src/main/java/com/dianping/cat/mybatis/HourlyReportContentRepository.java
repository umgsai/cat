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

import com.dianping.cat.mybatis.data.HourlyReportContentDO;
import com.dianping.cat.mybatis.mapper.HourlyReportContentMapper;

@Component("hourlyReportContentRepository")
public class HourlyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(HourlyReportContentRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public HourlyReportContentDO createLocal() {
		return new HourlyReportContentDO();
	}

	public int deleteByPK(HourlyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getReportId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for HourlyReportContent.", e);
		}
	}

	public List<HourlyReportContentDO> findOverloadReport(long startId) {
		HourlyReportContentMapper mapper = springMapper();
		HourlyReportContentDO record = new HourlyReportContentDO();

		record.setStartId(startId);
		try {
			return mapper.findOverloadReport(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findOverloadReport for HourlyReportContent.", e);
		}
	}

	public HourlyReportContentDO findByPK(long keyReportId, java.util.Date period) {
		HourlyReportContentMapper mapper = springMapper();

		try {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for HourlyReportContent.", e);
		}
	}

	public int insert(HourlyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for HourlyReportContent.", e);
		}
	}

	public int updateByPK(HourlyReportContentDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for HourlyReportContent.", e);
		}
	}

	private HourlyReportContentMapper springMapper() {
		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for HourlyReportContentMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("HourlyReportContentRepository is using Spring managed HourlyReportContentMapper.");
		}
		return sqlSessionTemplate.getMapper(HourlyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for HourlyReportContentMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private HourlyReportContentDO requireFound(HourlyReportContentDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No HourlyReportContent found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
