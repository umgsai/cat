package com.dianping.cat.mybatis.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.mybatis.data.BusinessConfigDO;
import com.dianping.cat.mybatis.SpringBackedRepositorySupport;

@Component("businessConfigRepository")
public class BusinessConfigRepository extends SpringBackedRepositorySupport<BusinessConfigMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessConfigRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/BusinessConfigMapper.xml";

	public BusinessConfigRepository() {
		super(BusinessConfigMapper.class, MAPPER_RESOURCE,
				"BusinessConfigRepository is using Spring managed BusinessConfigMapper.");
	}

	public BusinessConfig createLocal() {
		return new BusinessConfig();
	}

	public int deleteByPK(BusinessConfig proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
	}

	public List<BusinessConfig> findByName(String name) {
		BusinessConfigMapper mapper = springMapper(LOGGER);
		BusinessConfigDO record = new BusinessConfigDO();

		record.setName(name);
		return mapper.findByName(record).stream().map(this::toModel).collect(Collectors.toList());
	}

	public BusinessConfig findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public BusinessConfig findByPK(long keyId) {
		BusinessConfigMapper mapper = springMapper(LOGGER);

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public BusinessConfig findByNameDomain(String name, String domain) {
		BusinessConfigMapper mapper = springMapper(LOGGER);
		BusinessConfigDO record = new BusinessConfigDO();

		record.setName(name);
		record.setDomain(domain);
		BusinessConfigDO result = mapper.findByNameDomain(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByNameDomain", record.toString());
	}

	public int insert(BusinessConfig proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		BusinessConfigDO record = toRecord(proto);
		int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

		proto.setId(record.getId());
		proto.setKeyId(record.getId());
		return count;
	}

	public int updateByPK(BusinessConfig proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
	}

	public int updateBaseConfigByDomain(BusinessConfig proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper(LOGGER).updateBaseConfigByDomain(toRecord(proto)));
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
		if (record.getCreateTime() != null) {
			model.setCreateTime(record.getCreateTime());
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
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
