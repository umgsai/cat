package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.AlertSummaryDO;
import com.dianping.cat.mybatis.mapper.AlertSummaryMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("alertSummaryRepository")
public class AlertSummaryRepository extends SpringBackedRepositorySupport<AlertSummaryMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertSummaryRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlertSummaryMapper.xml";

	public AlertSummaryRepository() {
		super(AlertSummaryMapper.class, MAPPER_RESOURCE,
				"AlertSummaryRepository is using Spring managed AlertSummaryMapper.");
	}

	public AlertSummaryDO createLocal() {
		return new AlertSummaryDO();
	}

	public int deleteByPK(AlertSummaryDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for AlertSummary.", e);
		}
	}

	public AlertSummaryDO findByPK(int id) {
		return findByPK((long) id);
	}

	public AlertSummaryDO findByPK(long id) {
		AlertSummaryMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for AlertSummary.", e);
		}
	}

	public int insert(AlertSummaryDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for AlertSummary.", e);
		}
	}

	public int updateByPK(AlertSummaryDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for AlertSummary.", e);
		}
	}

	private AlertSummaryDO requireFound(AlertSummaryDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No AlertSummary found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
