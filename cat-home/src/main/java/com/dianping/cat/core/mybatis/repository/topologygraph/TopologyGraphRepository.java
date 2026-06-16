package com.dianping.cat.core.mybatis.repository.topologygraph;

import com.dianping.cat.core.mybatis.repository.SpringBackedRepositorySupport;
import com.dianping.cat.core.mybatis.generated.topologygraph.dao.TopologyGraphMapper;
import com.dianping.cat.core.mybatis.generated.topologygraph.dao.data.TopologyGraphDO;
import com.dianping.cat.home.dal.report.TopologyGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class TopologyGraphRepository extends SpringBackedRepositorySupport<TopologyGraphMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TopologyGraphRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/TopologyGraphMapper.xml";

	public TopologyGraphRepository() {
		super(TopologyGraphMapper.class, MAPPER_RESOURCE,
				"TopologyGraphRepository is using Spring managed TopologyGraphMapper.");
	}

	public TopologyGraph createLocal() {
		return new TopologyGraph();
	}

	public int deleteByPK(TopologyGraph proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for TopologyGraph.", e);
		}
	}

	public TopologyGraph findByPK(int keyId, Readset<TopologyGraph> readset) throws DalException {
		TopologyGraphMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for TopologyGraph.", e);
		}
	}

	public TopologyGraph findByPeriod(java.util.Date period, Readset<TopologyGraph> readset) throws DalException {
		TopologyGraphMapper mapper = springMapper(LOGGER);
		TopologyGraphDO record = new TopologyGraphDO();

		record.setPeriod(period);
		try {
			TopologyGraphDO result = mapper.findByPeriod(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByPeriod", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPeriod for TopologyGraph.", e);
		}
	}

	public int insert(TopologyGraph proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			TopologyGraphDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for TopologyGraph.", e);
		}
	}

	public int updateByPK(TopologyGraph proto, Updateset<TopologyGraph> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for TopologyGraph.", e);
		}
	}

	private TopologyGraph requireFound(TopologyGraphDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No TopologyGraph found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private TopologyGraph toModel(TopologyGraphDO record) {
		TopologyGraph model = new TopologyGraph();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getIp() != null) {
			model.setIp(record.getIp());
		}
		if (record.getPeriod() != null) {
			model.setPeriod(record.getPeriod());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
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

	private TopologyGraphDO toRecord(TopologyGraph model) {
		TopologyGraphDO record = new TopologyGraphDO();

		record.setId(model.getId());
		record.setIp(model.getIp());
		record.setPeriod(model.getPeriod());
		record.setType(model.getType());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
