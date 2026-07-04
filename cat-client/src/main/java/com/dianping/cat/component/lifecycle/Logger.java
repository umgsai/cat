package com.dianping.cat.component.lifecycle;

import com.dianping.cat.apiguardian.api.API;

@API(status = API.Status.INTERNAL, since = "3.1")
public interface Logger {
	void debug(String format, Object... args);

	void debug(Throwable cause, String format, Object... args);

	void error(String format, Object... args);

	void error(Throwable cause, String format, Object... args);

	Level getLevel();

	void info(String format, Object... args);

	void info(Throwable cause, String format, Object... args);

	void setLevel(Level level);

	void warn(String format, Object... args);

	void warn(Throwable cause, String format, Object... args);
	
	public enum Level {
		DEBUG(0),

		INFO(1),

		WARN(2),

		ERROR(3);

		private int m_level;

		private Level(int level) {
			m_level = level;
		}

		public int getLevel() {
			return m_level;
		}

		public boolean isDebugEnabled() {
			return DEBUG.getLevel() >= m_level;
		}

		public boolean isErrorEnabled() {
			return ERROR.getLevel() >= m_level;
		}

		public boolean isInfoEnabled() {
			return INFO.getLevel() >= m_level;
		}

		public boolean isWarnEnabled() {
			return WARN.getLevel() >= m_level;
		}
	}
}
