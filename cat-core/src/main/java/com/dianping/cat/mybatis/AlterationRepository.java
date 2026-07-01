package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.AlterationDO;
import com.dianping.cat.mybatis.mapper.AlterationMapper;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("alterationRepository")
public class AlterationRepository extends SpringBackedRepositorySupport<AlterationMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlterationRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlterationMapper.xml";

	public AlterationRepository() {
		super(AlterationMapper.class, MAPPER_RESOURCE,
				"AlterationRepository is using Spring managed AlterationMapper.");
	}

	public AlterationDO createLocal() {
		return new AlterationDO();
	}

	public int deleteByPK(AlterationDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for Alteration.", e);
		}
	}

	public List<AlterationDO> findByTypeDruation(java.util.Date startTime, java.util.Date endTime, String type) {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		try {
			return mapper.findByTypeDruation(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByTypeDruation for Alteration.", e);
		}
	}

	public List<AlterationDO> findByDtdh(java.util.Date startTime, java.util.Date endTime, String type, String domain, String hostname) {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		record.setDomain(domain);
		record.setHostname(hostname);
		try {
			return mapper.findByDtdh(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByDtdh for Alteration.", e);
		}
	}

	public List<AlterationDO> findByDtdhTypes(java.util.Date startTime, java.util.Date endTime, String type, String domain, String hostname, String[] types) {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		record.setDomain(domain);
		record.setHostname(hostname);
		record.setTypes(types);
		try {
			return mapper.findByDtdhTypes(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByDtdhTypes for Alteration.", e);
		}
	}

	public List<AlterationDO> findByDomainAndTime(java.util.Date startTime, java.util.Date endTime, String domain) {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setDomain(domain);
		try {
			return mapper.findByDomainAndTime(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByDomainAndTime for Alteration.", e);
		}
	}

	public AlterationDO findByPK(int id) {
		return findByPK((long) id);
	}

	public AlterationDO findByPK(long id) {
		AlterationMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for Alteration.", e);
		}
	}

	public int insert(AlterationDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for Alteration.", e);
		}
	}

	public int updateByPK(AlterationDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for Alteration.", e);
		}
	}

	private AlterationDO requireFound(AlterationDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Alteration found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
