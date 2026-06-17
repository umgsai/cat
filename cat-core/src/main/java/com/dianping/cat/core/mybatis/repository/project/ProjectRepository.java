package com.dianping.cat.core.mybatis.repository.project;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.mybatis.project.dao.ProjectMapper;
import com.dianping.cat.core.mybatis.project.dao.data.ProjectDO;

public class ProjectRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public Project createLocal() {
		return new Project();
	}

	public int deleteByPK(Project proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
	}

	public List<Project> findAll() {
		ProjectMapper mapper = springMapper();

		ProjectDO record = new ProjectDO();

		return mapper.findAll(record).stream().map(this::toModel).collect(Collectors.toList());
	}

	public Project findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public Project findByPK(long keyId) {
		ProjectMapper mapper = springMapper();

		return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
	}

	public Project findByDomain(String domain) {
		ProjectDO record = new ProjectDO();
		ProjectMapper mapper = springMapper();

		record.setDomain(domain);
		ProjectDO result = mapper.findByDomain(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByDomain", record.toString());
	}

	public Project findByCmdbDomain(String domain) {
		ProjectDO record = new ProjectDO();
		ProjectMapper mapper = springMapper();

		record.setDomain(domain);
		ProjectDO result = mapper.findByCmdbDomain(record).stream().findFirst().orElse(null);

		return requireFound(result, "findByCmdbDomain", record.toString());
	}

	public int insert(Project proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		ProjectDO record = toRecord(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(record));

		proto.setId(record.getId());
		proto.setKeyId(record.getId());
		return count;
	}

	public int updateByPK(Project proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
	}

	private ProjectMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for ProjectMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("ProjectRepository is using Spring managed ProjectMapper.");
		}

		return sqlSessionTemplate.getMapper(ProjectMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for ProjectMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private Project requireFound(ProjectDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Project found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private Project toModel(ProjectDO record) {
		Project model = new Project();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getCmdbDomain() != null) {
			model.setCmdbDomain(record.getCmdbDomain());
		}
		if (record.getLevel() != null) {
			model.setLevel(record.getLevel());
		}
		if (record.getBu() != null) {
			model.setBu(record.getBu());
		}
		if (record.getCmdbProductline() != null) {
			model.setCmdbProductline(record.getCmdbProductline());
		}
		if (record.getOwner() != null) {
			model.setOwner(record.getOwner());
		}
		if (record.getEmail() != null) {
			model.setEmail(record.getEmail());
		}
		if (record.getPhone() != null) {
			model.setPhone(record.getPhone());
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

	private ProjectDO toRecord(Project model) {
		ProjectDO record = new ProjectDO();

		record.setId(model.getId());
		record.setDomain(model.getDomain());
		record.setCmdbDomain(model.getCmdbDomain());
		record.setLevel(model.getLevel());
		record.setBu(model.getBu());
		record.setCmdbProductline(model.getCmdbProductline());
		record.setOwner(model.getOwner());
		record.setEmail(model.getEmail());
		record.setPhone(model.getPhone());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		return record;
	}

}
