package com.dianping.cat.core.mybatis.repository;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.lookup.annotation.Inject;

public abstract class SpringBackedRepositorySupport<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBackedRepositorySupport.class);

	@Inject
	private DataSourceManager m_dataSourceManager;

	private final Class<T> m_mapperClass;

	private final String m_mapperResource;

	private final String m_springMapperMessage;

	private final AtomicBoolean m_springMapperLogged = new AtomicBoolean();

	private volatile SqlSessionFactory m_sqlSessionFactory;

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	protected SpringBackedRepositorySupport(Class<T> mapperClass, String mapperResource,
			String springMapperMessage) {
		m_mapperClass = mapperClass;
		m_mapperResource = mapperResource;
		m_springMapperMessage = springMapperMessage;
	}

	protected SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	protected T springMapper(Logger logger) {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate != null) {
			if (m_springMapperLogged.compareAndSet(false, true)) {
				logger.info(m_springMapperMessage);
			}
			return sqlSessionTemplate.getMapper(m_mapperClass);
		}
		return SupportingMyBatisRepository.springMapper(m_mapperClass, logger, m_springMapperLogged,
				m_springMapperMessage);
	}

	protected TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate != null) {
			return m_transactionTemplate;
		}
		return SupportingMyBatisRepository.springTransactionTemplate();
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private SqlSessionFactory getSqlSessionFactory() {
		SqlSessionFactory sqlSessionFactory = m_sqlSessionFactory;

		if (sqlSessionFactory == null) {
			synchronized (this) {
				sqlSessionFactory = m_sqlSessionFactory;

				if (sqlSessionFactory == null) {
					if (m_dataSourceManager == null) {
						LOGGER.error("Cannot create fallback MyBatis SqlSessionFactory for mapper {} because "
								+ "DataSourceManager is not injected. Spring mapper is unavailable and legacy "
								+ "Unidal datasource fallback cannot be used.", m_mapperClass.getName());
					}

					sqlSessionFactory = SupportingMyBatisRepository.newSqlSessionFactory(m_dataSourceManager,
							m_mapperClass, m_mapperResource);
					m_sqlSessionFactory = sqlSessionFactory;
				}
			}
		}

		return sqlSessionFactory;
	}
}
