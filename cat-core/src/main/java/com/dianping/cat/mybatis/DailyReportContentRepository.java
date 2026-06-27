package com.dianping.cat.mybatis;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.mybatis.mapper.DailyReportContentMapper;
import com.dianping.cat.mybatis.data.DailyReportContentDO;

@Component("dailyReportContentRepository")
public class DailyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyReportContentRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public DailyReportContent createLocal() {
		return new DailyReportContent();
	}

	public int deleteByPK(DailyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for DailyReportContent.", e);
		}
	}

	public List<DailyReportContent> findOverloadReport(long startId) {
		DailyReportContentMapper mapper = springMapper();
		DailyReportContentDO record = new DailyReportContentDO();

		record.setStartId(startId);
		try {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findOverloadReport for DailyReportContent.", e);
		}
	}

	public DailyReportContent findByPK(long keyReportId) {
		DailyReportContentMapper mapper = springMapper();

		try {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for DailyReportContent.", e);
		}
	}

	public int insert(DailyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for DailyReportContent.", e);
		}
	}

	public int updateByPK(DailyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for DailyReportContent.", e);
		}
	}

	private DailyReportContentMapper springMapper() {
		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for DailyReportContentMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("DailyReportContentRepository is using Spring managed DailyReportContentMapper.");
		}
		return sqlSessionTemplate.getMapper(DailyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for DailyReportContentMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private DailyReportContent requireFound(DailyReportContentDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No DailyReportContent found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private DailyReportContent toModel(DailyReportContentDO record) {
		DailyReportContent model = new DailyReportContent();

		if (record.getReportId() != null) {
			model.setReportId(record.getReportId());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getCreateTime() != null) {
			model.setCreateTime(record.getCreateTime());
		}
		if (record.getUpdateTime() != null) {
			model.setUpdateTime(record.getUpdateTime());
		}
		if (record.getContentLength() != null) {
			model.setContentLength(record.getContentLength());
		}
		model.afterLoad();
		return model;
	}

	private DailyReportContentDO toRecord(DailyReportContent model) {
		DailyReportContentDO record = new DailyReportContentDO();

		record.setReportId(model.getReportId());
		record.setContent(model.getContent());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyReportId(model.getKeyReportId());
		record.setStartId(model.getStartId());
		record.setEndId(model.getEndId());
		record.setCapacity(model.getCapacity());
		return record;
	}
}
