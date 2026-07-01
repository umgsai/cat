package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.mapper.UserDefineRuleMapper;
import com.dianping.cat.mybatis.data.UserDefineRuleDO;
import com.dianping.cat.mybatis.SpringBackedRepositorySupport;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("userDefineRuleRepository")
public class UserDefineRuleRepository extends SpringBackedRepositorySupport<UserDefineRuleMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDefineRuleRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/UserDefineRuleMapper.xml";

	public UserDefineRuleRepository() {
		super(UserDefineRuleMapper.class, MAPPER_RESOURCE,
				"UserDefineRuleRepository is using Spring managed UserDefineRuleMapper.");
	}

	public UserDefineRuleDO createLocal() {
		return new UserDefineRuleDO();
	}

	public int deleteByPK(UserDefineRuleDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for UserDefineRule.", e);
		}
	}

	public UserDefineRuleDO findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public UserDefineRuleDO findByPK(long keyId) {
		UserDefineRuleMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for UserDefineRule.", e);
		}
	}

	public UserDefineRuleDO findMaxId() {
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

	public int insert(UserDefineRuleDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));

			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for UserDefineRule.", e);
		}
	}

	public int updateByPK(UserDefineRuleDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for UserDefineRule.", e);
		}
	}

	private UserDefineRuleDO requireFound(UserDefineRuleDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No UserDefineRule found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
