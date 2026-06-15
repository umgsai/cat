package com.dianping.cat.core.mybatis.repository.project;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;
import org.unidal.dal.jdbc.datasource.DataSourceManager;

import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.mybatis.generated.project.dao.ProjectMapper;
import com.dianping.cat.core.mybatis.generated.project.dao.data.ProjectDO;

public class ProjectRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepository.class);

	private static final String DATA_SOURCE_NAME = "cat";

	private static final String MAPPER_RESOURCE = "mybatis/mapper/ProjectMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();
	private DataSourceManager m_dataSourceManager;

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public Project createLocal() {
		return new Project();
	}

	public int deleteByPK(Project proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(ProjectMapper.class).deleteByPrimaryKey(proto.getKeyId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Project.", e);
		}
	}

	public List<Project> findAll(Readset<Project> readset) throws DalException {
		ProjectMapper mapper = springMapper();

		if (mapper != null) {
			ProjectDO record = new ProjectDO();

			return mapper.findAll(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			ProjectDO record = new ProjectDO();

			return session.getMapper(ProjectMapper.class).findAll(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findAll for Project.", e);
		}
	}

	public Project findByPK(int keyId, Readset<Project> readset) throws DalException {
		ProjectMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		}

		try (SqlSession session = openSession()) {
			ProjectDO record = session.getMapper(ProjectMapper.class).findByPrimaryKey(keyId);

			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Project.", e);
		}
	}

	public Project findByDomain(String domain, Readset<Project> readset) throws DalException {
		ProjectDO record = new ProjectDO();
		ProjectMapper mapper = springMapper();

		record.setDomain(domain);
		if (mapper != null) {
			ProjectDO result = mapper.findByDomain(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByDomain", record.toString());
		}

		try (SqlSession session = openSession()) {
			ProjectDO result = session.getMapper(ProjectMapper.class).findByDomain(record).stream().findFirst()
					.orElse(null);

			return requireFound(result, "findByDomain", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByDomain for Project.", e);
		}
	}

	public Project findByCmdbDomain(String domain, Readset<Project> readset) throws DalException {
		ProjectDO record = new ProjectDO();
		ProjectMapper mapper = springMapper();

		record.setDomain(domain);
		if (mapper != null) {
			ProjectDO result = mapper.findByCmdbDomain(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByCmdbDomain", record.toString());
		}

		try (SqlSession session = openSession()) {
			ProjectDO result = session.getMapper(ProjectMapper.class).findByCmdbDomain(record).stream().findFirst()
					.orElse(null);

			return requireFound(result, "findByCmdbDomain", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByCmdbDomain for Project.", e);
		}
	}

	public int insert(Project proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			ProjectDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper().insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		}

		try (SqlSession session = openSession()) {
			ProjectDO record = toRecord(proto);
			int count = session.getMapper(ProjectMapper.class).insert(record);

			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Project.", e);
		}
	}

	public int updateByPK(Project proto, Updateset<Project> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(ProjectMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Project.", e);
		}
	}

	private SqlSessionFactory getSqlSessionFactory() {
		SqlSessionFactory sqlSessionFactory = m_sqlSessionFactory;

		if (sqlSessionFactory == null) {
			synchronized (this) {
				sqlSessionFactory = m_sqlSessionFactory;

				if (sqlSessionFactory == null) {
					sqlSessionFactory = newSqlSessionFactory();
					m_sqlSessionFactory = sqlSessionFactory;
				}
			}
		}

		return sqlSessionFactory;
	}

	private void loadMapperXml(Configuration configuration) {
		try (Reader reader = Resources.getResourceAsReader(MAPPER_RESOURCE)) {
			XMLMapperBuilder mapperParser = new XMLMapperBuilder(reader, configuration, MAPPER_RESOURCE,
					configuration.getSqlFragments());

			mapperParser.parse();
		} catch (IOException e) {
			throw new IllegalStateException("Error when loading MyBatis mapper: " + MAPPER_RESOURCE, e);
		}
	}

	private SqlSessionFactory newSqlSessionFactory() {
		Configuration configuration = new Configuration(new Environment(DATA_SOURCE_NAME, new JdbcTransactionFactory(),
				new UnidalDataSource(m_dataSourceManager, DATA_SOURCE_NAME)));

		configuration.addMapper(ProjectMapper.class);
		loadMapperXml(configuration);
		return new SqlSessionFactoryBuilder().build(configuration);
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private ProjectMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			return null;
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("ProjectRepository is using Spring managed ProjectMapper.");
		}

		return sqlSessionTemplate.getMapper(ProjectMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private Project requireFound(ProjectDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Project found by " + field + "(" + value + ").");
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
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getModifyDate() != null) {
			model.setModifyDate(record.getModifyDate());
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
		record.setCreationDate(model.getCreationDate());
		record.setModifyDate(model.getModifyDate());
		record.setKeyId(model.getKeyId());
		return record;
	}

	private static final class UnidalDataSource implements DataSource {
		private final DataSourceManager m_dataSourceManager;

		private final String m_dataSourceName;

		private UnidalDataSource(DataSourceManager dataSourceManager, String dataSourceName) {
			m_dataSourceManager = dataSourceManager;
			m_dataSourceName = dataSourceName;
		}

		@Override
		public Connection getConnection() throws java.sql.SQLException {
			return m_dataSourceManager.getDataSource(m_dataSourceName).getConnection();
		}

		@Override
		public Connection getConnection(String username, String password) throws java.sql.SQLException {
			return getConnection();
		}

		@Override
		public int getLoginTimeout() {
			return 0;
		}

		@Override
		public java.io.PrintWriter getLogWriter() {
			return null;
		}

		@Override
		public java.util.logging.Logger getParentLogger() {
			return java.util.logging.Logger.getGlobal();
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) {
			return false;
		}

		@Override
		public void setLoginTimeout(int seconds) {
		}

		@Override
		public void setLogWriter(java.io.PrintWriter out) {
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
			throw new java.sql.SQLException("Not a wrapper for " + iface.getName());
		}
	}
}
