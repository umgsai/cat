package com.dianping.cat.core.mybatis.repository;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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
		return SupportingMyBatisRepository.springMapper(m_mapperClass, logger, m_springMapperLogged,
				m_springMapperMessage);
	}

	protected TransactionTemplate springTransactionTemplate() {
		return SupportingMyBatisRepository.springTransactionTemplate();
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
