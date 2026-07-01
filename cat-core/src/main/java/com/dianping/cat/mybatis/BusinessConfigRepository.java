package com.dianping.cat.mybatis;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.data.BusinessConfigDO;
import com.dianping.cat.mybatis.mapper.BusinessConfigMapper;

@Component("businessConfigRepository")
public class BusinessConfigRepository extends SpringBackedRepositorySupport<BusinessConfigMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessConfigRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/BusinessConfigMapper.xml";

	public BusinessConfigRepository() {
		super(BusinessConfigMapper.class, MAPPER_RESOURCE,
				"BusinessConfigRepository is using Spring managed BusinessConfigMapper.");
	}

	public BusinessConfigDO createLocal() {
		return new BusinessConfigDO();
	}

	public int deleteByPK(BusinessConfigDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
	}

	public List<BusinessConfigDO> findByName(String name) {
		BusinessConfigMapper mapper = springMapper(LOGGER);
		BusinessConfigDO record = new BusinessConfigDO();

		record.setName(name);
		return mapper.findByName(record);
	}

	public BusinessConfigDO findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public BusinessConfigDO findByPK(long keyId) {
		BusinessConfigMapper mapper = springMapper(LOGGER);

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public BusinessConfigDO findByNameDomain(String name, String domain) {
		BusinessConfigMapper mapper = springMapper(LOGGER);
		BusinessConfigDO record = new BusinessConfigDO();

		record.setName(name);
		record.setDomain(domain);
		BusinessConfigDO result = mapper.findByNameDomain(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByNameDomain", record.toString());
	}

	public int insert(BusinessConfigDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));

		return count;
	}

	public int updateByPK(BusinessConfigDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
	}

	public int updateBaseConfigByDomain(BusinessConfigDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper(LOGGER).updateBaseConfigByDomain(proto));
	}

	private BusinessConfigDO requireFound(BusinessConfigDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No BusinessConfig found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
