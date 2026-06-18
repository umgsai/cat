package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.AlertSummaryDO;
import com.dianping.cat.mybatis.mapper.AlertSummaryMapper;
import com.dianping.cat.home.dal.report.AlertSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

public class AlertSummaryRepository extends SpringBackedRepositorySupport<AlertSummaryMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertSummaryRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlertSummaryMapper.xml";

	public AlertSummaryRepository() {
		super(AlertSummaryMapper.class, MAPPER_RESOURCE,
				"AlertSummaryRepository is using Spring managed AlertSummaryMapper.");
	}

	public AlertSummary createLocal() {
		return new AlertSummary();
	}

	public int deleteByPK(AlertSummary proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for AlertSummary.", e);
		}
	}

	public AlertSummary findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public AlertSummary findByPK(long keyId) {
		AlertSummaryMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for AlertSummary.", e);
		}
	}

	public int insert(AlertSummary proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			AlertSummaryDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for AlertSummary.", e);
		}
	}

	public int updateByPK(AlertSummary proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for AlertSummary.", e);
		}
	}

	private AlertSummary requireFound(AlertSummaryDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No AlertSummary found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private AlertSummary toModel(AlertSummaryDO record) {
		AlertSummary model = new AlertSummary();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getAlertTime() != null) {
			model.setAlertTime(record.getAlertTime());
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
		model.afterLoad();
		return model;
	}

	private AlertSummaryDO toRecord(AlertSummary model) {
		AlertSummaryDO record = new AlertSummaryDO();

		record.setId(model.getId());
		record.setDomain(model.getDomain());
		record.setAlertTime(model.getAlertTime());
		record.setContent(model.getContent());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
