package com.dianping.cat.mybatis;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class SpringBackedRepositorySupport<T> {
	private final Class<T> mapperClass;

	private final String springMapperMessage;

	private final AtomicBoolean springMapperLogged = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	protected SpringBackedRepositorySupport(Class<T> mapperClass, String mapperResource,
			String springMapperMessage) {
		this.mapperClass = mapperClass;
		this.springMapperMessage = springMapperMessage;
	}

	protected T springMapper(Logger logger) {
		SqlSessionTemplate template = sqlSessionTemplate;

		if (template == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for "
					+ mapperClass.getName() + ".");
		}
		if (springMapperLogged.compareAndSet(false, true)) {
			logger.info(springMapperMessage);
		}
		return template.getMapper(mapperClass);
	}

	protected TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for "
					+ mapperClass.getName() + ".");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}
}
