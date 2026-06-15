package com.dianping.cat.core.mybatis.repository.hostinfo;

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

import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.mybatis.generated.hostinfo.dao.HostinfoMapper;
import com.dianping.cat.core.mybatis.generated.hostinfo.dao.data.HostinfoDO;

public class HostinfoRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(HostinfoRepository.class);

	private static final String DATA_SOURCE_NAME = "cat";

	private static final String MAPPER_RESOURCE = "mybatis/mapper/HostinfoMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private DataSourceManager m_dataSourceManager;

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public Hostinfo createLocal() {
		return new Hostinfo();
	}

	public int deleteByPK(Hostinfo proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(HostinfoMapper.class).deleteByPrimaryKey(proto.getKeyId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Hostinfo.", e);
		}
	}

	public List<Hostinfo> findAllIp(Readset<Hostinfo> readset) throws DalException {
		HostinfoMapper mapper = springMapper();

		if (mapper != null) {
			HostinfoDO record = new HostinfoDO();

			return mapper.findAllIp(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			HostinfoDO record = new HostinfoDO();

			return session.getMapper(HostinfoMapper.class).findAllIp(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findAllIp for Hostinfo.", e);
		}
	}

	public Hostinfo findByPK(int keyId, Readset<Hostinfo> readset) throws DalException {
		HostinfoMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		}

		try (SqlSession session = openSession()) {
			HostinfoDO record = session.getMapper(HostinfoMapper.class).findByPrimaryKey(keyId);

			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Hostinfo.", e);
		}
	}

	public Hostinfo findByIp(String ip, Readset<Hostinfo> readset) throws DalException {
		HostinfoMapper mapper = springMapper();
		HostinfoDO record = new HostinfoDO();

		record.setIp(ip);
		if (mapper != null) {
			HostinfoDO result = mapper.findByIp(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByIp", record.toString());
		}

		try (SqlSession session = openSession()) {
			HostinfoDO result = session.getMapper(HostinfoMapper.class).findByIp(record).stream().findFirst()
					.orElse(null);

			return requireFound(result, "findByIp", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByIp for Hostinfo.", e);
		}
	}

	public int insert(Hostinfo proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			HostinfoDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper().insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		}

		try (SqlSession session = openSession()) {
			HostinfoDO record = toRecord(proto);
			int count = session.getMapper(HostinfoMapper.class).insert(record);

			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Hostinfo.", e);
		}
	}

	public int updateByPK(Hostinfo proto, Updateset<Hostinfo> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(HostinfoMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Hostinfo.", e);
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

		configuration.addMapper(HostinfoMapper.class);
		loadMapperXml(configuration);
		return new SqlSessionFactoryBuilder().build(configuration);
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private HostinfoMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			return null;
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("HostinfoRepository is using Spring managed HostinfoMapper.");
		}

		return sqlSessionTemplate.getMapper(HostinfoMapper.class);
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

	private Hostinfo requireFound(HostinfoDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Hostinfo found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private Hostinfo toModel(HostinfoDO record) {
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
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getLastModifiedDate() != null) {
			model.setLastModifiedDate(record.getLastModifiedDate());
		}
		model.afterLoad();
		return model;
	}

	private HostinfoDO toRecord(Hostinfo model) {
		HostinfoDO record = new HostinfoDO();

		record.setId(model.getId());
		record.setIp(model.getIp());
		record.setDomain(model.getDomain());
		record.setHostname(model.getHostname());
		record.setCreationDate(model.getCreationDate());
		record.setLastModifiedDate(model.getLastModifiedDate());
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
