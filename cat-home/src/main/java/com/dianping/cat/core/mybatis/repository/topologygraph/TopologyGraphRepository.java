package com.dianping.cat.core.mybatis.repository.topologygraph;

import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.topologygraph.dao.TopologyGraphMapper;
import com.dianping.cat.core.mybatis.generated.topologygraph.dao.data.TopologyGraphDO;
import com.dianping.cat.home.dal.report.TopologyGraph;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class TopologyGraphRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/TopologyGraphMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return TopologyGraphMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public TopologyGraph createLocal() {
		return new TopologyGraph();
	}

	public int deleteByPK(TopologyGraph proto) throws DalException {
		try (SqlSession session = openSession()) {
			TopologyGraphMapper mapper = session.getMapper(TopologyGraphMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for TopologyGraph.", e);
		}
	}

	public TopologyGraph findByPK(int keyId, Readset<TopologyGraph> readset) throws DalException {
		try (SqlSession session = openSession()) {
			TopologyGraphMapper mapper = session.getMapper(TopologyGraphMapper.class);
			TopologyGraphDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for TopologyGraph.", e);
		}
	}

	public TopologyGraph findByPeriod(java.util.Date period, Readset<TopologyGraph> readset) throws DalException {
		try (SqlSession session = openSession()) {
			TopologyGraphMapper mapper = session.getMapper(TopologyGraphMapper.class);
			TopologyGraphDO record = new TopologyGraphDO();
			record.setPeriod(period);
			TopologyGraphDO result = mapper.findByPeriod(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByPeriod", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPeriod for TopologyGraph.", e);
		}
	}

	public int insert(TopologyGraph proto) throws DalException {
		try (SqlSession session = openSession()) {
			TopologyGraphMapper mapper = session.getMapper(TopologyGraphMapper.class);
			TopologyGraphDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for TopologyGraph.", e);
		}
	}

	public int updateByPK(TopologyGraph proto, Updateset<TopologyGraph> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			TopologyGraphMapper mapper = session.getMapper(TopologyGraphMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
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
