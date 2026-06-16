package com.dianping.cat.core.mybatis.repository.monthly.report.content;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.mybatis.generated.monthly.report.content.dao.MonthlyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.monthly.report.content.dao.data.MonthlyReportContentDO;

public class MonthlyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyReportContentRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public MonthlyReportContent createLocal() {
		return new MonthlyReportContent();
	}

	public int deleteByPK(MonthlyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for MonthlyReportContent.", e);
		}
	}

	public List<MonthlyReportContent> findOverloadReport(int startId) {
		MonthlyReportContentMapper mapper = springMapper();
		MonthlyReportContentDO record = new MonthlyReportContentDO();

		record.setStartId(startId);
		try {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findOverloadReport for MonthlyReportContent.", e);
		}
	}

	public MonthlyReportContent findByPK(int keyReportId) {
		MonthlyReportContentMapper mapper = springMapper();

		try {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for MonthlyReportContent.", e);
		}
	}

	public int insert(MonthlyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for MonthlyReportContent.", e);
		}
	}

	public int updateByPK(MonthlyReportContent proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for MonthlyReportContent.", e);
		}
	}

	private MonthlyReportContentMapper springMapper() {
		if (m_sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for MonthlyReportContentMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("MonthlyReportContentRepository is using Spring managed MonthlyReportContentMapper.");
		}
		return m_sqlSessionTemplate.getMapper(MonthlyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for MonthlyReportContentMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private MonthlyReportContent requireFound(MonthlyReportContentDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No MonthlyReportContent found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private MonthlyReportContent toModel(MonthlyReportContentDO record) {
		MonthlyReportContent model = new MonthlyReportContent();

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

	private MonthlyReportContentDO toRecord(MonthlyReportContent model) {
		MonthlyReportContentDO record = new MonthlyReportContentDO();

		record.setReportId(model.getReportId());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setKeyReportId(model.getKeyReportId());
		record.setCapacity(model.getCapacity());
		record.setStartId(model.getStartId());
		return record;
	}
}
