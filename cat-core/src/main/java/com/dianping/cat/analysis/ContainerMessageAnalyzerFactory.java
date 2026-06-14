package com.dianping.cat.analysis;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ContainerMessageAnalyzerFactory implements MessageAnalyzerFactory, ApplicationContextAware {
	public static final String ANALYZER_BEAN_PREFIX = "messageAnalyzer.";

	private ApplicationContext m_context;

	@Override
	public MessageAnalyzer createAnalyzer(String name) {
		try {
			return getContext().getBean(ANALYZER_BEAN_PREFIX + name, MessageAnalyzer.class);
		} catch (BeansException e) {
			throw new IllegalStateException("Unable to create message analyzer from Spring: " + name, e);
		}
	}

	@Override
	public Map<String, MessageAnalyzer> getAnalyzerMap() {
		try {
			Map<String, MessageAnalyzer> beans = getContext().getBeansOfType(MessageAnalyzer.class);
			Map<String, MessageAnalyzer> analyzers = new java.util.LinkedHashMap<String, MessageAnalyzer>();

			for (Map.Entry<String, MessageAnalyzer> entry : beans.entrySet()) {
				String beanName = entry.getKey();

				if (beanName.startsWith(ANALYZER_BEAN_PREFIX)) {
					analyzers.put(beanName.substring(ANALYZER_BEAN_PREFIX.length()), entry.getValue());
				}
			}
			return analyzers;
		} catch (BeansException e) {
			throw new IllegalStateException("Unable to load message analyzer map from Spring.", e);
		}
	}

	private ApplicationContext getContext() {
		if (m_context == null) {
			throw new IllegalStateException("Spring application context is required for message analyzer factory.");
		}
		return m_context;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_context = applicationContext;
	}
}
