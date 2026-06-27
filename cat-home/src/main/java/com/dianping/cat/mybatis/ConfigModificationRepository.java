package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.ConfigModificationDO;
import com.dianping.cat.mybatis.mapper.ConfigModificationMapper;
import com.dianping.cat.home.dal.report.ConfigModification;
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

	public ConfigModification createLocal() {
		return new ConfigModification();
	}

	public int deleteByPK(ConfigModification proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for ConfigModification.", e);
		}
	}

	public ConfigModification findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public ConfigModification findByPK(long keyId) {
		ConfigModificationMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for ConfigModification.", e);
		}
	}

	public int insert(ConfigModification proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			ConfigModificationDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for ConfigModification.", e);
		}
	}

	public int updateByPK(ConfigModification proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for ConfigModification.", e);
		}
	}

	private ConfigModification requireFound(ConfigModificationDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No ConfigModification found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private ConfigModification toModel(ConfigModificationDO record) {
		ConfigModification model = new ConfigModification();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getUserName() != null) {
			model.setUserName(record.getUserName());
		}
		if (record.getAccountName() != null) {
			model.setAccountName(record.getAccountName());
		}
		if (record.getActionName() != null) {
			model.setActionName(record.getActionName());
		}
		if (record.getArgument() != null) {
			model.setArgument(record.getArgument());
		}
		if (record.getDate() != null) {
			model.setDate(record.getDate());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getUpdateTime() != null) {
			model.setUpdateTime(record.getUpdateTime());
		}
		model.afterLoad();
		return model;
	}

	private ConfigModificationDO toRecord(ConfigModification model) {
		ConfigModificationDO record = new ConfigModificationDO();

		record.setId(model.getId());
		record.setUserName(model.getUserName());
		record.setAccountName(model.getAccountName());
		record.setActionName(model.getActionName());
		record.setArgument(model.getArgument());
		record.setModifyTime(model.getModifyTime());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
