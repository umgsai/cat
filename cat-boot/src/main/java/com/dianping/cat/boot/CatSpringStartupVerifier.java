package com.dianping.cat.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

final class CatSpringStartupVerifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatSpringStartupVerifier.class);

	static void log(ApplicationContext applicationContext) {
		LOGGER.info("Spring context initialized, id={}, cat.home={}, cat.log.path={}, server.port={}",
				applicationContext.getId(), System.getProperty("cat.home"), System.getProperty("cat.log.path"),
				System.getProperty("server.port", "8080"));
	}

	private CatSpringStartupVerifier() {
	}
}
