package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

public class SpringMvcPermissionControllerTest {
	@Test
	public void shouldDefaultToUserAction() {
		SpringMvcPermissionController controller = new SpringMvcPermissionController();

		Assert.assertEquals("user", controller.action(request(null)));
		Assert.assertEquals("resource", controller.action(request("resource")));
		Assert.assertEquals("error", controller.action(request("error")));
	}

	@Test
	public void shouldResolveJspByAction() {
		SpringMvcPermissionController controller = new SpringMvcPermissionController();

		Assert.assertEquals("/jsp/spring/system/permission/userConfigUpdate.jsp", controller.jsp("user"));
		Assert.assertEquals("/jsp/spring/system/permission/resourceConfigUpdate.jsp", controller.jsp("resource"));
		Assert.assertEquals("/jsp/spring/system/permission/error.jsp", controller.jsp("error"));
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
							return "SpringMvcPermissionControllerTestRequest";
						}
						return null;
					}
				});
	}
}
