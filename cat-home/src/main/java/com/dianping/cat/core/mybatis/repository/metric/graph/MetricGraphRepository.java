package com.dianping.cat.core.mybatis.repository.metric.graph;

import com.dianping.cat.core.mybatis.repository.SpringBackedRepositorySupport;
import com.dianping.cat.core.mybatis.generated.metric.graph.dao.MetricGraphMapper;
import com.dianping.cat.core.mybatis.generated.metric.graph.dao.data.MetricGraphDO;
import com.dianping.cat.home.dal.report.MetricGraph;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

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

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(MetricGraphMapper.class).deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for MetricGraph.", e);
		}
	}

	public int deleteBeforeDate(MetricGraph proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteBeforeDate(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(MetricGraphMapper.class).deleteBeforeDate(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteBeforeDate for MetricGraph.", e);
		}
	}

	public MetricGraph findByPK(int keyId, Readset<MetricGraph> readset) throws DalException {
		MetricGraphMapper mapper = springMapper(LOGGER);

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		}

		try (SqlSession session = openSession()) {
			MetricGraphDO record = session.getMapper(MetricGraphMapper.class).findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for MetricGraph.", e);
		}
	}

	public MetricGraph findByGrapId(long graphId, Readset<MetricGraph> readset) throws DalException {
		MetricGraphMapper mapper = springMapper(LOGGER);
		MetricGraphDO record = new MetricGraphDO();

		record.setGraphId(graphId);
		if (mapper != null) {
			MetricGraphDO result = mapper.findByGrapId(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByGrapId", record.toString());
		}

		try (SqlSession session = openSession()) {
			MetricGraphDO result = session.getMapper(MetricGraphMapper.class).findByGrapId(record).stream()
					.findFirst()
					.orElse(null);
			return requireFound(result, "findByGrapId", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByGrapId for MetricGraph.", e);
		}
	}

	public MetricGraph findLast(int number, Readset<MetricGraph> readset) throws DalException {
		MetricGraphMapper mapper = springMapper(LOGGER);
		MetricGraphDO record = new MetricGraphDO();

		record.setNumber(number);
		if (mapper != null) {
			MetricGraphDO result = mapper.findLast(record).stream().findFirst().orElse(null);

			return requireFound(result, "findLast", record.toString());
		}

		try (SqlSession session = openSession()) {
			MetricGraphDO result = session.getMapper(MetricGraphMapper.class).findLast(record).stream().findFirst()
					.orElse(null);
			return requireFound(result, "findLast", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findLast for MetricGraph.", e);
		}
	}

	public int insert(MetricGraph proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			MetricGraphDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		}

		try (SqlSession session = openSession()) {
			MetricGraphDO record = toRecord(proto);
			int count = session.getMapper(MetricGraphMapper.class).insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for MetricGraph.", e);
		}
	}

	public int updateByPK(MetricGraph proto, Updateset<MetricGraph> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(MetricGraphMapper.class).updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
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
