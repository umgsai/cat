package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.ConfigModificationDO;
import com.dianping.cat.mybatis.mapper.ConfigModificationMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("configModificationRepository")
public class ConfigModificationRepository extends SpringBackedRepositorySupport<ConfigModificationMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigModificationRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/ConfigModificationMapper.xml";

	public ConfigModificationRepository() {
		super(ConfigModificationMapper.class, MAPPER_RESOURCE,
				"ConfigModificationRepository is using Spring managed ConfigModificationMapper.");
	}

	public ConfigModificationDO createLocal() {
		return new ConfigModificationDO();
	}

	public int deleteByPK(ConfigModificationDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for ConfigModification.", e);
		}
	}

	public ConfigModificationDO findByPK(int id) {
		return findByPK((long) id);
	}

	public ConfigModificationDO findByPK(long id) {
		ConfigModificationMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for ConfigModification.", e);
		}
	}

	public int insert(ConfigModificationDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for ConfigModification.", e);
		}
	}

	public int updateByPK(ConfigModificationDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for ConfigModification.", e);
		}
	}

	private ConfigModificationDO requireFound(ConfigModificationDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No ConfigModification found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
