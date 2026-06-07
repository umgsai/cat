package com.dianping.cat.core.mybatis.repository.config.modification;

import com.dianping.cat.core.mybatis.repository.SpringBackedRepositorySupport;
import com.dianping.cat.core.mybatis.generated.config.modification.dao.ConfigModificationMapper;
import com.dianping.cat.core.mybatis.generated.config.modification.dao.data.ConfigModificationDO;
import com.dianping.cat.home.dal.report.ConfigModification;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

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

	public int deleteByPK(ConfigModification proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(ConfigModificationMapper.class).deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for ConfigModification.", e);
		}
	}

	public ConfigModification findByPK(int keyId, Readset<ConfigModification> readset) throws DalException {
		ConfigModificationMapper mapper = springMapper(LOGGER);

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		}

		try (SqlSession session = openSession()) {
			ConfigModificationDO record = session.getMapper(ConfigModificationMapper.class).findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for ConfigModification.", e);
		}
	}

	public int insert(ConfigModification proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			ConfigModificationDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		}

		try (SqlSession session = openSession()) {
			ConfigModificationDO record = toRecord(proto);
			int count = session.getMapper(ConfigModificationMapper.class).insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for ConfigModification.", e);
		}
	}

	public int updateByPK(ConfigModification proto, Updateset<ConfigModification> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(ConfigModificationMapper.class).updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for ConfigModification.", e);
		}
	}

	private ConfigModification requireFound(ConfigModificationDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No ConfigModification found by " + field + "(" + value + ").");
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
		record.setDate(model.getDate());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
