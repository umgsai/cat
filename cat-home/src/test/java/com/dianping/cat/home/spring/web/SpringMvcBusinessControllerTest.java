package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.Context;
import com.dianping.cat.system.page.business.Model;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;

public class SpringMvcBusinessControllerTest {
	@Test
	public void shouldDefaultToListAction() {
		SpringMvcBusinessController controller = new SpringMvcBusinessController();

		Assert.assertEquals("list", controller.action(request(null, null, "/cat")));
		Assert.assertEquals("delete", controller.action(request("delete", null, "/cat")));
	}

	@Test
	public void shouldBuildReadonlyBusinessModel() {
		SpringMvcBusinessController controller = new SpringMvcBusinessController();
		BusinessReportConfig config = new BusinessReportConfig();

		config.setId("cat");
		config.addBusinessItemConfig(new BusinessItemConfig("slow").setTitle("Slow").setViewOrder(2));
		config.addBusinessItemConfig(new BusinessItemConfig("fast").setTitle("Fast").setViewOrder(1));
		config.addCustomConfig(new CustomConfig("custom").setTitle("Custom").setViewOrder(3));
		controller.setProjectService(new StubProjectService("cat", "mobile-api"));
		controller.setConfigManager(new StubBusinessConfigManager(config));
		controller.setTagConfigManager(new StubBusinessTagConfigManager());

		Context context = controller.businessContext(request("list", "cat", "/cat"), response());
		Model model = controller.businessModel(context);

		Assert.assertEquals("cat", context.getPayload().getDomain());
		Assert.assertEquals("list", context.getPayload().getAction().getName());
		Assert.assertEquals("/cat/mvc/s/business", model.getPageUri());
		Assert.assertEquals("fast", model.getConfigs().get(0).getId());
		Assert.assertEquals("custom", model.getCustomConfigs().get(0).getId());
		Assert.assertTrue(model.getDomains().contains("mobile-api"));
	}

	private HttpServletRequest request(String action, String domain, String contextPath) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getParameter".equals(method.getName())) {
							if ("op".equals(args[0])) {
								return action;
							}
							if ("domain".equals(args[0])) {
								return domain;
							}
						}
						if ("getContextPath".equals(method.getName())) {
							return contextPath;
						}
						if ("getQueryString".equals(method.getName())) {
							return "op=list&domain=" + domain;
						}
						if ("getSession".equals(method.getName())) {
							return session();
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcBusinessControllerTestRequest";
						}
						return null;
					}
				});
	}

	private HttpServletResponse response() {
		return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("toString".equals(method.getName())) {
							return "SpringMvcBusinessControllerTestResponse";
						}
						return null;
					}
				});
	}

	private HttpSession session() {
		return (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { HttpSession.class },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getServletContext".equals(method.getName())) {
							return servletContext();
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcBusinessControllerTestSession";
						}
						return null;
					}
				});
	}

	private ServletContext servletContext() {
		return (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { ServletContext.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getRealPath".equals(method.getName())) {
							return "D:/workspace/cat/cat-home/src/main/webapp";
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcBusinessControllerTestServletContext";
						}
						return null;
					}
				});
	}

	private static class StubProjectService extends ProjectService {
		private final Set<String> m_domains;

		StubProjectService(String... domains) {
			m_domains = new HashSet<String>(Arrays.asList(domains));
		}

		@Override
		public Set<String> findAllDomains() {
			return m_domains;
		}
	}

	private static class StubBusinessConfigManager extends BusinessConfigManager {
		private final BusinessReportConfig m_config;

		StubBusinessConfigManager(BusinessReportConfig config) {
			m_config = config;
		}

		@Override
		public BusinessReportConfig queryConfigByDomain(String domain) {
			return m_config;
		}
	}

	private static class StubBusinessTagConfigManager extends BusinessTagConfigManager {
		@Override
		public Map<String, Set<String>> findTagByDomain(String domain) {
			return Collections.emptyMap();
		}
	}
}
