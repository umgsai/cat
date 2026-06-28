package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.router.service.CachedRouterConfigService;

public class SpringMvcRouterControllerTest {
	@Test
	public void shouldDefaultToApiAction() {
		SpringMvcRouterController controller = controller();

		Assert.assertEquals("api", controller.action(request(Collections.<String, String>emptyMap())));
		Assert.assertEquals("json", controller.action(request(Collections.singletonMap("op", "json"))));
	}

	@Test
	public void shouldBuildServerString() {
		SpringMvcRouterController controller = controller();

		Assert.assertEquals("10.1.1.1:2280;10.1.1.2:2280;",
				controller.buildServerStr(Arrays.asList(new Server().setId("10.1.1.1").setPort(2280),
						new Server().setId("10.1.1.2").setPort(2280))));
	}

	@Test
	public void shouldBuildRouterConfigWithYesterdayPeriod() {
		SpringMvcRouterController controller = controller();
		StubRouterConfigHandler handler = new StubRouterConfigHandler(true);

		controller.setRouterConfigHandler(handler);

		Assert.assertEquals("true", controller.buildRouterConfig());
		Assert.assertNotNull(handler.getPeriod());
	}

	@Test
	public void shouldReturnFalseWhenRouterBuildFails() {
		SpringMvcRouterController controller = controller();

		controller.setRouterConfigHandler(new StubRouterConfigHandler(false));

		Assert.assertEquals("false", controller.buildRouterConfig());
	}

	private SpringMvcRouterController controller() {
		SpringMvcRouterController controller = new SpringMvcRouterController();

		inject(controller, "cachedRouterConfigService", new CachedRouterConfigService());
		inject(controller, "routerConfigManager", new RouterConfigManager());
		inject(controller, "sampleConfigManager", new SampleConfigManager());
		inject(controller, "serverFilterConfigManager", new ServerFilterConfigManager());
		inject(controller, "routerConfigHandler", new StubRouterConfigHandler(true));
		return controller;
	}

	private void inject(Object target, String field, Object value) {
		try {
			Field declaredField = target.getClass().getDeclaredField(field);

			declaredField.setAccessible(true);
			declaredField.set(target, value);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private HttpServletRequest request(Map<String, String> parameters) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getParameter".equals(method.getName())) {
							return parameters.get(args[0]);
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcRouterControllerTestRequest";
						}
						return null;
					}
				});
	}

	private static class StubRouterConfigHandler extends RouterConfigHandler {
		private final boolean m_result;

		private Date m_period;

		StubRouterConfigHandler(boolean result) {
			m_result = result;
		}

		Date getPeriod() {
			return m_period;
		}

		@Override
		public boolean updateRouterConfig(Date period) {
			m_period = period;
			return m_result;
		}
	}
}
