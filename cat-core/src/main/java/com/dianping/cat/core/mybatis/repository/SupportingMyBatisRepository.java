package com.dianping.cat.core.mybatis.repository;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.datasource.DataSourceManager;

import com.dianping.cat.spring.CatSpringContext;

public final class SupportingMyBatisRepository {
	private static final String DATA_SOURCE_NAME = "cat";

	private SupportingMyBatisRepository() {
	}

	public static SqlSessionFactory newSqlSessionFactory(DataSourceManager dataSourceManager, Class<?> mapperClass,
			String mapperResource) {
		Configuration configuration = new Configuration(new Environment(DATA_SOURCE_NAME, new JdbcTransactionFactory(),
				new UnidalDataSource(dataSourceManager, DATA_SOURCE_NAME)));

		configuration.addMapper(mapperClass);
		loadMapperXml(configuration, mapperResource);
		return new SqlSessionFactoryBuilder().build(configuration);
	}

	public static <T> T springMapper(Class<T> mapperClass, Logger logger, AtomicBoolean logged, String message) {
		SqlSessionTemplate sqlSessionTemplate = CatSpringContext.getBean(SqlSessionTemplate.class);

		if (sqlSessionTemplate == null) {
			return null;
		}

		if (logged.compareAndSet(false, true)) {
			logger.info(message);
		}

		return sqlSessionTemplate.getMapper(mapperClass);
	}

	public static TransactionTemplate springTransactionTemplate() {
		return CatSpringContext.getBean(TransactionTemplate.class);
	}

	private static void loadMapperXml(Configuration configuration, String mapperResource) {
		try (Reader reader = Resources.getResourceAsReader(mapperResource)) {
			XMLMapperBuilder mapperParser = new XMLMapperBuilder(reader, configuration, mapperResource,
					configuration.getSqlFragments());

			mapperParser.parse();
		} catch (IOException e) {
			throw new IllegalStateException("Error when loading MyBatis mapper: " + mapperResource, e);
		}
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
