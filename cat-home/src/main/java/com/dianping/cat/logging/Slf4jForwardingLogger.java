package com.dianping.cat.logging;

import org.codehaus.plexus.logging.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jForwardingLogger implements Logger {

	private final Logger m_delegate;

	private final org.slf4j.Logger m_logger;

	public Slf4jForwardingLogger(Logger delegate) {
		m_delegate = delegate;
		m_logger = LoggerFactory.getLogger(delegate.getName());
	}

	@Override
	public void debug(String message) {
		m_delegate.debug(message);
	}

	@Override
	public void debug(String message, Throwable throwable) {
		m_delegate.debug(message, throwable);
	}

	@Override
	public void error(String message) {
		m_delegate.error(message);
		m_logger.error(message);
	}

	@Override
	public void error(String message, Throwable throwable) {
		m_delegate.error(message, throwable);
		m_logger.error(message, throwable);
	}

	@Override
	public void fatalError(String message) {
		m_delegate.fatalError(message);
		m_logger.error(message);
	}

	@Override
	public void fatalError(String message, Throwable throwable) {
		m_delegate.fatalError(message, throwable);
		m_logger.error(message, throwable);
	}

	@Override
	public Logger getChildLogger(String name) {
		return this;
	}

	@Override
	public String getName() {
		return m_delegate.getName();
	}

	@Override
	public int getThreshold() {
		return m_delegate.getThreshold();
	}

	@Override
	public void info(String message) {
		m_delegate.info(message);
	}

	@Override
	public void info(String message, Throwable throwable) {
		m_delegate.info(message, throwable);
	}

	@Override
	public boolean isDebugEnabled() {
		return m_delegate.isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return m_delegate.isErrorEnabled();
	}

	@Override
	public boolean isFatalErrorEnabled() {
		return m_delegate.isFatalErrorEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return m_delegate.isInfoEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return m_delegate.isWarnEnabled();
	}

	@Override
	public void setThreshold(int threshold) {
		m_delegate.setThreshold(threshold);
	}

	@Override
	public void warn(String message) {
		m_delegate.warn(message);
	}

	@Override
	public void warn(String message, Throwable throwable) {
		m_delegate.warn(message, throwable);
	}
}
