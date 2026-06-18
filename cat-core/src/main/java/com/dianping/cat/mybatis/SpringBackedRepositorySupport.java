package com.dianping.cat.mybatis;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class SpringBackedRepositorySupport<T> {
	private final Class<T> m_mapperClass;

	private final String m_springMapperMessage;

	private final AtomicBoolean m_springMapperLogged = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	protected SpringBackedRepositorySupport(Class<T> mapperClass, String mapperResource,
			String springMapperMessage) {
		m_mapperClass = mapperClass;
		m_springMapperMessage = springMapperMessage;
	}

	protected T springMapper(Logger logger) {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for "
					+ m_mapperClass.getName() + ".");
		}
		if (m_springMapperLogged.compareAndSet(false, true)) {
			logger.info(m_springMapperMessage);
		}
		return sqlSessionTemplate.getMapper(m_mapperClass);
	}

	protected TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for "
					+ m_mapperClass.getName() + ".");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}
}
