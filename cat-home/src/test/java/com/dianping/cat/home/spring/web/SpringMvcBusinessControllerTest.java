package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

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

		Map<String, Object> model = controller.businessModel(request("list", "cat", "/cat"), "list");
		List<BusinessItemConfig> configs = (List<BusinessItemConfig>) model.get("configs");
		List<CustomConfig> customConfigs = (List<CustomConfig>) model.get("customConfigs");

		Assert.assertEquals("cat", model.get("domain"));
		Assert.assertEquals("list", model.get("actionName"));
		Assert.assertEquals("/cat/mvc/s/business", model.get("businessUrl"));
		Assert.assertEquals("fast", configs.get(0).getId());
		Assert.assertEquals("custom", customConfigs.get(0).getId());
		Assert.assertTrue(((Set<String>) model.get("domains")).contains("mobile-api"));
	}

	@Test
	public void shouldBuildAndSubmitBusinessTagConfigModel() {
		SpringMvcBusinessController controller = new SpringMvcBusinessController();
		StubBusinessTagConfigManager tagConfigManager = new StubBusinessTagConfigManager();
		Map<String, Object> model;

		controller.setProjectService(new StubProjectService("cat"));
		controller.setConfigManager(new StubBusinessConfigManager(new BusinessReportConfig()));
		controller.setTagConfigManager(tagConfigManager);
		controller.setConfigHtmlParser(new ConfigHtmlParser());
		model = controller.businessModel(request("tagConfig", null, "/cat", "content", "<business-tag-config/>"),
				"tagConfig");

		Assert.assertEquals("tagConfig", model.get("actionName"));
		Assert.assertEquals("<business-tag-config/>", tagConfigManager.getStored());
		Assert.assertEquals("Success", model.get("opState"));
		Assert.assertTrue(model.get("content").toString().contains("&lt;business-tag-config"));
	}

	private HttpServletRequest request(String action, String domain, String contextPath) {
		return request(action, domain, contextPath, new String[0]);
	}

	private HttpServletRequest request(String action, String domain, String contextPath, String... parameters) {
		Map<String, String> values = new HashMap<String, String>();

		for (int i = 0; i < parameters.length; i += 2) {
			values.put(parameters[i], parameters[i + 1]);
		}
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
							return values.get(args[0]);
						}
						if ("getContextPath".equals(method.getName())) {
							return contextPath;
						}
						if ("getQueryString".equals(method.getName())) {
							return "op=list&domain=" + domain;
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcBusinessControllerTestRequest";
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
		private String m_stored;

		@Override
		public Map<String, Set<String>> findTagByDomain(String domain) {
			return Collections.emptyMap();
		}

		@Override
		public BusinessTagConfig getConfig() {
			return new BusinessTagConfig();
		}

		@Override
		public boolean store(String xml) {
			m_stored = xml;
			return true;
		}

		String getStored() {
			return m_stored;
		}
	}
}
