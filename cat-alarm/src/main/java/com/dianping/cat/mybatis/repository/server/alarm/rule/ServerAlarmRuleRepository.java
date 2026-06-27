package com.dianping.cat.mybatis.repository.server.alarm.rule;

import com.dianping.cat.alarm.ServerAlarmRule;
import com.dianping.cat.mybatis.server.alarm.rule.dao.ServerAlarmRuleMapper;
import com.dianping.cat.mybatis.server.alarm.rule.dao.data.ServerAlarmRuleDO;
import com.dianping.cat.mybatis.SpringBackedRepositorySupport;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("serverAlarmRuleRepository")
public class ServerAlarmRuleRepository extends SpringBackedRepositorySupport<ServerAlarmRuleMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerAlarmRuleRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/ServerAlarmRuleMapper.xml";

	public ServerAlarmRuleRepository() {
		super(ServerAlarmRuleMapper.class, MAPPER_RESOURCE,
				"ServerAlarmRuleRepository is using Spring managed ServerAlarmRuleMapper.");
	}

	public ServerAlarmRule createLocal() {
		return new ServerAlarmRule();
	}

	public int deleteByPK(ServerAlarmRule proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for ServerAlarmRule.", e);
		}
	}

	public List<ServerAlarmRule> findAll() {
		ServerAlarmRuleMapper mapper = springMapper(LOGGER);
		ServerAlarmRuleDO record = new ServerAlarmRuleDO();

		try {
			return mapper.findAll(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findAll for ServerAlarmRule.", e);
		}
	}

	public ServerAlarmRule findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public ServerAlarmRule findByPK(long keyId) {
		ServerAlarmRuleMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for ServerAlarmRule.", e);
		}
	}

	public int insert(ServerAlarmRule proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			ServerAlarmRuleDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for ServerAlarmRule.", e);
		}
	}

	public int updateByPK(ServerAlarmRule proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for ServerAlarmRule.", e);
		}
	}

	private ServerAlarmRule requireFound(ServerAlarmRuleDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No ServerAlarmRule found by " + field + "(" + value + ").", 1);
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
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
