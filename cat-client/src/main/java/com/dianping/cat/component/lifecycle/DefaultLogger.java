package com.dianping.cat.component.lifecycle;

import com.dianping.cat.apiguardian.api.API;
import org.slf4j.LoggerFactory;

@API(status = API.Status.INTERNAL, since = "3.1")
public class DefaultLogger implements Logger {
	private Output m_output = new ConsoleOutput();

	private Level m_level = Level.INFO;

	@Override
	public void debug(String format, Object... args) {
		if (m_level.isDebugEnabled()) {
			if (args.length == 0) {
				m_output.write(Level.DEBUG, format, null);
			} else {
				m_output.write(Level.DEBUG, String.format(format, args), null);
			}
		}
	}

	@Override
	public void debug(Throwable cause, String format, Object... args) {
		if (m_level.isDebugEnabled()) {
			if (args.length == 0) {
				m_output.write(Level.DEBUG, format, cause);
			} else {
				m_output.write(Level.DEBUG, String.format(format, args), cause);
			}
		}
	}

	@Override
	public void error(String format, Object... args) {
		if (m_level.isErrorEnabled()) {
			if (args.length == 0) {
				m_output.write(Level.ERROR, format, null);
			} else {
				m_output.write(Level.ERROR, String.format(format, args), null);
			}
		}
	}

	@Override
	public void error(Throwable cause, String format, Object... args) {
		if (m_level.isErrorEnabled()) {
			if (args.length == 0) {
				m_output.write(Level.ERROR, format, cause);
			} else {
				m_output.write(Level.ERROR, String.format(format, args), cause);
			}
		}
	}

	@Override
	public Level getLevel() {
		return m_level;
	}

	@Override
	public void info(String format, Object... args) {
		if (m_level.isInfoEnabled()) {
			if (args.length == 0) {
				m_output.write(Level.INFO, format, null);
			} else {
				m_output.write(Level.INFO, String.format(format, args), null);
			}
		}
	}

	@Override
	public void info(Throwable cause, String format, Object... args) {
		if (m_level.isInfoEnabled()) {
			if (args.length == 0) {
				m_output.write(Level.INFO, format, cause);
			} else {
				m_output.write(Level.INFO, String.format(format, args), cause);
			}
		}
	}

	public DefaultLogger output(Output output) {
		m_output = output;
		return this;
	}

	@Override
	public void setLevel(Level level) {
		m_level = level;
	}

	@Override
	public void warn(String format, Object... args) {
		if (m_level.isWarnEnabled()) {
			if (args.length == 0) {
				m_output.write(Level.WARN, format, null);
			} else {
				m_output.write(Level.WARN, String.format(format, args), null);
			}
		}
	}

	@Override
	public void warn(Throwable cause, String format, Object... args) {
		if (m_level.isWarnEnabled()) {
			if (args.length == 0) {
				m_output.write(Level.WARN, format, cause);
			} else {
				m_output.write(Level.WARN, String.format(format, args), cause);
			}
		}
	}

	public static class ConsoleOutput implements Output {
		private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("com.dianping.cat.client");

		@Override
		public void write(Level level, String message, Throwable cause) {
			try {
				if (level == Level.ERROR) {
					LOGGER.error(message, cause);
				} else if (level == Level.WARN) {
					LOGGER.warn(message, cause);
				} else if (cause != null) {
					LOGGER.warn(message, cause);
				} else if (level == Level.INFO) {
					LOGGER.info(message);
				} else {
					LOGGER.debug(message);
				}
			} catch (Throwable e) {
				// ignore it
			}
		}
	}

	public static interface Output {
		public void write(Level level, String message, Throwable cause);
	}
}
