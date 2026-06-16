package com.dianping.cat.core.mybatis.repository.daily.report.content;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.mybatis.generated.daily.report.content.dao.DailyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.daily.report.content.dao.data.DailyReportContentDO;

public class DailyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyReportContentRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public DailyReportContent createLocal() {
		return new DailyReportContent();
	}

	public int deleteByPK(DailyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for DailyReportContent.", e);
		}
	}

	public List<DailyReportContent> findOverloadReport(int startId, Readset<DailyReportContent> readset)
			throws DalException {
		DailyReportContentMapper mapper = springMapper();
		DailyReportContentDO record = new DailyReportContentDO();

		record.setStartId(startId);
		try {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for DailyReportContent.", e);
		}
	}

	public DailyReportContent findByPK(int keyReportId, Readset<DailyReportContent> readset) throws DalException {
		DailyReportContentMapper mapper = springMapper();

		try {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for DailyReportContent.", e);
		}
	}

	public int insert(DailyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		} catch (Exception e) {
			throw new DalException("Error when executing insert for DailyReportContent.", e);
		}
	}

	public int updateByPK(DailyReportContent proto, Updateset<DailyReportContent> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for DailyReportContent.", e);
		}
	}

	private DailyReportContentMapper springMapper() {
		if (m_sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for DailyReportContentMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("DailyReportContentRepository is using Spring managed DailyReportContentMapper.");
		}
		return m_sqlSessionTemplate.getMapper(DailyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for DailyReportContentMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private DailyReportContent requireFound(DailyReportContentDO record, String field, String value)
			throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No DailyReportContent found by " + field + "(" + value + ").");
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
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
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
		record.setCreationDate(model.getCreationDate());
		record.setKeyReportId(model.getKeyReportId());
		record.setStartId(model.getStartId());
		record.setEndId(model.getEndId());
		record.setCapacity(model.getCapacity());
		return record;
	}
}
