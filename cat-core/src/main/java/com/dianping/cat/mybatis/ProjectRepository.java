package com.dianping.cat.mybatis;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.mapper.ProjectMapper;
import com.dianping.cat.mybatis.data.ProjectDO;

@Component("projectRepository")
public class ProjectRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public ProjectDO createLocal() {
		return new ProjectDO();
	}

	public int deleteByPK(ProjectDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getId()));
	}

	public List<ProjectDO> findAll() {
		ProjectMapper mapper = springMapper();

		ProjectDO record = new ProjectDO();

		return mapper.findAll(record);
	}

	public ProjectDO findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public ProjectDO findByPK(long keyId) {
		ProjectMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public ProjectDO findByDomain(String domain) {
		ProjectDO record = new ProjectDO();
		ProjectMapper mapper = springMapper();

		record.setDomain(domain);
		ProjectDO result = mapper.findByDomain(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByDomain", record.toString());
	}

	public ProjectDO findByCmdbDomain(String domain) {
		ProjectDO record = new ProjectDO();
		ProjectMapper mapper = springMapper();

		record.setDomain(domain);
		ProjectDO result = mapper.findByCmdbDomain(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByCmdbDomain", record.toString());
	}

	public int insert(ProjectDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		normalize(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(proto));

		return count;
	}

	public int updateByPK(ProjectDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		normalize(proto);
		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(proto));
	}

	private ProjectMapper springMapper() {
		SqlSessionTemplate template = sqlSessionTemplate;

		if (template == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for ProjectMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("ProjectRepository is using Spring managed ProjectMapper.");
		}

		return template.getMapper(ProjectMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for ProjectMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private ProjectDO requireFound(ProjectDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Project found by " + field + "(" + value + ").", 1);
		}

		return record;
	}

	private void normalize(ProjectDO record) {
		record.setEmail(Optional.ofNullable(record.getEmail()).orElse(""));
		record.setPhone(Optional.ofNullable(record.getPhone()).orElse(""));
	}

}
