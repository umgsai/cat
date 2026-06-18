package com.dianping.cat.mybatis.repository.alert;

import com.dianping.cat.alarm.Alert;
import com.dianping.cat.mybatis.alert.dao.AlertMapper;
import com.dianping.cat.mybatis.alert.dao.data.AlertDO;
import com.dianping.cat.mybatis.SpringBackedRepositorySupport;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

public class AlertRepository extends SpringBackedRepositorySupport<AlertMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlertMapper.xml";

	public AlertRepository() {
		super(AlertMapper.class, MAPPER_RESOURCE, "AlertRepository is using Spring managed AlertMapper.");
	}

	public Alert createLocal() {
		return new Alert();
	}

	public int deleteByPK(Alert proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for Alert.", e);
		}
	}

	public List<Alert> queryAlertsByTimeDomain(java.util.Date startTime, java.util.Date endTime, String domain) {
		AlertMapper mapper = springMapper(LOGGER);
		AlertDO record = new AlertDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setDomain(domain);
		try {
			return mapper.queryAlertsByTimeDomain(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing queryAlertsByTimeDomain for Alert.", e);
		}
	}

	public List<Alert> queryAlertsByTimeDomainCategories(java.util.Date startTime, java.util.Date endTime, String domain, String[] categories) {
		AlertMapper mapper = springMapper(LOGGER);
		AlertDO record = new AlertDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setDomain(domain);
		record.setCategories(categories);
		try {
			return mapper.queryAlertsByTimeDomainCategories(record).stream().map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing queryAlertsByTimeDomainCategories for Alert.", e);
		}
	}

	public List<Alert> queryAlertsByTimeCategoryDomain(java.util.Date startTime, java.util.Date endTime, String category, String domain) {
		AlertMapper mapper = springMapper(LOGGER);
		AlertDO record = new AlertDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setCategory(category);
		record.setDomain(domain);
		try {
			return mapper.queryAlertsByTimeCategoryDomain(record).stream().map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing queryAlertsByTimeCategoryDomain for Alert.", e);
		}
	}

	public List<Alert> queryAlertsByTimeCategory(java.util.Date startTime, java.util.Date endTime, String category) {
		AlertMapper mapper = springMapper(LOGGER);
		AlertDO record = new AlertDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setCategory(category);
		try {
			return mapper.queryAlertsByTimeCategory(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing queryAlertsByTimeCategory for Alert.", e);
		}
	}

	public Alert findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public Alert findByPK(long keyId) {
		AlertMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for Alert.", e);
		}
	}

	public int insert(Alert proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			AlertDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for Alert.", e);
		}
	}

	public int updateByPK(Alert proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for Alert.", e);
		}
	}

	private Alert requireFound(AlertDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Alert found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private Alert toModel(AlertDO record) {
		Alert model = new Alert();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getAlertTime() != null) {
			model.setAlertTime(record.getAlertTime());
		}
		if (record.getCategory() != null) {
			model.setCategory(record.getCategory());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getMetric() != null) {
			model.setMetric(record.getMetric());
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

	private AlertDO toRecord(Alert model) {
		AlertDO record = new AlertDO();

		record.setId(model.getId());
		record.setDomain(model.getDomain());
		record.setAlertTime(model.getAlertTime());
		record.setCategory(model.getCategory());
		record.setType(model.getType());
		record.setContent(model.getContent());
		record.setMetric(model.getMetric());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		record.setStartTime(model.getStartTime());
		record.setEndTime(model.getEndTime());
		record.setCategories(model.getCategories());
		return record;
	}
}
