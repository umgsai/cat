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

import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.business.ConfigItem;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
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

	@Test
	public void shouldBuildBusinessItemAddModel() {
		SpringMvcBusinessController controller = new SpringMvcBusinessController();
		BusinessReportConfig config = new BusinessReportConfig();

		config.addBusinessItemConfig(new BusinessItemConfig("slow").setTitle("Slow").setViewOrder(2));
		controller.setProjectService(new StubProjectService("cat"));
		controller.setConfigManager(new StubBusinessConfigManager(config));
		controller.setTagConfigManager(new StubBusinessTagConfigManager());

		Map<String, Object> model = controller.businessModel(request("add", "cat", "/cat", "key", "slow"), "add");
		BusinessItemConfig item = (BusinessItemConfig) model.get("businessItemConfig");

		Assert.assertEquals("slow", item.getId());
		Assert.assertEquals("Slow", item.getTitle());
	}

	@Test
	public void shouldSubmitNewBusinessItemAndReturnListModel() {
		SpringMvcBusinessController controller = new SpringMvcBusinessController();
		BusinessReportConfig config = new BusinessReportConfig().setId("cat");
		StubBusinessConfigManager configManager = new StubBusinessConfigManager(config);

		controller.setProjectService(new StubProjectService("cat"));
		controller.setConfigManager(configManager);
		controller.setTagConfigManager(new StubBusinessTagConfigManager());

		Map<String, Object> model = controller.businessModel(
				request("addSubmit", "cat", "/cat", "businessItemConfig.id", "newKey", "businessItemConfig.title",
						"New Title", "businessItemConfig.viewOrder", "3", "businessItemConfig.showAvg", "true"),
				"addSubmit");

		Assert.assertEquals("Success", model.get("opState"));
		Assert.assertEquals("newKey", configManager.getInsertedKey());
		Assert.assertEquals("New Title", configManager.getInsertedItem().getTitle());
		Assert.assertTrue(configManager.getInsertedItem().isShowAvg());
		Assert.assertNotNull(model.get("configs"));
	}

	@Test
	public void shouldBuildCustomAddAndAlertRuleModels() {
		SpringMvcBusinessController controller = new SpringMvcBusinessController();
		BusinessReportConfig config = new BusinessReportConfig();

		config.addCustomConfig(new CustomConfig("custom").setTitle("Custom").setPattern("${cat,k,AVG}"));
		controller.setProjectService(new StubProjectService("cat"));
		controller.setConfigManager(new StubBusinessConfigManager(config));
		controller.setTagConfigManager(new StubBusinessTagConfigManager());
		controller.setBusinessRuleConfigManager(new StubBusinessRuleConfigManager());
		controller.setRuleDecorator(new StubRuleDecorator());

		Map<String, Object> customModel = controller.businessModel(request("customAdd", "cat", "/cat", "key", "custom"),
				"customAdd");
		Map<String, Object> alertModel = controller.businessModel(
				request("alertRuleAdd", "cat", "/cat", "key", "custom", "attributes", "AVG"), "alertRuleAdd");

		Assert.assertEquals("custom", ((CustomConfig) customModel.get("customConfig")).getId());
		Assert.assertEquals("AVG", alertModel.get("attributes"));
		Assert.assertEquals("<div></div>", alertModel.get("content"));
	}

	@Test
	public void shouldSubmitBusinessAlertRule() {
		SpringMvcBusinessController controller = new SpringMvcBusinessController();
		StubBusinessRuleConfigManager ruleManager = new StubBusinessRuleConfigManager();

		controller.setProjectService(new StubProjectService("cat"));
		controller.setConfigManager(new StubBusinessConfigManager(new BusinessReportConfig()));
		controller.setTagConfigManager(new StubBusinessTagConfigManager());
		controller.setBusinessRuleConfigManager(ruleManager);

		controller.businessModel(
				request("alertRuleAddSubmit", "cat", "/cat", "key", "custom", "attributes", "AVG", "content", "[]"),
				"alertRuleAddSubmit");

		Assert.assertEquals("cat", ruleManager.getDomain());
		Assert.assertEquals("custom", ruleManager.getKey());
		Assert.assertEquals("AVG", ruleManager.getType());
		Assert.assertEquals("[]", ruleManager.getConfigs());
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

		private String m_insertedKey;

		private ConfigItem m_insertedItem;

		StubBusinessConfigManager(BusinessReportConfig config) {
			m_config = config;
		}

		@Override
		public boolean deleteBusinessItem(String domain, String key) {
			m_config.removeBusinessItemConfig(key);
			return true;
		}

		@Override
		public boolean deleteCustomItem(String domain, String key) {
			m_config.removeCustomConfig(key);
			return true;
		}

		String getInsertedKey() {
			return m_insertedKey;
		}

		ConfigItem getInsertedItem() {
			return m_insertedItem;
		}

		@Override
		public boolean insertBusinessConfigIfNotExist(String domain, String key, ConfigItem item) {
			m_insertedKey = key;
			m_insertedItem = item;
			m_config.addBusinessItemConfig(new BusinessItemConfig(key).setTitle(item.getTitle())
					.setShowAvg(item.isShowAvg()).setShowCount(item.isShowCount()).setShowSum(item.isShowSum())
					.setViewOrder(item.getViewOrder()));
			return true;
		}

		@Override
		public BusinessReportConfig queryConfigByDomain(String domain) {
			return m_config;
		}

		@Override
		public boolean updateConfigByDomain(BusinessReportConfig config) {
			return true;
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

	private static class StubBusinessRuleConfigManager extends BusinessRuleConfigManager {
		private String m_configs;

		private String m_domain;

		private String m_key;

		private String m_type;

		String getConfigs() {
			return m_configs;
		}

		String getDomain() {
			return m_domain;
		}

		String getKey() {
			return m_key;
		}

		String getType() {
			return m_type;
		}

		@Override
		public Rule queryRule(String domain, String key, String type) {
			return null;
		}

		@Override
		public void updateRule(String domain, String key, String configsStr, String type) {
			m_domain = domain;
			m_key = key;
			m_configs = configsStr;
			m_type = type;
		}
	}

	private static class StubRuleDecorator extends RuleFTLDecorator {
		@Override
		public String generateConfigsHtml(String templateValue) {
			return "<div>" + templateValue + "</div>";
		}
	}
}
