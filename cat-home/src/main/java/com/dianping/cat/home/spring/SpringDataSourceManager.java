package com.dianping.cat.home.spring;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.unidal.dal.jdbc.datasource.DataSource;
import org.unidal.dal.jdbc.datasource.DataSourceDescriptor;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptor;

final class SpringDataSourceManager implements DataSourceManager {
	private static final String CAT_DATA_SOURCE_NAME = "cat";

	private final DataSource m_catDataSource;

	SpringDataSourceManager(javax.sql.DataSource catDataSource) {
		m_catDataSource = new SpringDataSource(catDataSource);
	}

	@Override
	public DataSource getDataSource(String name) {
		if (CAT_DATA_SOURCE_NAME.equals(name)) {
			return m_catDataSource;
		}
		throw new IllegalArgumentException("Unsupported datasource: " + name);
	}

	@Override
	public List<String> getDataSourceNames() {
		return Collections.singletonList(CAT_DATA_SOURCE_NAME);
	}

	private static final class SpringDataSource implements DataSource {
		private final javax.sql.DataSource m_delegate;

		private final DataSourceDescriptor m_descriptor;

		private SpringDataSource(javax.sql.DataSource delegate) {
			m_delegate = delegate;
			JdbcDataSourceDescriptor descriptor = new JdbcDataSourceDescriptor();

			descriptor.setId(CAT_DATA_SOURCE_NAME);
			descriptor.setType("jdbc");
			m_descriptor = descriptor;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return m_delegate.getConnection();
		}

		@Override
		public DataSourceDescriptor getDescriptor() {
			return m_descriptor;
		}

		@Override
		public void initialize(DataSourceDescriptor descriptor) {
		}
	}
}
