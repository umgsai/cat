package com.dianping.cat.spring;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public final class CatSpringContext {
	private static volatile ApplicationContext s_context;

	private CatSpringContext() {
	}

	public static <T> T getBean(Class<T> type) {
		ApplicationContext context = s_context;

		return context == null ? null : context.getBean(type);
	}

	public static <T> T getBeanIfAvailable(Class<T> type) {
		ApplicationContext context = s_context;

		if (context == null) {
			return null;
		}
		try {
			return context.getBean(type);
		} catch (BeansException e) {
			return null;
		}
	}

	public static <T> T getBeanIfAvailable(String name, Class<T> type) {
		ApplicationContext context = s_context;

		if (context == null) {
			return null;
		}
		try {
			return context.getBean(name, type);
		} catch (BeansException e) {
			return null;
		}
	}

	public static <T> Map<String, T> getBeansIfAvailable(Class<T> type) {
		ApplicationContext context = s_context;

		if (context == null) {
			return Collections.emptyMap();
		}
		try {
			return context.getBeansOfType(type);
		} catch (BeansException e) {
			return Collections.emptyMap();
		}
	}

	public static void setContext(ApplicationContext context) {
		s_context = context;
	}

	public static void clear(ApplicationContext context) {
		if (s_context == context) {
			s_context = null;
		}
	}
}
