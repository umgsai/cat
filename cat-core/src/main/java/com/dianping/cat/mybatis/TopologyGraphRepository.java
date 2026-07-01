package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.TopologyGraphDO;
import com.dianping.cat.mybatis.mapper.TopologyGraphMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("topologyGraphRepository")
public class TopologyGraphRepository extends SpringBackedRepositorySupport<TopologyGraphMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TopologyGraphRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/TopologyGraphMapper.xml";

	public TopologyGraphRepository() {
		super(TopologyGraphMapper.class, MAPPER_RESOURCE,
				"TopologyGraphRepository is using Spring managed TopologyGraphMapper.");
	}

	public TopologyGraphDO createLocal() {
		return new TopologyGraphDO();
	}

	public int deleteByPK(TopologyGraphDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for TopologyGraph.", e);
		}
	}

	public TopologyGraphDO findByPK(int id) {
		return findByPK((long) id);
	}

	public TopologyGraphDO findByPK(long id) {
		TopologyGraphMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for TopologyGraph.", e);
		}
	}

	public TopologyGraphDO findByPeriod(java.util.Date period) {
		TopologyGraphMapper mapper = springMapper(LOGGER);
		TopologyGraphDO record = new TopologyGraphDO();

		record.setPeriod(period);
		try {
			TopologyGraphDO result = mapper.findByPeriod(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByPeriod", record.toString());
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPeriod for TopologyGraph.", e);
		}
	}

	public int insert(TopologyGraphDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for TopologyGraph.", e);
		}
	}

	public int updateByPK(TopologyGraphDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for TopologyGraph.", e);
		}
	}

	private TopologyGraphDO requireFound(TopologyGraphDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No TopologyGraph found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
