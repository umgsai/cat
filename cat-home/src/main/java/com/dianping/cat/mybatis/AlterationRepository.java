package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.AlterationDO;
import com.dianping.cat.mybatis.mapper.AlterationMapper;
import com.dianping.cat.home.dal.report.Alteration;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

public class AlterationRepository extends SpringBackedRepositorySupport<AlterationMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlterationRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlterationMapper.xml";

	public AlterationRepository() {
		super(AlterationMapper.class, MAPPER_RESOURCE,
				"AlterationRepository is using Spring managed AlterationMapper.");
	}

	public Alteration createLocal() {
		return new Alteration();
	}

	public int deleteByPK(Alteration proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for Alteration.", e);
		}
	}

	public List<Alteration> findByTypeDruation(java.util.Date startTime, java.util.Date endTime, String type) {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		try {
			return mapper.findByTypeDruation(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByTypeDruation for Alteration.", e);
		}
	}

	public List<Alteration> findByDtdh(java.util.Date startTime, java.util.Date endTime, String type, String domain, String hostname) {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		record.setDomain(domain);
		record.setHostname(hostname);
		try {
			return mapper.findByDtdh(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByDtdh for Alteration.", e);
		}
	}

	public List<Alteration> findByDtdhTypes(java.util.Date startTime, java.util.Date endTime, String type, String domain, String hostname, String[] types) {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		record.setDomain(domain);
		record.setHostname(hostname);
		record.setTypes(types);
		try {
			return mapper.findByDtdhTypes(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByDtdhTypes for Alteration.", e);
		}
	}

	public List<Alteration> findByDomainAndTime(java.util.Date startTime, java.util.Date endTime, String domain) {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setDomain(domain);
		try {
			return mapper.findByDomainAndTime(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByDomainAndTime for Alteration.", e);
		}
	}

	public Alteration findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public Alteration findByPK(long keyId) {
		AlterationMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for Alteration.", e);
		}
	}

	public int insert(Alteration proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			AlterationDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for Alteration.", e);
		}
	}

	public int updateByPK(Alteration proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for Alteration.", e);
		}
	}

	private Alteration requireFound(AlterationDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Alteration found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private Alteration toModel(AlterationDO record) {
		Alteration model = new Alteration();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
		}
		if (record.getTitle() != null) {
			model.setTitle(record.getTitle());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getHostname() != null) {
			model.setHostname(record.getHostname());
		}
		if (record.getIp() != null) {
			model.setIp(record.getIp());
		}
		if (record.getChangeTime() != null) {
			model.setChangeTime(record.getChangeTime());
		}
		if (record.getUser() != null) {
			model.setUser(record.getUser());
		}
		if (record.getAltGroup() != null) {
			model.setAltGroup(record.getAltGroup());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getUrl() != null) {
			model.setUrl(record.getUrl());
		}
		if (record.getStatus() != null) {
			model.setStatus(record.getStatus());
		}
		if (record.getCreateTime() != null) {
			model.setCreateTime(record.getCreateTime());
		}
		if (record.getUpdateTime() != null) {
			model.setUpdateTime(record.getUpdateTime());
		}
		model.afterLoad();
		return model;
	}

	private AlterationDO toRecord(Alteration model) {
		AlterationDO record = new AlterationDO();

		record.setId(model.getId());
		record.setType(model.getType());
		record.setTitle(model.getTitle());
		record.setDomain(model.getDomain());
		record.setHostname(model.getHostname());
		record.setIp(model.getIp());
		record.setChangeTime(model.getChangeTime());
		record.setUser(model.getUser());
		record.setAltGroup(model.getAltGroup());
		record.setContent(model.getContent());
		record.setUrl(model.getUrl());
		record.setStatus(model.getStatus());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		record.setStartTime(model.getStartTime());
		record.setEndTime(model.getEndTime());
		record.setTypes(model.getTypes());
		return record;
	}
}
