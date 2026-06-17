package com.dianping.cat.core.mybatis.repository.user.define.rule;

import com.dianping.cat.alarm.UserDefineRule;
import com.dianping.cat.core.mybatis.user.define.rule.dao.UserDefineRuleMapper;
import com.dianping.cat.core.mybatis.user.define.rule.dao.data.UserDefineRuleDO;
import com.dianping.cat.core.mybatis.repository.SpringBackedRepositorySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

public class UserDefineRuleRepository extends SpringBackedRepositorySupport<UserDefineRuleMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDefineRuleRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/UserDefineRuleMapper.xml";

	public UserDefineRuleRepository() {
		super(UserDefineRuleMapper.class, MAPPER_RESOURCE,
				"UserDefineRuleRepository is using Spring managed UserDefineRuleMapper.");
	}

	public UserDefineRule createLocal() {
		return new UserDefineRule();
	}

	public int deleteByPK(UserDefineRule proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for UserDefineRule.", e);
		}
	}

	public UserDefineRule findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public UserDefineRule findByPK(long keyId) {
		UserDefineRuleMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for UserDefineRule.", e);
		}
	}

	public UserDefineRule findMaxId() {
		UserDefineRuleMapper mapper = springMapper(LOGGER);
		UserDefineRuleDO record = new UserDefineRuleDO();

		try {
			UserDefineRuleDO result = mapper.findMaxId(record).stream().findFirst().orElse(null);

			return requireFound(result, "findMaxId", record.toString());
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findMaxId for UserDefineRule.", e);
		}
	}

	public int insert(UserDefineRule proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			UserDefineRuleDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for UserDefineRule.", e);
		}
	}

	public int updateByPK(UserDefineRule proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for UserDefineRule.", e);
		}
	}

	private UserDefineRule requireFound(UserDefineRuleDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No UserDefineRule found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private UserDefineRule toModel(UserDefineRuleDO record) {
		UserDefineRule model = new UserDefineRule();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getUpdateTime() != null) {
			model.setUpdateTime(record.getUpdateTime());
		}
		if (record.getMaxId() != null) {
			model.setMaxId(record.getMaxId());
		}
		model.afterLoad();
		return model;
	}

	private UserDefineRuleDO toRecord(UserDefineRule model) {
		UserDefineRuleDO record = new UserDefineRuleDO();

		record.setId(model.getId());
		record.setContent(model.getContent());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
