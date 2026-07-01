package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.MetricScreenDO;
import com.dianping.cat.mybatis.mapper.MetricScreenMapper;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("metricScreenRepository")
public class MetricScreenRepository extends SpringBackedRepositorySupport<MetricScreenMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetricScreenRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/MetricScreenMapper.xml";

	public MetricScreenRepository() {
		super(MetricScreenMapper.class, MAPPER_RESOURCE,
				"MetricScreenRepository is using Spring managed MetricScreenMapper.");
	}

	public MetricScreenDO createLocal() {
		return new MetricScreenDO();
	}

	public int deleteByPK(MetricScreenDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for MetricScreen.", e);
		}
	}

	public int deleteByName(MetricScreenDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByName(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByName for MetricScreen.", e);
		}
	}

	public int deleteByNameGraph(MetricScreenDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByNameGraph(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByNameGraph for MetricScreen.", e);
		}
	}

	public List<MetricScreenDO> findAll() {
		MetricScreenMapper mapper = springMapper(LOGGER);
		MetricScreenDO record = new MetricScreenDO();

		try {
			return mapper.findAll(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findAll for MetricScreen.", e);
		}
	}

	public List<MetricScreenDO> findByName(String name) {
		MetricScreenMapper mapper = springMapper(LOGGER);
		MetricScreenDO record = new MetricScreenDO();

		record.setName(name);
		try {
			return mapper.findByName(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByName for MetricScreen.", e);
		}
	}

	public MetricScreenDO findByPK(int id) {
		return findByPK((long) id);
	}

	public MetricScreenDO findByPK(long id) {
		MetricScreenMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for MetricScreen.", e);
		}
	}

	public MetricScreenDO findByNameGraph(String name, String graphName) {
		MetricScreenMapper mapper = springMapper(LOGGER);
		MetricScreenDO record = new MetricScreenDO();

		record.setName(name);
		record.setGraphName(graphName);
		try {
			MetricScreenDO result = mapper.findByNameGraph(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByNameGraph", record.toString());
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByNameGraph for MetricScreen.", e);
		}
	}

	public int insert(MetricScreenDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for MetricScreen.", e);
		}
	}

	public int insertOrUpdateByNameGraph(MetricScreenDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insertOrUpdateByNameGraph(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insertOrUpdateByNameGraph for MetricScreen.", e);
		}
	}

	public int updateByPK(MetricScreenDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for MetricScreen.", e);
		}
	}

	private MetricScreenDO requireFound(MetricScreenDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No MetricScreen found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
