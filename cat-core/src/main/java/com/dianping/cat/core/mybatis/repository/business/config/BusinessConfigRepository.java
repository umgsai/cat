package com.dianping.cat.core.mybatis.repository.business.config;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.mybatis.business.config.dao.BusinessConfigMapper;
import com.dianping.cat.core.mybatis.business.config.dao.data.BusinessConfigDO;

public class BusinessConfigRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessConfigRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public BusinessConfig createLocal() {
		return new BusinessConfig();
	}

	public int deleteByPK(BusinessConfig proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
	}

	public List<BusinessConfig> findByName(String name) {
		BusinessConfigMapper mapper = springMapper();
		BusinessConfigDO record = new BusinessConfigDO();

		record.setName(name);
		return mapper.findByName(record).stream().map(this::toModel).collect(Collectors.toList());
	}

	public BusinessConfig findByPK(int keyId) {
		BusinessConfigMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public BusinessConfig findByNameDomain(String name, String domain) {
		BusinessConfigMapper mapper = springMapper();
		BusinessConfigDO record = new BusinessConfigDO();

		record.setName(name);
		record.setDomain(domain);
		BusinessConfigDO result = mapper.findByNameDomain(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByNameDomain", record.toString());
	}

	public int insert(BusinessConfig proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		BusinessConfigDO record = toRecord(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(record));

		proto.setId(record.getId());
		proto.setKeyId(record.getId());
		return count;
	}

	public int updateByPK(BusinessConfig proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
	}

	public int updateBaseConfigByDomain(BusinessConfig proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateBaseConfigByDomain(toRecord(proto)));
	}

	private BusinessConfigMapper springMapper() {
		if (m_sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for BusinessConfigMapper.");
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("BusinessConfigRepository is using Spring managed BusinessConfigMapper.");
		}

		return m_sqlSessionTemplate.getMapper(BusinessConfigMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for BusinessConfigMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private BusinessConfig requireFound(BusinessConfigDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No BusinessConfig found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private BusinessConfig toModel(BusinessConfigDO record) {
		BusinessConfig model = new BusinessConfig();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getName() != null) {
			model.setName(record.getName());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getUpdatetime() != null) {
			model.setUpdatetime(record.getUpdatetime());
		}
		model.afterLoad();
		return model;
	}

	private BusinessConfigDO toRecord(BusinessConfig model) {
		BusinessConfigDO record = new BusinessConfigDO();

		record.setId(model.getId());
		record.setName(model.getName());
		record.setDomain(model.getDomain());
		record.setContent(model.getContent());
		record.setUpdatetime(model.getUpdatetime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
