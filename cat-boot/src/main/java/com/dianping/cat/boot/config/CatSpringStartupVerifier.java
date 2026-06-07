package com.dianping.cat.boot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CatSpringStartupVerifier implements ApplicationRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatSpringStartupVerifier.class);

	private final ApplicationContext m_applicationContext;

	public CatSpringStartupVerifier(ApplicationContext applicationContext) {
		m_applicationContext = applicationContext;
	}

	@Override
	public void run(org.springframework.boot.ApplicationArguments args) {
		LOGGER.info("Spring context initialized, id={}, cat.home={}, cat.log.path={}, server.port={}",
				m_applicationContext.getId(), System.getProperty("cat.home"), System.getProperty("cat.log.path"),
				System.getProperty("server.port", "8080"));
	}
}
