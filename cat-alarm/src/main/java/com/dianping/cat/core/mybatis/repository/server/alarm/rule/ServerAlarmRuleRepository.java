package com.dianping.cat.core.mybatis.repository.server.alarm.rule;

import com.dianping.cat.alarm.ServerAlarmRule;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.server.alarm.rule.dao.ServerAlarmRuleMapper;
import com.dianping.cat.core.mybatis.generated.server.alarm.rule.dao.data.ServerAlarmRuleDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class ServerAlarmRuleRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/ServerAlarmRuleMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return ServerAlarmRuleMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public ServerAlarmRule createLocal() {
		return new ServerAlarmRule();
	}

	public int deleteByPK(ServerAlarmRule proto) throws DalException {
		try (SqlSession session = openSession()) {
			ServerAlarmRuleMapper mapper = session.getMapper(ServerAlarmRuleMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for ServerAlarmRule.", e);
		}
	}

	public List<ServerAlarmRule> findAll(Readset<ServerAlarmRule> readset) throws DalException {
		try (SqlSession session = openSession()) {
			ServerAlarmRuleMapper mapper = session.getMapper(ServerAlarmRuleMapper.class);
			ServerAlarmRuleDO record = new ServerAlarmRuleDO();
			return mapper.findAll(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findAll for ServerAlarmRule.", e);
		}
	}

	public ServerAlarmRule findByPK(int keyId, Readset<ServerAlarmRule> readset) throws DalException {
		try (SqlSession session = openSession()) {
			ServerAlarmRuleMapper mapper = session.getMapper(ServerAlarmRuleMapper.class);
			ServerAlarmRuleDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for ServerAlarmRule.", e);
		}
	}

	public int insert(ServerAlarmRule proto) throws DalException {
		try (SqlSession session = openSession()) {
			ServerAlarmRuleMapper mapper = session.getMapper(ServerAlarmRuleMapper.class);
			ServerAlarmRuleDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for ServerAlarmRule.", e);
		}
	}

	public int updateByPK(ServerAlarmRule proto, Updateset<ServerAlarmRule> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			ServerAlarmRuleMapper mapper = session.getMapper(ServerAlarmRuleMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for ServerAlarmRule.", e);
		}
	}

	private ServerAlarmRule requireFound(ServerAlarmRuleDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No ServerAlarmRule found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private ServerAlarmRule toModel(ServerAlarmRuleDO record) {
		ServerAlarmRule model = new ServerAlarmRule();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getCategory() != null) {
			model.setCategory(record.getCategory());
		}
		if (record.getEndPoint() != null) {
			model.setEndPoint(record.getEndPoint());
		}
		if (record.getMeasurement() != null) {
			model.setMeasurement(record.getMeasurement());
		}
		if (record.getTags() != null) {
			model.setTags(record.getTags());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
		}
		if (record.getCreator() != null) {
			model.setCreator(record.getCreator());
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

	private ServerAlarmRuleDO toRecord(ServerAlarmRule model) {
		ServerAlarmRuleDO record = new ServerAlarmRuleDO();

		record.setId(model.getId());
		record.setCategory(model.getCategory());
		record.setEndPoint(model.getEndPoint());
		record.setMeasurement(model.getMeasurement());
		record.setTags(model.getTags());
		record.setContent(model.getContent());
		record.setType(model.getType());
		record.setCreator(model.getCreator());
		record.setCreationDate(model.getCreationDate());
		record.setUpdatetime(model.getUpdatetime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
