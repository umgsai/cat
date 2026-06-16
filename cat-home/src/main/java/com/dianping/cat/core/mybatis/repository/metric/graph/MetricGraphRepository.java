package com.dianping.cat.core.mybatis.repository.metric.graph;

import com.dianping.cat.core.mybatis.repository.SpringBackedRepositorySupport;
import com.dianping.cat.core.mybatis.generated.metric.graph.dao.MetricGraphMapper;
import com.dianping.cat.core.mybatis.generated.metric.graph.dao.data.MetricGraphDO;
import com.dianping.cat.home.dal.report.MetricGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import com.dianping.cat.core.dal.jdbc.DalException;
import com.dianping.cat.core.dal.jdbc.DalNotFoundException;

public class MetricGraphRepository extends SpringBackedRepositorySupport<MetricGraphMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetricGraphRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/MetricGraphMapper.xml";

	public MetricGraphRepository() {
		super(MetricGraphMapper.class, MAPPER_RESOURCE,
				"MetricGraphRepository is using Spring managed MetricGraphMapper.");
	}

	public MetricGraph createLocal() {
		return new MetricGraph();
	}

	public int deleteByPK(MetricGraph proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for MetricGraph.", e);
		}
	}

	public int deleteBeforeDate(MetricGraph proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteBeforeDate(toRecord(proto)));
		} catch (Exception e) {
			throw new DalException("Error when executing deleteBeforeDate for MetricGraph.", e);
		}
	}

	public MetricGraph findByPK(int keyId, Object readset) throws DalException {
		MetricGraphMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for MetricGraph.", e);
		}
	}

	public MetricGraph findByGrapId(long graphId, Object readset) throws DalException {
		MetricGraphMapper mapper = springMapper(LOGGER);
		MetricGraphDO record = new MetricGraphDO();

		record.setGraphId(graphId);
		try {
			MetricGraphDO result = mapper.findByGrapId(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByGrapId", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByGrapId for MetricGraph.", e);
		}
	}

	public MetricGraph findLast(int number, Object readset) throws DalException {
		MetricGraphMapper mapper = springMapper(LOGGER);
		MetricGraphDO record = new MetricGraphDO();

		record.setNumber(number);
		try {
			MetricGraphDO result = mapper.findLast(record).stream().findFirst().orElse(null);

			return requireFound(result, "findLast", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findLast for MetricGraph.", e);
		}
	}

	public int insert(MetricGraph proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			MetricGraphDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for MetricGraph.", e);
		}
	}

	public int updateByPK(MetricGraph proto, Object updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for MetricGraph.", e);
		}
	}

	private MetricGraph requireFound(MetricGraphDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No MetricGraph found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private MetricGraph toModel(MetricGraphDO record) {
		MetricGraph model = new MetricGraph();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getGraphId() != null) {
			model.setGraphId(record.getGraphId());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getUpdatetime() != null) {
			model.setUpdatetime(record.getUpdatetime());
		}
		model.afterLoad();
		return model;
	}

	private MetricGraphDO toRecord(MetricGraph model) {
		MetricGraphDO record = new MetricGraphDO();

		record.setId(model.getId());
		record.setGraphId(model.getGraphId());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setUpdatetime(model.getUpdatetime());
		record.setKeyId(model.getKeyId());
		record.setNumber(model.getNumber());
		return record;
	}
}
