package com.dianping.cat.core.config.repository;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.List;
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
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.dao.ConfigMapper;
import com.dianping.cat.core.config.dao.data.ConfigDO;

public class ConfigRepository {
	private static final String DATA_SOURCE_NAME = "cat";

	private static final String MAPPER_RESOURCE = "mybatis/mapper/ConfigMapper.xml";

	@Inject
	private DataSourceManager m_dataSourceManager;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public Config createLocal() {
		return new Config();
	}

	public int deleteByPK(Config proto) throws DalException {
		try (SqlSession session = openSession()) {
			int count = session.getMapper(ConfigMapper.class).deleteById(proto.getKeyId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when deleting config by primary key: " + proto, e);
		}
	}

	public List<Config> findAllConfig(Readset<Config> readset) throws DalException {
		try (SqlSession session = openSession()) {
			return session.getMapper(ConfigMapper.class).queryAll().stream()
					.map(this::toConfig)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when finding all config records.", e);
		}
	}

	public Config findByName(String name, Readset<Config> readset) throws DalException {
		try (SqlSession session = openSession()) {
			ConfigDO config = session.getMapper(ConfigMapper.class).findByName(name);

			return requireFound(config, "name", name);
		} catch (DalException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when finding config by name: " + name, e);
		}
	}

	public Config findByPK(int keyId, Readset<Config> readset) throws DalException {
		try (SqlSession session = openSession()) {
			ConfigDO config = session.getMapper(ConfigMapper.class).findById(keyId);

			return requireFound(config, "id", String.valueOf(keyId));
		} catch (DalException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when finding config by primary key: " + keyId, e);
		}
	}

	public int insert(Config proto) throws DalException {
		try (SqlSession session = openSession()) {
			ConfigDO config = toConfigDO(proto);
			int count = session.getMapper(ConfigMapper.class).insert(config);

			session.commit();
			proto.setId(config.getId());
			proto.setKeyId(config.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when inserting config: " + proto, e);
		}
	}

	public int updateByPK(Config proto, Updateset<Config> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			int count = session.getMapper(ConfigMapper.class).updateById(toConfigDO(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when updating config by primary key: " + proto, e);
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

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private Config requireFound(ConfigDO config, String field, String value) throws DalNotFoundException {
		if (config == null) {
			throw new DalNotFoundException(String.format("No config found by %s(%s).", field, value));
		}

		return toConfig(config);
	}

	private SqlSessionFactory newSqlSessionFactory() {
		Configuration configuration = new Configuration(new Environment(DATA_SOURCE_NAME, new JdbcTransactionFactory(),
				new UnidalDataSource(m_dataSourceManager, DATA_SOURCE_NAME)));

		configuration.addMapper(ConfigMapper.class);
		loadMapperXml(configuration);
		return new SqlSessionFactoryBuilder().build(configuration);
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

	private Config toConfig(ConfigDO configDO) {
		Config config = new Config();

		config.setId(configDO.getId());
		config.setName(configDO.getName());
		config.setContent(configDO.getContent());
		config.setCreationDate(configDO.getCreationDate());
		config.setModifyDate(configDO.getModifyDate());
		config.afterLoad();
		return config;
	}

	private ConfigDO toConfigDO(Config config) {
		ConfigDO configDO = new ConfigDO();

		configDO.setId(config.getKeyId() > 0 ? config.getKeyId() : config.getId());
		configDO.setName(config.getName());
		configDO.setContent(config.getContent());
		configDO.setCreationDate(config.getCreationDate());
		configDO.setModifyDate(config.getModifyDate());
		return configDO;
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
