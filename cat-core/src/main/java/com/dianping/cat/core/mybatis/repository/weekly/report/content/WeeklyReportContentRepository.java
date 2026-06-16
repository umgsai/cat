package com.dianping.cat.core.mybatis.repository.weekly.report.content;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.core.mybatis.weekly.report.content.dao.WeeklyReportContentMapper;
import com.dianping.cat.core.mybatis.weekly.report.content.dao.data.WeeklyReportContentDO;

public class WeeklyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyReportContentRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public WeeklyReportContent createLocal() {
		return new WeeklyReportContent();
	}

	public int deleteByPK(WeeklyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for WeeklyReportContent.", e);
		}
	}

	public List<WeeklyReportContent> findOverloadReport(long startId) {
		WeeklyReportContentMapper mapper = springMapper();
		WeeklyReportContentDO record = new WeeklyReportContentDO();

		record.setStartId(startId);
		try {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findOverloadReport for WeeklyReportContent.", e);
		}
	}

	public WeeklyReportContent findByPK(long keyReportId) {
		WeeklyReportContentMapper mapper = springMapper();

		try {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for WeeklyReportContent.", e);
		}
	}

	public int insert(WeeklyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for WeeklyReportContent.", e);
		}
	}

	public int updateByPK(WeeklyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for WeeklyReportContent.", e);
		}
	}

	private WeeklyReportContentMapper springMapper() {
		if (m_sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for WeeklyReportContentMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("WeeklyReportContentRepository is using Spring managed WeeklyReportContentMapper.");
		}
		return m_sqlSessionTemplate.getMapper(WeeklyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for WeeklyReportContentMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private WeeklyReportContent requireFound(WeeklyReportContentDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No WeeklyReportContent found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private WeeklyReportContent toModel(WeeklyReportContentDO record) {
		WeeklyReportContent model = new WeeklyReportContent();

		if (record.getReportId() != null) {
			model.setReportId(record.getReportId());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getContentLength() != null) {
			model.setContentLength(record.getContentLength());
		}
		model.afterLoad();
		return model;
	}

	private WeeklyReportContentDO toRecord(WeeklyReportContent model) {
		WeeklyReportContentDO record = new WeeklyReportContentDO();

		record.setReportId(model.getReportId());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setKeyReportId(model.getKeyReportId());
		record.setCapacity(model.getCapacity());
		record.setStartId(model.getStartId());
		return record;
	}
}
