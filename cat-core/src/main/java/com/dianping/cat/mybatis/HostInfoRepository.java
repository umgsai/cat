package com.dianping.cat.mybatis;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.mapper.HostInfoMapper;
import com.dianping.cat.mybatis.data.HostInfoDO;

@Component("hostinfoRepository")
public class HostInfoRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(HostInfoRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public HostInfoDO createLocal() {
		return new HostInfoDO();
	}

	public int deleteByPK(HostInfoDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getId()));
	}

	public List<HostInfoDO> findAllIp() {
		HostInfoMapper mapper = springMapper();

		HostInfoDO record = new HostInfoDO();

		return mapper.findAllIp(record);
	}

	public HostInfoDO findByPK(long id) {
		HostInfoMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
	}

	public HostInfoDO findByIp(String ip) {
		HostInfoMapper mapper = springMapper();
		HostInfoDO record = new HostInfoDO();

		record.setIp(ip);
		HostInfoDO result = mapper.findByIp(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByIp", record.toString());
	}

	public int insert(HostInfoDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		Date now = new Date();

		if (proto.getCreateTime() == null) {
			proto.setCreateTime(now);
		}
		if (proto.getUpdateTime() == null) {
			proto.setUpdateTime(now);
		}

		int count = transactionTemplate.execute(status -> springMapper().insert(proto));

		return count;
	}

	public int updateByPK(HostInfoDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
	}

	private HostInfoMapper springMapper() {
		SqlSessionTemplate template = sqlSessionTemplate;

		if (template == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for HostinfoMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("HostinfoRepository is using Spring managed HostinfoMapper.");
		}

		return template.getMapper(HostInfoMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for HostinfoMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private HostInfoDO requireFound(HostInfoDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No host info found by " + field + "(" + value + ").", 1);
		}

		return record;
	}

}
