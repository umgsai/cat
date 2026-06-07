package com.dianping.cat.core.report.daily.repository;

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

import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.report.daily.dao.DailyReportMapper;
import com.dianping.cat.core.report.daily.dao.data.DailyReportDO;

public class DailyReportRepository {
	private static final String DATA_SOURCE_NAME = "cat";

	private static final String MAPPER_RESOURCE = "mybatis/mapper/DailyReportMapper.xml";

	@Inject
	private DataSourceManager m_dataSourceManager;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public DailyReport createLocal() {
		return new DailyReport();
	}

	public int deleteByDomainNamePeriod(DailyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			int count = session.getMapper(DailyReportMapper.class).deleteByDomainNamePeriod(proto.getDomain(),
					proto.getName(), proto.getPeriod());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when deleting daily report by domain/name/period: " + proto, e);
		}
	}

	public int deleteByPK(DailyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			int count = session.getMapper(DailyReportMapper.class).deleteById(proto.getKeyId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when deleting daily report by primary key: " + proto, e);
		}
	}

	public DailyReport findByDomainNamePeriod(String domain, String name, java.util.Date period,
			Readset<DailyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			DailyReportDO report = session.getMapper(DailyReportMapper.class).findByDomainNamePeriod(domain, name,
					period);

			return requireFound(report, "domain/name/period", domain + "/" + name + "/" + period);
		} catch (DalException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when finding daily report by domain/name/period: " + domain + "/" + name
					+ "/" + period, e);
		}
	}

	public DailyReport findByPK(int keyId, Readset<DailyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			DailyReportDO report = session.getMapper(DailyReportMapper.class).findById(keyId);

			return requireFound(report, "id", String.valueOf(keyId));
		} catch (DalException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when finding daily report by primary key: " + keyId, e);
		}
	}

	public int insert(DailyReport proto) throws DalException {
		try (SqlSession session = openSession()) {
			DailyReportDO report = toDailyReportDO(proto);
			int count = session.getMapper(DailyReportMapper.class).insert(report);

			session.commit();
			proto.setId(report.getId());
			proto.setKeyId(report.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when inserting daily report: " + proto, e);
		}
	}

	public List<DailyReport> queryLatestReportsByDomainName(String domain, String name, int limits,
			Readset<DailyReport> readset) throws DalException {
		try (SqlSession session = openSession()) {
			return session.getMapper(DailyReportMapper.class).queryLatestReportsByDomainName(domain, name, limits)
					.stream()
					.map(this::toDailyReport)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when querying latest daily reports by domain/name: " + domain + "/" + name,
					e);
		}
	}

	public int updateByPK(DailyReport proto, Updateset<DailyReport> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			int count = session.getMapper(DailyReportMapper.class).updateById(toDailyReportDO(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when updating daily report by primary key: " + proto, e);
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

		configuration.addMapper(DailyReportMapper.class);
		loadMapperXml(configuration);
		return new SqlSessionFactoryBuilder().build(configuration);
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private DailyReport requireFound(DailyReportDO report, String field, String value) throws DalNotFoundException {
		if (report == null) {
			throw new DalNotFoundException(String.format("No daily report found by %s(%s).", field, value));
		}

		return toDailyReport(report);
	}

	private DailyReport toDailyReport(DailyReportDO reportDO) {
		DailyReport report = new DailyReport();

		report.setId(reportDO.getId());
		report.setName(reportDO.getName());
		report.setIp(reportDO.getIp());
		report.setDomain(reportDO.getDomain());
		report.setPeriod(reportDO.getPeriod());
		report.setType(reportDO.getType());
		report.setCreationDate(reportDO.getCreationDate());
		report.afterLoad();
		return report;
	}

	private DailyReportDO toDailyReportDO(DailyReport report) {
		DailyReportDO reportDO = new DailyReportDO();

		reportDO.setId(report.getKeyId() > 0 ? report.getKeyId() : report.getId());
		reportDO.setName(report.getName());
		reportDO.setIp(report.getIp());
		reportDO.setDomain(report.getDomain());
		reportDO.setPeriod(report.getPeriod());
		reportDO.setType(report.getType());
		reportDO.setCreationDate(report.getCreationDate());
		return reportDO;
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
