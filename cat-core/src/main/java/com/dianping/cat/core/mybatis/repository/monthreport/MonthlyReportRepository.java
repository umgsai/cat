package com.dianping.cat.core.mybatis.repository.monthreport;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

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

import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.mybatis.generated.monthreport.dao.MonthreportMapper;
import com.dianping.cat.core.mybatis.generated.monthreport.dao.data.MonthreportDO;
import com.dianping.cat.spring.CatSpringContext;

public class MonthlyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyReportRepository.class);

	private static final String DATA_SOURCE_NAME = "cat";

	private static final String MAPPER_RESOURCE = "mybatis/mapper/MonthreportMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();
	private DataSourceManager m_dataSourceManager;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public void setDataSourceManager(DataSourceManager dataSourceManager) {
		m_dataSourceManager = dataSourceManager;
	}

	public MonthlyReport createLocal() {
		return new MonthlyReport();
	}

	public int deleteByPK(MonthlyReport proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(MonthreportMapper.class).deleteByPrimaryKey(proto.getKeyId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for MonthlyReport.", e);
		}
	}

	public int deleteReportByDomainNamePeriod(MonthlyReport proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteReportByDomainNamePeriod(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(MonthreportMapper.class).deleteReportByDomainNamePeriod(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteReportByDomainNamePeriod for MonthlyReport.", e);
		}
	}

	public MonthlyReport findByPK(int keyId, Readset<MonthlyReport> readset) throws DalException {
		MonthreportMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		}

		try (SqlSession session = openSession()) {
			MonthreportDO record = session.getMapper(MonthreportMapper.class).findByPrimaryKey(keyId);

			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for MonthlyReport.", e);
		}
	}

	public MonthlyReport findReportByDomainNamePeriod(java.util.Date period, String domain, String name,
			Readset<MonthlyReport> readset) throws DalException {
		MonthreportDO record = new MonthreportDO();
		MonthreportMapper mapper = springMapper();

		record.setPeriod(period);
		record.setDomain(domain);
		record.setName(name);
		if (mapper != null) {
			MonthreportDO result = mapper.findReportByDomainNamePeriod(record).stream().findFirst().orElse(null);

			return requireFound(result, "findReportByDomainNamePeriod", record.toString());
		}

		try (SqlSession session = openSession()) {
			MonthreportDO result = session.getMapper(MonthreportMapper.class).findReportByDomainNamePeriod(record)
					.stream()
					.findFirst()
					.orElse(null);

			return requireFound(result, "findReportByDomainNamePeriod", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findReportByDomainNamePeriod for MonthlyReport.", e);
		}
	}

	public int insert(MonthlyReport proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			MonthreportDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper().insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		}

		try (SqlSession session = openSession()) {
			MonthreportDO record = toRecord(proto);
			int count = session.getMapper(MonthreportMapper.class).insert(record);

			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for MonthlyReport.", e);
		}
	}

	public int updateByPK(MonthlyReport proto, Updateset<MonthlyReport> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(MonthreportMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for MonthlyReport.", e);
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

		configuration.addMapper(MonthreportMapper.class);
		loadMapperXml(configuration);
		return new SqlSessionFactoryBuilder().build(configuration);
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private MonthreportMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = CatSpringContext.getBean(SqlSessionTemplate.class);

		if (sqlSessionTemplate == null) {
			return null;
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("MonthlyReportRepository is using Spring managed MonthreportMapper.");
		}

		return sqlSessionTemplate.getMapper(MonthreportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		return CatSpringContext.getBean(TransactionTemplate.class);
	}

	private MonthlyReport requireFound(MonthreportDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No MonthlyReport found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private MonthlyReport toModel(MonthreportDO record) {
		MonthlyReport model = new MonthlyReport();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getName() != null) {
			model.setName(record.getName());
		}
		if (record.getIp() != null) {
			model.setIp(record.getIp());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getPeriod() != null) {
			model.setPeriod(record.getPeriod());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		model.afterLoad();
		return model;
	}

	private MonthreportDO toRecord(MonthlyReport model) {
		MonthreportDO record = new MonthreportDO();

		record.setId(model.getId());
		record.setName(model.getName());
		record.setIp(model.getIp());
		record.setDomain(model.getDomain());
		record.setPeriod(model.getPeriod());
		record.setType(model.getType());
		record.setCreationDate(model.getCreationDate());
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
