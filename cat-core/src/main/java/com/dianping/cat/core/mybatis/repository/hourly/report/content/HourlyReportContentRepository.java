package com.dianping.cat.core.mybatis.repository.hourly.report.content;

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

import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.mybatis.generated.hourly.report.content.dao.HourlyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.hourly.report.content.dao.data.HourlyReportContentDO;

public class HourlyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(HourlyReportContentRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public HourlyReportContent createLocal() {
		return new HourlyReportContent();
	}

	public int deleteByPK(HourlyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for HourlyReportContent.", e);
		}
	}

	public List<HourlyReportContent> findOverloadReport(int startId, Readset<HourlyReportContent> readset)
			throws DalException {
		HourlyReportContentMapper mapper = springMapper();
		HourlyReportContentDO record = new HourlyReportContentDO();

		record.setStartId(startId);
		try {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for HourlyReportContent.", e);
		}
	}

	public HourlyReportContent findByPK(int keyReportId, java.util.Date period, Readset<HourlyReportContent> readset)
			throws DalException {
		HourlyReportContentMapper mapper = springMapper();

		try {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for HourlyReportContent.", e);
		}
	}

	public int insert(HourlyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		} catch (Exception e) {
			throw new DalException("Error when executing insert for HourlyReportContent.", e);
		}
	}

	public int updateByPK(HourlyReportContent proto, Updateset<HourlyReportContent> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for HourlyReportContent.", e);
		}
	}

	private HourlyReportContentMapper springMapper() {
		if (m_sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for HourlyReportContentMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("HourlyReportContentRepository is using Spring managed HourlyReportContentMapper.");
		}
		return m_sqlSessionTemplate.getMapper(HourlyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for HourlyReportContentMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private HourlyReportContent requireFound(HourlyReportContentDO record, String field, String value)
			throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No HourlyReportContent found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private HourlyReportContent toModel(HourlyReportContentDO record) {
		HourlyReportContent model = new HourlyReportContent();

		if (record.getReportId() != null) {
			model.setReportId(record.getReportId());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getPeriod() != null) {
			model.setPeriod(record.getPeriod());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getContentLength() != null) {
			model.setContentLength(record.getContentLength().longValue());
		}
		model.afterLoad();
		return model;
	}

	private HourlyReportContentDO toRecord(HourlyReportContent model) {
		HourlyReportContentDO record = new HourlyReportContentDO();

		record.setReportId(model.getReportId());
		record.setContent(model.getContent());
		record.setPeriod(model.getPeriod());
		record.setCreationDate(model.getCreationDate());
		record.setKeyReportId(model.getKeyReportId());
		record.setStartId(model.getStartId());
		record.setCapacity(model.getCapacity());
		return record;
	}
}
