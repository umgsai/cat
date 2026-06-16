package com.dianping.cat.core.mybatis.repository.alert.summary;

import com.dianping.cat.core.mybatis.repository.SpringBackedRepositorySupport;
import com.dianping.cat.core.mybatis.generated.alert.summary.dao.AlertSummaryMapper;
import com.dianping.cat.core.mybatis.generated.alert.summary.dao.data.AlertSummaryDO;
import com.dianping.cat.home.dal.report.AlertSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import com.dianping.cat.core.dal.jdbc.DalException;
import com.dianping.cat.core.dal.jdbc.DalNotFoundException;

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

	public int deleteByPK(AlertSummary proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for AlertSummary.", e);
		}
	}

	public AlertSummary findByPK(int keyId, Object readset) throws DalException {
		AlertSummaryMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for AlertSummary.", e);
		}
	}

	public int insert(AlertSummary proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			AlertSummaryDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for AlertSummary.", e);
		}
	}

	public int updateByPK(AlertSummary proto, Object updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for AlertSummary.", e);
		}
	}

	private AlertSummary requireFound(AlertSummaryDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No AlertSummary found by " + field + "(" + value + ").");
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
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
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
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
