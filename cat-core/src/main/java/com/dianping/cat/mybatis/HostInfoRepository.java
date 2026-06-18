package com.dianping.cat.mybatis;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.mybatis.mapper.HostInfoMapper;
import com.dianping.cat.mybatis.data.HostInfoDO;

public class HostInfoRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(HostInfoRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public Hostinfo createLocal() {
		return new Hostinfo();
	}

	public int deleteByPK(Hostinfo proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
	}

	public List<Hostinfo> findAllIp() {
		HostInfoMapper mapper = springMapper();

		HostInfoDO record = new HostInfoDO();

		return mapper.findAllIp(record).stream().map(this::toModel).collect(Collectors.toList());
	}

	public Hostinfo findByPK(long keyId) {
		HostInfoMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public Hostinfo findByIp(String ip) {
		HostInfoMapper mapper = springMapper();
		HostInfoDO record = new HostInfoDO();

		record.setIp(ip);
		HostInfoDO result = mapper.findByIp(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByIp", record.toString());
	}

	public int insert(Hostinfo proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		Date now = new Date();

		if (proto.getCreationDate() == null) {
			proto.setCreationDate(now);
		}
		if (proto.getLastModifiedDate() == null) {
			proto.setLastModifiedDate(now);
		}

		HostInfoDO record = toRecord(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(record));

		proto.setId(record.getId());
		proto.setKeyId(record.getId());
		return count;
	}

	public int updateByPK(Hostinfo proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
	}

	private HostInfoMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for HostinfoMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("HostinfoRepository is using Spring managed HostinfoMapper.");
		}

		return sqlSessionTemplate.getMapper(HostInfoMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for HostinfoMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private Hostinfo requireFound(HostInfoDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Hostinfo found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private Hostinfo toModel(HostInfoDO record) {
		Hostinfo model = new Hostinfo();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getIp() != null) {
			model.setIp(record.getIp());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getHostname() != null) {
			model.setHostname(record.getHostname());
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

	private HostInfoDO toRecord(Hostinfo model) {
		HostInfoDO record = new HostInfoDO();

		record.setId(model.getId());
		record.setIp(model.getIp());
		record.setDomain(model.getDomain());
		record.setHostname(model.getHostname());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		return record;
	}

}
