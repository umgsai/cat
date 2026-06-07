package com.dianping.cat.core.mybatis.repository.metric.screen;

import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.metric.screen.dao.MetricScreenMapper;
import com.dianping.cat.core.mybatis.generated.metric.screen.dao.data.MetricScreenDO;
import com.dianping.cat.home.dal.report.MetricScreen;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class MetricScreenRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/MetricScreenMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return MetricScreenMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public MetricScreen createLocal() {
		return new MetricScreen();
	}

	public int deleteByPK(MetricScreen proto) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for MetricScreen.", e);
		}
	}

	public int deleteByName(MetricScreen proto) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			int count = mapper.deleteByName(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByName for MetricScreen.", e);
		}
	}

	public int deleteByNameGraph(MetricScreen proto) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			int count = mapper.deleteByNameGraph(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByNameGraph for MetricScreen.", e);
		}
	}

	public List<MetricScreen> findAll(Readset<MetricScreen> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			MetricScreenDO record = new MetricScreenDO();
			return mapper.findAll(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findAll for MetricScreen.", e);
		}
	}

	public List<MetricScreen> findByName(String name, Readset<MetricScreen> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			MetricScreenDO record = new MetricScreenDO();
			record.setName(name);
			return mapper.findByName(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByName for MetricScreen.", e);
		}
	}

	public MetricScreen findByPK(int keyId, Readset<MetricScreen> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			MetricScreenDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for MetricScreen.", e);
		}
	}

	public MetricScreen findByNameGraph(String name, String graphName, Readset<MetricScreen> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			MetricScreenDO record = new MetricScreenDO();
			record.setName(name);
			record.setGraphName(graphName);
			MetricScreenDO result = mapper.findByNameGraph(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByNameGraph", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByNameGraph for MetricScreen.", e);
		}
	}

	public int insert(MetricScreen proto) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			MetricScreenDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for MetricScreen.", e);
		}
	}

	public int insertOrUpdateByNameGraph(MetricScreen proto) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			int count = mapper.insertOrUpdateByNameGraph(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insertOrUpdateByNameGraph for MetricScreen.", e);
		}
	}

	public int updateByPK(MetricScreen proto, Updateset<MetricScreen> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			MetricScreenMapper mapper = session.getMapper(MetricScreenMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for MetricScreen.", e);
		}
	}

	private MetricScreen requireFound(MetricScreenDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No MetricScreen found by " + field + "(" + value + ").");
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
