package com.dianping.cat.core.config.repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.dao.ConfigMapper;
import com.dianping.cat.core.config.dao.data.ConfigDO;

public class ConfigRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public Config createLocal() {
		return new Config();
	}

	public int deleteByPK(Config proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteById(proto.getKeyId()));
	}

	public List<Config> findAllConfig(Readset<Config> readset) throws DalException {
		ConfigMapper mapper = springMapper();

		return mapper.queryAll().stream().map(this::toConfig).collect(Collectors.toList());
	}

	public Config findByName(String name, Readset<Config> readset) throws DalException {
		ConfigMapper mapper = springMapper();

		return requireFound(mapper.findByName(name), "name", name);
	}

	public Config findByPK(int keyId, Readset<Config> readset) throws DalException {
		ConfigMapper mapper = springMapper();

		return requireFound(mapper.findById(keyId), "id", String.valueOf(keyId));
	}

	public int insert(Config proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		ConfigDO config = toConfigDO(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(config));

		proto.setId(config.getId());
		proto.setKeyId(config.getId());
		return count;
	}

	public int updateByPK(Config proto, Updateset<Config> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateById(toConfigDO(proto)));
	}

	private ConfigMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for ConfigMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("ConfigRepository is using Spring managed ConfigMapper.");
		}

		return sqlSessionTemplate.getMapper(ConfigMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for ConfigMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private Config requireFound(ConfigDO config, String field, String value) throws DalNotFoundException {
		if (config == null) {
			throw new DalNotFoundException(String.format("No config found by %s(%s).", field, value));
		}

		return toConfig(config);
	}

	private Config toConfig(ConfigDO configDO) {
		Config config = new Config();

		config.setId(configDO.getId());
		config.setName(configDO.getName());
		config.setContent(configDO.getContent());
		config.setCreationDate(configDO.getCreationDate());
		config.setModifyDate(configDO.getModifyDate());
		config.afterLoad();
		return config;
	}

	private ConfigDO toConfigDO(Config config) {
		ConfigDO configDO = new ConfigDO();

		configDO.setId(config.getKeyId() > 0 ? config.getKeyId() : config.getId());
		configDO.setName(config.getName());
		configDO.setContent(config.getContent());
		configDO.setCreationDate(config.getCreationDate());
		configDO.setModifyDate(config.getModifyDate());
		return configDO;
	}

}
