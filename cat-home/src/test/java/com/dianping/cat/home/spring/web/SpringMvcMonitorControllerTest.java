package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;

public class SpringMvcMonitorControllerTest {
	@Test
	public void shouldKeepLegacyEmptyMonitorResponse() throws Exception {
		SpringMvcMonitorController controller = new SpringMvcMonitorController();
		Headers headers = new Headers();

		controller.monitor(request("count"), response(headers));

		Assert.assertEquals("text/html;charset=UTF-8", headers.contentType);
	}

	private HttpServletRequest request(String action) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getParameter".equals(method.getName()) && "op".equals(args[0])) {
							return action;
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcMonitorControllerTestRequest";
						}
						return null;
					}
				});
	}

	private HttpServletResponse response(Headers headers) {
		return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("setContentType".equals(method.getName())) {
							headers.contentType = (String) args[0];
							return null;
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcMonitorControllerTestResponse";
						}
						return null;
					}
				});
	}

	private static class Headers {
		private String contentType;
	}
}
