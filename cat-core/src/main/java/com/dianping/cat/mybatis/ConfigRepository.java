package com.dianping.cat.mybatis;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.mapper.ConfigMapper;
import com.dianping.cat.mybatis.data.ConfigDO;

@Component("configRepository")
public class ConfigRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public ConfigDO createLocal() {
		return new ConfigDO();
	}

	public int deleteByPK(ConfigDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteById(proto.getId()));
	}

	public List<ConfigDO> findAllConfig() {
		ConfigMapper mapper = springMapper();

		return mapper.queryAll();
	}

	public ConfigDO findByName(String name) {
		ConfigMapper mapper = springMapper();

		return requireFound(mapper.findByName(name), "name", name);
	}

	public ConfigDO findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public ConfigDO findByPK(long keyId) {
		ConfigMapper mapper = springMapper();

		return requireFound(mapper.findById(keyId), "id", String.valueOf(keyId));
	}

	public int insert(ConfigDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();
		Date now = new Date();

		if (proto.getCreateTime() == null) {
			proto.setCreateTime(now);
		}
		if (proto.getUpdateTime() == null) {
			proto.setUpdateTime(now);
		}

		int count = transactionTemplate.execute(status -> springMapper().insert(proto));

		return count;
	}

	public int updateByPK(ConfigDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		proto.setUpdateTime(new Date());
		return transactionTemplate.execute(status -> springMapper().updateById(proto));
	}

	private ConfigMapper springMapper() {
		SqlSessionTemplate template = sqlSessionTemplate;

		if (template == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for ConfigMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("ConfigRepository is using Spring managed ConfigMapper.");
		}

		return template.getMapper(ConfigMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for ConfigMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private ConfigDO requireFound(ConfigDO config, String field, String value) {
		if (config == null) {
			throw new EmptyResultDataAccessException(String.format("No config found by %s(%s).", field, value), 1);
		}

		return config;
	}
}
