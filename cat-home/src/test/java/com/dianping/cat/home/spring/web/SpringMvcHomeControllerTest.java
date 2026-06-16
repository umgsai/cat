package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

public class SpringMvcHomeControllerTest {
	@Test
	public void shouldBuildDefaultHomeModel() {
		SpringMvcHomeController controller = new SpringMvcHomeController();
		Map<String, Object> model = controller.homeModel(request("/cat", Collections.<String, String>emptyMap()));

		Assert.assertEquals("index", model.get("docName"));
		Assert.assertEquals("spring-mvc-migration", model.get("runtime"));
		Assert.assertEquals("/cat/r/home", model.get("legacyHomeUrl"));
		Assert.assertEquals("/cat/mvc/s/login", model.get("loginUrl"));
	}

	@Test
	public void shouldPreserveRequestedDocName() {
		SpringMvcHomeController controller = new SpringMvcHomeController();
		Map<String, Object> model = controller.homeModel(request("/cat", Collections.singletonMap("docName", "plugin")));

		Assert.assertEquals("plugin", model.get("docName"));
	}

	private HttpServletRequest request(String contextPath, Map<String, String> parameters) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getContextPath".equals(method.getName())) {
							return contextPath;
						}
						if ("getParameter".equals(method.getName())) {
							return parameters.get(args[0]);
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcHomeControllerTestRequest";
						}
						return null;
					}
				});
	}
}
