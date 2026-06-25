package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.alarm.rule.entity.MetricItem;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.heartbeat.entity.Group;
import com.dianping.cat.home.heartbeat.entity.HeartbeatDisplayPolicy;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class SpringMvcConfigControllerTest {
	@Test
	public void shouldDefaultToProjectsAction() {
		SpringMvcConfigController controller = new SpringMvcConfigController();

		Assert.assertEquals("projects", controller.action(request("/cat")));
		Assert.assertEquals("displayPolicy", controller.action(request("/cat", "op", "displayPolicy")));
	}

	@Test
	public void shouldBuildTransactionRuleListModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubTransactionRuleConfigManager manager = new StubTransactionRuleConfigManager();
		Map<String, Object> model;

		manager.addRule(new Rule("cat;URL;All;count"));
		controller.setTransactionRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "transactionRule"), "transactionRule");

		Rule rule = ((Collection<Rule>) model.get("rules")).iterator().next();

		Assert.assertEquals(Boolean.TRUE, rule.getAvailable());
		Assert.assertNull(model.get("opState"));
	}

	@Test
	public void shouldBuildEventRuleListModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubEventRuleConfigManager manager = new StubEventRuleConfigManager();
		Map<String, Object> model;

		manager.addRule(new Rule("cat;URL;All;count"));
		controller.setEventRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "eventRule"), "eventRule");

		Rule rule = ((Collection<Rule>) model.get("rules")).iterator().next();

		Assert.assertEquals(Boolean.TRUE, rule.getAvailable());
		Assert.assertNull(model.get("opState"));
	}

	@Test
	public void shouldBuildEventRuleUpdateModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubEventRuleConfigManager manager = new StubEventRuleConfigManager();
		Rule rule = new Rule("cat;URL;All;failRatio").setAvailable(false);
		Map<String, Object> model;

		rule.addConfig(new Config().setStarttime("00:00").setEndtime("24:00"));
		manager.addRule(rule);
		controller.setEventRuleConfigManager(manager);
		controller.setRuleDecorator(new StubRuleDecorator());
		model = controller.configModel(request("/cat", "op", "eventRuleUpdate", "ruleId", "cat;URL;All;failRatio"),
				"eventRuleUpdate");

		Assert.assertEquals("cat;URL;All;failRatio", model.get("ruleId"));
		Assert.assertEquals(Boolean.FALSE, model.get("available"));
		Assert.assertTrue(model.get("content").toString().contains("\"starttime\":\"00:00\""));
	}

	@Test
	public void shouldBuildHeartbeatRuleListModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubHeartbeatRuleConfigManager manager = new StubHeartbeatRuleConfigManager();
		Map<String, Object> model;

		manager.addRule(new Rule("cat;DescVal").setAvailable(null));
		controller.setHeartbeatRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "heartbeatRuleConfigList"), "heartbeatRuleConfigList");

		Rule rule = ((Collection<Rule>) model.get("rules")).iterator().next();

		Assert.assertEquals(Boolean.TRUE, rule.getAvailable());
		Assert.assertNull(model.get("opState"));
	}

	@Test
	public void shouldBuildHeartbeatRuleUpdateModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubHeartbeatRuleConfigManager manager = new StubHeartbeatRuleConfigManager();
		Rule rule = new Rule("cat;DescVal").setAvailable(false);
		Map<String, Object> model;

		rule.addMetricItem(new MetricItem().setProductText("cat").setMetricItemText("DescVal").setMonitorCount(true));
		rule.addConfig(new Config().setStarttime("00:00").setEndtime("24:00"));
		manager.addRule(rule);
		controller.setHeartbeatRuleConfigManager(manager);
		controller.setDisplayPolicyManager(new StubDisplayPolicyManager());
		controller.setRuleDecorator(new StubRuleDecorator());
		model = controller.configModel(request("/cat", "op", "heartbeatRuleUpdate", "ruleId", "cat;DescVal"),
				"heartbeatRuleUpdate");

		Assert.assertEquals("cat;DescVal", model.get("ruleId"));
		Assert.assertEquals(Boolean.FALSE, model.get("available"));
		Assert.assertTrue(model.get("content").toString().contains("\"starttime\":\"00:00\""));
		Assert.assertTrue(model.get("configHeader").toString().contains("\"productText\":\"cat\""));
		Assert.assertTrue(((Collection<String>) model.get("heartbeatExtensionMetrics")).contains("System:Heap"));
	}

	@Test
	public void shouldSubmitHeartbeatRule() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubHeartbeatRuleConfigManager manager = new StubHeartbeatRuleConfigManager();
		Map<String, Object> model;

		controller.setHeartbeatRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "heartbeatRuleSubmit", "ruleId", "cat;DescVal",
				"configs", "[]", "metrics", "[]", "available", "false"), "heartbeatRuleSubmit");

		Assert.assertEquals("cat;DescVal", manager.getUpdatedId());
		Assert.assertEquals("[]", manager.getUpdatedConfigs());
		Assert.assertEquals("[]", manager.getUpdatedMetrics());
		Assert.assertEquals(Boolean.FALSE, manager.getUpdatedAvailable());
		Assert.assertEquals(Boolean.TRUE, model.get("opState"));
	}

	@Test
	public void shouldDeleteHeartbeatRule() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubHeartbeatRuleConfigManager manager = new StubHeartbeatRuleConfigManager();
		Rule rule = new Rule("cat;DescVal");
		Map<String, Object> model;

		manager.addRule(rule);
		controller.setHeartbeatRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "heartbeatRulDelete", "ruleId", "cat;DescVal"),
				"heartbeatRulDelete");

		Assert.assertNull(manager.queryRule("cat;DescVal"));
		Assert.assertEquals(Boolean.TRUE, model.get("opState"));
	}

	@Test
	public void shouldSubmitEventRule() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubEventRuleConfigManager manager = new StubEventRuleConfigManager();
		Map<String, Object> model;

		controller.setEventRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "eventRuleSubmit", "ruleId", "cat;URL;All;count", "configs",
				"[]", "available", "false"), "eventRuleSubmit");

		Assert.assertEquals("cat;URL;All;count", manager.getUpdatedId());
		Assert.assertEquals("[]", manager.getUpdatedConfigs());
		Assert.assertEquals(Boolean.FALSE, manager.getUpdatedAvailable());
		Assert.assertEquals(Boolean.TRUE, model.get("opState"));
	}

	@Test
	public void shouldDeleteEventRule() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubEventRuleConfigManager manager = new StubEventRuleConfigManager();
		Rule rule = new Rule("cat;URL;All;count");
		Map<String, Object> model;

		manager.addRule(rule);
		controller.setEventRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "eventRuleDelete", "ruleId", "cat;URL;All;count"),
				"eventRuleDelete");

		Assert.assertNull(manager.queryRule("cat;URL;All;count"));
		Assert.assertEquals(Boolean.TRUE, model.get("opState"));
	}

	@Test
	public void shouldBuildTransactionRuleUpdateModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubTransactionRuleConfigManager manager = new StubTransactionRuleConfigManager();
		Rule rule = new Rule("cat;URL;All;avg").setAvailable(false);
		Map<String, Object> model;

		rule.addConfig(new Config().setStarttime("00:00").setEndtime("24:00"));
		manager.addRule(rule);
		controller.setTransactionRuleConfigManager(manager);
		controller.setRuleDecorator(new StubRuleDecorator());
		model = controller.configModel(request("/cat", "op", "transactionRuleUpdate", "ruleId", "cat;URL;All;avg"),
				"transactionRuleUpdate");

		Assert.assertEquals("cat;URL;All;avg", model.get("ruleId"));
		Assert.assertEquals(Boolean.FALSE, model.get("available"));
		Assert.assertTrue(model.get("content").toString().contains("\"starttime\":\"00:00\""));
	}

	@Test
	public void shouldSubmitTransactionRule() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubTransactionRuleConfigManager manager = new StubTransactionRuleConfigManager();
		Map<String, Object> model;

		controller.setTransactionRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "transactionRuleSubmit", "ruleId", "cat;URL;All;count",
				"configs", "[]", "available", "false"), "transactionRuleSubmit");

		Assert.assertEquals("cat;URL;All;count", manager.getUpdatedId());
		Assert.assertEquals("[]", manager.getUpdatedConfigs());
		Assert.assertEquals(Boolean.FALSE, manager.getUpdatedAvailable());
		Assert.assertEquals(Boolean.TRUE, model.get("opState"));
	}

	@Test
	public void shouldBuildDisplayPolicyModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubDisplayPolicyManager displayPolicyManager = new StubDisplayPolicyManager();
		Map<String, Object> model;

		controller.setDisplayPolicyManager(displayPolicyManager);
		controller.setConfigHtmlParser(new ConfigHtmlParser());
		model = controller.configModel(request("/cat", "op", "displayPolicy"), "displayPolicy");

		Assert.assertEquals("/cat/mvc/s/config", model.get("configUrl"));
		Assert.assertTrue(model.get("content").toString().contains("&lt;heartbeat-display-policy&gt;"));
		Assert.assertTrue(model.get("content").toString().contains("&lt;?xml version=\"1.0\" encoding=\"utf-8\"?&gt;"));
		Assert.assertNull(model.get("opState"));
	}

	@Test
	public void shouldBuildExceptionListModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubExceptionRuleConfigManager manager = new StubExceptionRuleConfigManager();
		Map<String, Object> model;

		manager.addLimit(new ExceptionLimit().setDomain("cat").setName("java.lang.Exception").setWarning(1).setError(2));
		manager.addExclude(new ExceptionExclude().setDomain("cat").setName("ignore"));
		controller.setExceptionRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "exception"), "exception");

		Assert.assertEquals(1, ((Collection<?>) model.get("exceptionLimits")).size());
		Assert.assertEquals(1, ((Collection<?>) model.get("exceptionExcludes")).size());
		Assert.assertNull(model.get("opState"));
	}

	@Test
	public void shouldBuildExceptionThresholdUpdateModel() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubExceptionRuleConfigManager manager = new StubExceptionRuleConfigManager();
		ExceptionLimit limit = new ExceptionLimit().setDomain("cat").setName("java.lang.Exception").setWarning(1)
				.setError(2).setAvailable(false);
		Map<String, Object> model;

		manager.addLimit(limit);
		controller.setProjectService(new StubProjectService());
		controller.setExceptionRuleConfigManager(manager);
		model = controller.configModel(request("/cat", "op", "exceptionThresholdUpdate", "domain", "cat", "exception",
				"java.lang.Exception"), "exceptionThresholdUpdate");

		Assert.assertEquals("cat", ((ExceptionLimit) model.get("exceptionLimit")).getDomain());
		Assert.assertEquals(Boolean.FALSE, ((ExceptionLimit) model.get("exceptionLimit")).getAvailable());
	}

	@Test
	public void shouldSubmitDisplayPolicyContent() {
		SpringMvcConfigController controller = new SpringMvcConfigController();
		StubDisplayPolicyManager displayPolicyManager = new StubDisplayPolicyManager();
		Map<String, Object> model;

		controller.setDisplayPolicyManager(displayPolicyManager);
		controller.setConfigHtmlParser(new ConfigHtmlParser());
		model = controller.configModel(request("/cat", "op", "displayPolicy", "content", "<policy/>", "submit", "提交"),
				"displayPolicy");

		Assert.assertEquals("<policy/>", displayPolicyManager.getInserted());
		Assert.assertEquals(Boolean.TRUE, model.get("opState"));
	}

	private HttpServletRequest request(String contextPath, String... parameters) {
		Map<String, String> values = new HashMap<String, String>();

		for (int i = 0; i < parameters.length; i += 2) {
			values.put(parameters[i], parameters[i + 1]);
		}
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getParameter".equals(method.getName())) {
							return values.get(args[0]);
						}
						if ("getContextPath".equals(method.getName())) {
							return contextPath;
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcConfigControllerTestRequest";
						}
						return null;
					}
				});
	}

	private static class StubDisplayPolicyManager extends HeartbeatDisplayPolicyManager {
		private String m_inserted;

		@Override
		public HeartbeatDisplayPolicy getHeartbeatDisplayPolicy() {
			return new HeartbeatDisplayPolicy().addGroup(new Group("System").setOrder(1));
		}

		@Override
		public boolean insert(String xml) {
			m_inserted = xml;
			return true;
		}

		String getInserted() {
			return m_inserted;
		}
	}

	private static class StubHeartbeatRuleConfigManager extends HeartbeatRuleConfigManager {
		private final MonitorRules m_rules = new MonitorRules();

		private String m_updatedId;

		private String m_updatedMetrics;

		private String m_updatedConfigs;

		private Boolean m_updatedAvailable;

		void addRule(Rule rule) {
			m_rules.addRule(rule);
		}

		@Override
		public MonitorRules getMonitorRules() {
			return m_rules;
		}

		@Override
		public Rule queryRule(String key) {
			return m_rules.findRule(key);
		}

		@Override
		public String updateRule(String id, String metricsStr, String configsStr, Boolean available) {
			m_updatedId = id;
			m_updatedMetrics = metricsStr;
			m_updatedConfigs = configsStr;
			m_updatedAvailable = available;
			m_rules.addRule(new Rule(id).setAvailable(available));
			return m_rules.toString();
		}

		@Override
		public String deleteRule(String key) {
			m_rules.removeRule(key);
			return m_rules.toString();
		}

		@Override
		public boolean insert(String xml) {
			return true;
		}

		String getUpdatedId() {
			return m_updatedId;
		}

		String getUpdatedMetrics() {
			return m_updatedMetrics;
		}

		String getUpdatedConfigs() {
			return m_updatedConfigs;
		}

		Boolean getUpdatedAvailable() {
			return m_updatedAvailable;
		}
	}

	private static class StubRuleDecorator extends RuleFTLDecorator {
		@Override
		public String generateConfigsHtml(String templateValue) {
			return "<div>" + templateValue + "</div>";
		}
	}

	private static class StubTransactionRuleConfigManager extends TransactionRuleConfigManager {
		private final MonitorRules m_rules = new MonitorRules();

		private String m_updatedId;

		private String m_updatedConfigs;

		private Boolean m_updatedAvailable;

		void addRule(Rule rule) {
			m_rules.addRule(rule);
		}

		@Override
		public MonitorRules getMonitorRules() {
			return m_rules;
		}

		@Override
		public Rule queryRule(String key) {
			return m_rules.findRule(key);
		}

		@Override
		public String updateRule(String id, String metricsStr, String configsStr, Boolean available) {
			m_updatedId = id;
			m_updatedConfigs = configsStr;
			m_updatedAvailable = available;
			m_rules.addRule(new Rule(id).setAvailable(available));
			return m_rules.toString();
		}

		@Override
		public String deleteRule(String key) {
			m_rules.removeRule(key);
			return m_rules.toString();
		}

		@Override
		public boolean insert(String xml) {
			return true;
		}

		String getUpdatedId() {
			return m_updatedId;
		}

		String getUpdatedConfigs() {
			return m_updatedConfigs;
		}

		Boolean getUpdatedAvailable() {
			return m_updatedAvailable;
		}
	}

	private static class StubEventRuleConfigManager extends EventRuleConfigManager {
		private final MonitorRules m_rules = new MonitorRules();

		private String m_updatedId;

		private String m_updatedConfigs;

		private Boolean m_updatedAvailable;

		void addRule(Rule rule) {
			m_rules.addRule(rule);
		}

		@Override
		public MonitorRules getMonitorRules() {
			return m_rules;
		}

		@Override
		public Rule queryRule(String key) {
			return m_rules.findRule(key);
		}

		@Override
		public String updateRule(String id, String metricsStr, String configsStr, Boolean available) {
			m_updatedId = id;
			m_updatedConfigs = configsStr;
			m_updatedAvailable = available;
			m_rules.addRule(new Rule(id).setAvailable(available));
			return m_rules.toString();
		}

		@Override
		public String deleteRule(String key) {
			m_rules.removeRule(key);
			return m_rules.toString();
		}

		@Override
		public boolean insert(String xml) {
			return true;
		}

		String getUpdatedId() {
			return m_updatedId;
		}

		String getUpdatedConfigs() {
			return m_updatedConfigs;
		}

		Boolean getUpdatedAvailable() {
			return m_updatedAvailable;
		}
	}

	private static class StubProjectService extends ProjectService {
		@Override
		public List<Project> findAll() {
			List<Project> projects = new ArrayList<Project>();

			projects.add(new Project().setDomain("cat"));
			return projects;
		}
	}

	private static class StubExceptionRuleConfigManager extends ExceptionRuleConfigManager {
		private final Map<String, ExceptionLimit> m_limits = new HashMap<String, ExceptionLimit>();
		private final Map<String, ExceptionExclude> m_excludes = new HashMap<String, ExceptionExclude>();

		void addLimit(ExceptionLimit limit) {
			m_limits.put(limit.getDomain() + ":" + limit.getName(), limit);
		}

		void addExclude(ExceptionExclude exclude) {
			m_excludes.put(exclude.getDomain() + ":" + exclude.getName(), exclude);
		}

		@Override
		public List<ExceptionLimit> queryAllExceptionLimits() {
			return new java.util.ArrayList<ExceptionLimit>(m_limits.values());
		}

		@Override
		public List<ExceptionExclude> queryAllExceptionExcludes() {
			return new java.util.ArrayList<ExceptionExclude>(m_excludes.values());
		}

		@Override
		public ExceptionLimit queryExceptionLimit(String domain, String exceptionName) {
			return m_limits.get(domain + ":" + exceptionName);
		}

		@Override
		public boolean insertExceptionLimit(ExceptionLimit limit) {
			m_limits.put(limit.getDomain() + ":" + limit.getName(), limit);
			return true;
		}

		@Override
		public boolean insertExceptionExclude(ExceptionExclude exclude) {
			m_excludes.put(exclude.getDomain() + ":" + exclude.getName(), exclude);
			return true;
		}

		@Override
		public boolean deleteExceptionLimit(String domain, String exceptionName) {
			m_limits.remove(domain + ":" + exceptionName);
			return true;
		}

		@Override
		public boolean deleteExceptionExclude(String domain, String exceptionName) {
			m_excludes.remove(domain + ":" + exceptionName);
			return true;
		}
	}
}
