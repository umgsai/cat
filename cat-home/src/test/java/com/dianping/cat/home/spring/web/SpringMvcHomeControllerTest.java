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
		Map<String, Object> model = controller.homeModel(request(Collections.<String, String>emptyMap()));

		Assert.assertEquals("index", model.get("docName"));
		Assert.assertEquals("spring-mvc-migration", model.get("runtime"));
		Assert.assertEquals("/cat/mvc/r/home", model.get("homeUrl"));
		Assert.assertEquals("/cat/mvc/s/login", model.get("loginUrl"));
	}

	@Test
	public void shouldPreserveRequestedDocName() {
		SpringMvcHomeController controller = new SpringMvcHomeController();
		Map<String, Object> model = controller.homeModel(request(Collections.singletonMap("docName", "plugin")));

		Assert.assertEquals("plugin", model.get("docName"));
	}

	@Test
	public void shouldRunCheckpointAction() {
		StubHomeController controller = new StubHomeController();
		Map<String, String> parameters = new java.util.HashMap<String, String>();

		parameters.put("op", "checkpoint");
		Map<String, Object> model = controller.homeModel(request(parameters));

		Assert.assertTrue(controller.isCheckpointCalled());
		Assert.assertEquals("checkpoint", model.get("actionName"));
	}

	@Test
	public void shouldBuildThreadDumpContent() {
		SpringMvcHomeController controller = new SpringMvcHomeController();
		Map<String, String> parameters = new java.util.HashMap<String, String>();

		parameters.put("op", "threadDump");

		Map<String, Object> model = controller.homeModel(request(parameters));

		Assert.assertEquals("threadDump", model.get("actionName"));
		Assert.assertTrue(model.get("content").toString().contains("Threads:"));
	}

	private HttpServletRequest request(Map<String, String> parameters) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getContextPath".equals(method.getName())) {
							return "/cat";
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

	private static class StubHomeController extends SpringMvcHomeController {
		private boolean m_checkpointCalled;

		@Override
		void checkpoint() {
			m_checkpointCalled = true;
		}

		boolean isCheckpointCalled() {
			return m_checkpointCalled;
		}
	}
}
