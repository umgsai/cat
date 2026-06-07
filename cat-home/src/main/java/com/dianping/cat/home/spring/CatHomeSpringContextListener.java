package com.dianping.cat.home.spring;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.dianping.cat.spring.CatSpringContext;

public class CatHomeSpringContextListener implements ServletContextListener {
	public static final String ATTRIBUTE_NAME = CatHomeSpringContextListener.class.getName() + ".context";

	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeSpringContextListener.class);

	private AnnotationConfigApplicationContext m_context;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

		context.register(CatHomeSpringConfiguration.class);
		context.refresh();
		event.getServletContext().setAttribute(ATTRIBUTE_NAME, context);
		CatSpringContext.setContext(context);
		m_context = context;
		LOGGER.info("CAT home Spring context initialized, beanCount={}.", context.getBeanDefinitionCount());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		event.getServletContext().removeAttribute(ATTRIBUTE_NAME);

		if (m_context != null) {
			CatSpringContext.clear(m_context);
			m_context.close();
			LOGGER.info("CAT home Spring context closed.");
		}
	}
}
