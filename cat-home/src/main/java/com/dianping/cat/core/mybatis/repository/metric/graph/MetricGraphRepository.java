package com.dianping.cat.core.mybatis.repository.metric.graph;

import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.metric.graph.dao.MetricGraphMapper;
import com.dianping.cat.core.mybatis.generated.metric.graph.dao.data.MetricGraphDO;
import com.dianping.cat.home.dal.report.MetricGraph;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class MetricGraphRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/MetricGraphMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return MetricGraphMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public MetricGraph createLocal() {
		return new MetricGraph();
	}

	public int deleteByPK(MetricGraph proto) throws DalException {
		try (SqlSession session = openSession()) {
			MetricGraphMapper mapper = session.getMapper(MetricGraphMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for MetricGraph.", e);
		}
	}

	public int deleteBeforeDate(MetricGraph proto) throws DalException {
		try (SqlSession session = openSession()) {
			MetricGraphMapper mapper = session.getMapper(MetricGraphMapper.class);
			int count = mapper.deleteBeforeDate(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteBeforeDate for MetricGraph.", e);
		}
	}

	public MetricGraph findByPK(int keyId, Readset<MetricGraph> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricGraphMapper mapper = session.getMapper(MetricGraphMapper.class);
			MetricGraphDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for MetricGraph.", e);
		}
	}

	public MetricGraph findByGrapId(long graphId, Readset<MetricGraph> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricGraphMapper mapper = session.getMapper(MetricGraphMapper.class);
			MetricGraphDO record = new MetricGraphDO();
			record.setGraphId(graphId);
			MetricGraphDO result = mapper.findByGrapId(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByGrapId", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByGrapId for MetricGraph.", e);
		}
	}

	public MetricGraph findLast(int number, Readset<MetricGraph> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricGraphMapper mapper = session.getMapper(MetricGraphMapper.class);
			MetricGraphDO record = new MetricGraphDO();
			record.setNumber(number);
			MetricGraphDO result = mapper.findLast(record).stream().findFirst().orElse(null);
			return requireFound(result, "findLast", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findLast for MetricGraph.", e);
		}
	}

	public int insert(MetricGraph proto) throws DalException {
		try (SqlSession session = openSession()) {
			MetricGraphMapper mapper = session.getMapper(MetricGraphMapper.class);
			MetricGraphDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for MetricGraph.", e);
		}
	}

	public int updateByPK(MetricGraph proto, Updateset<MetricGraph> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricGraphMapper mapper = session.getMapper(MetricGraphMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
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
