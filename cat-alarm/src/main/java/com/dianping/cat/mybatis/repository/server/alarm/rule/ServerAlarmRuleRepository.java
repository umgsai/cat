package com.dianping.cat.mybatis.repository.server.alarm.rule;

import com.dianping.cat.mybatis.server.alarm.rule.dao.ServerAlarmRuleMapper;
import com.dianping.cat.mybatis.server.alarm.rule.dao.data.ServerAlarmRuleDO;
import com.dianping.cat.mybatis.SpringBackedRepositorySupport;
import java.util.List;
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

	public ServerAlarmRuleDO createLocal() {
		return new ServerAlarmRuleDO();
	}

	public int deleteByPK(ServerAlarmRuleDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for ServerAlarmRule.", e);
		}
	}

	public List<ServerAlarmRuleDO> findAll() {
		ServerAlarmRuleMapper mapper = springMapper(LOGGER);
		ServerAlarmRuleDO record = new ServerAlarmRuleDO();

		try {
			return mapper.findAll(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findAll for ServerAlarmRule.", e);
		}
	}

	public ServerAlarmRuleDO findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public ServerAlarmRuleDO findByPK(long keyId) {
		ServerAlarmRuleMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for ServerAlarmRule.", e);
		}
	}

	public int insert(ServerAlarmRuleDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));

			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for ServerAlarmRule.", e);
		}
	}

	public int updateByPK(ServerAlarmRuleDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for ServerAlarmRule.", e);
		}
	}

	private ServerAlarmRuleDO requireFound(ServerAlarmRuleDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No ServerAlarmRule found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
