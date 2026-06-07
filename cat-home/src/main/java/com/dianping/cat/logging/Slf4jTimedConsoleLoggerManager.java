package com.dianping.cat.logging;

import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.logger.TimedConsoleLoggerManager;

public class Slf4jTimedConsoleLoggerManager extends TimedConsoleLoggerManager {

	@Override
	public Logger createLogger(int threshold, String name) {
		return new Slf4jForwardingLogger(super.createLogger(threshold, name));
	}
}
