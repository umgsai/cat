package com.dianping.cat.core.mybatis.repository.metric.screen;

import com.dianping.cat.core.mybatis.repository.SpringBackedRepositorySupport;
import com.dianping.cat.core.mybatis.metric.screen.dao.MetricScreenMapper;
import com.dianping.cat.core.mybatis.metric.screen.dao.data.MetricScreenDO;
import com.dianping.cat.home.dal.report.MetricScreen;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

public class MetricScreenRepository extends SpringBackedRepositorySupport<MetricScreenMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetricScreenRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/MetricScreenMapper.xml";

	public MetricScreenRepository() {
		super(MetricScreenMapper.class, MAPPER_RESOURCE,
				"MetricScreenRepository is using Spring managed MetricScreenMapper.");
	}

	public MetricScreen createLocal() {
		return new MetricScreen();
	}

	public int deleteByPK(MetricScreen proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for MetricScreen.", e);
		}
	}

	public int deleteByName(MetricScreen proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByName(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByName for MetricScreen.", e);
		}
	}

	public int deleteByNameGraph(MetricScreen proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByNameGraph(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByNameGraph for MetricScreen.", e);
		}
	}

	public List<MetricScreen> findAll() {
		MetricScreenMapper mapper = springMapper(LOGGER);
		MetricScreenDO record = new MetricScreenDO();

		try {
			return mapper.findAll(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findAll for MetricScreen.", e);
		}
	}

	public List<MetricScreen> findByName(String name) {
		MetricScreenMapper mapper = springMapper(LOGGER);
		MetricScreenDO record = new MetricScreenDO();

		record.setName(name);
		try {
			return mapper.findByName(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByName for MetricScreen.", e);
		}
	}

	public MetricScreen findByPK(int keyId) {
		MetricScreenMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for MetricScreen.", e);
		}
	}

	public MetricScreen findByNameGraph(String name, String graphName) {
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

	public int insert(MetricScreen proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			MetricScreenDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for MetricScreen.", e);
		}
	}

	public int insertOrUpdateByNameGraph(MetricScreen proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insertOrUpdateByNameGraph(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insertOrUpdateByNameGraph for MetricScreen.", e);
		}
	}

	public int updateByPK(MetricScreen proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for MetricScreen.", e);
		}
	}

	private MetricScreen requireFound(MetricScreenDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No MetricScreen found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private MetricScreen toModel(MetricScreenDO record) {
		MetricScreen model = new MetricScreen();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getName() != null) {
			model.setName(record.getName());
		}
		if (record.getGraphName() != null) {
			model.setGraphName(record.getGraphName());
		}
		if (record.getView() != null) {
			model.setView(record.getView());
		}
		if (record.getEndPoints() != null) {
			model.setEndPoints(record.getEndPoints());
		}
		if (record.getMeasurements() != null) {
			model.setMeasurements(record.getMeasurements());
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

	private MetricScreenDO toRecord(MetricScreen model) {
		MetricScreenDO record = new MetricScreenDO();

		record.setId(model.getId());
		record.setName(model.getName());
		record.setGraphName(model.getGraphName());
		record.setView(model.getView());
		record.setEndPoints(model.getEndPoints());
		record.setMeasurements(model.getMeasurements());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setUpdatetime(model.getUpdatetime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
