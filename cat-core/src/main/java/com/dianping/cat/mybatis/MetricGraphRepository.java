package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.MetricGraphDO;
import com.dianping.cat.mybatis.mapper.MetricGraphMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("metricGraphRepository")
public class MetricGraphRepository extends SpringBackedRepositorySupport<MetricGraphMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetricGraphRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/MetricGraphMapper.xml";

	public MetricGraphRepository() {
		super(MetricGraphMapper.class, MAPPER_RESOURCE,
				"MetricGraphRepository is using Spring managed MetricGraphMapper.");
	}

	public MetricGraphDO createLocal() {
		return new MetricGraphDO();
	}

	public int deleteByPK(MetricGraphDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for MetricGraph.", e);
		}
	}

	public int deleteBeforeDate(MetricGraphDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteBeforeDate(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteBeforeDate for MetricGraph.", e);
		}
	}

	public MetricGraphDO findByPK(int id) {
		return findByPK((long) id);
	}

	public MetricGraphDO findByPK(long id) {
		MetricGraphMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for MetricGraph.", e);
		}
	}

	public MetricGraphDO findByGrapId(long graphId) {
		MetricGraphMapper mapper = springMapper(LOGGER);
		MetricGraphDO record = new MetricGraphDO();

		record.setGraphId(graphId);
		try {
			MetricGraphDO result = mapper.findByGrapId(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByGrapId", record.toString());
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByGrapId for MetricGraph.", e);
		}
	}

	public MetricGraphDO findLast(int number) {
		MetricGraphMapper mapper = springMapper(LOGGER);
		MetricGraphDO record = new MetricGraphDO();

		record.setNumber(number);
		try {
			MetricGraphDO result = mapper.findLast(record).stream().findFirst().orElse(null);

			return requireFound(result, "findLast", record.toString());
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findLast for MetricGraph.", e);
		}
	}

	public int insert(MetricGraphDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for MetricGraph.", e);
		}
	}

	public int updateByPK(MetricGraphDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for MetricGraph.", e);
		}
	}

	private MetricGraphDO requireFound(MetricGraphDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No MetricGraph found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
