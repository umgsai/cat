package com.dianping.cat.mybatis.repository.alert;

import com.dianping.cat.mybatis.alert.dao.AlertMapper;
import com.dianping.cat.mybatis.alert.dao.data.AlertDO;
import com.dianping.cat.mybatis.SpringBackedRepositorySupport;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("alertRepository")
public class AlertRepository extends SpringBackedRepositorySupport<AlertMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlertMapper.xml";

	public AlertRepository() {
		super(AlertMapper.class, MAPPER_RESOURCE, "AlertRepository is using Spring managed AlertMapper.");
	}

	public AlertDO createLocal() {
		return new AlertDO();
	}

	public int deleteByPK(AlertDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for Alert.", e);
		}
	}

	public List<AlertDO> queryAlertsByTimeDomain(java.util.Date startTime, java.util.Date endTime, String domain) {
		AlertMapper mapper = springMapper(LOGGER);
		AlertDO record = new AlertDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setDomain(domain);
		try {
			return mapper.queryAlertsByTimeDomain(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing queryAlertsByTimeDomain for Alert.", e);
		}
	}

	public List<AlertDO> queryAlertsByTimeDomainCategories(java.util.Date startTime, java.util.Date endTime, String domain, String[] categories) {
		AlertMapper mapper = springMapper(LOGGER);
		AlertDO record = new AlertDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setDomain(domain);
		record.setCategories(categories);
		try {
			return mapper.queryAlertsByTimeDomainCategories(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing queryAlertsByTimeDomainCategories for Alert.", e);
		}
	}

	public List<AlertDO> queryAlertsByTimeCategoryDomain(java.util.Date startTime, java.util.Date endTime, String category, String domain) {
		AlertMapper mapper = springMapper(LOGGER);
		AlertDO record = new AlertDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setCategory(category);
		record.setDomain(domain);
		try {
			return mapper.queryAlertsByTimeCategoryDomain(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing queryAlertsByTimeCategoryDomain for Alert.", e);
		}
	}

	public List<AlertDO> queryAlertsByTimeCategory(java.util.Date startTime, java.util.Date endTime, String category) {
		AlertMapper mapper = springMapper(LOGGER);
		AlertDO record = new AlertDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setCategory(category);
		try {
			return mapper.queryAlertsByTimeCategory(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing queryAlertsByTimeCategory for Alert.", e);
		}
	}

	public AlertDO findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public AlertDO findByPK(long keyId) {
		AlertMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for Alert.", e);
		}
	}

	public int insert(AlertDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));

			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for Alert.", e);
		}
	}

	public int updateByPK(AlertDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for Alert.", e);
		}
	}

	private AlertDO requireFound(AlertDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Alert found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
