package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.Constants;
import com.dianping.cat.config.business.ConfigItem;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.login.service.Session;
import com.dianping.cat.system.page.login.service.SigninContext;
import com.dianping.cat.system.page.login.service.SigninService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SpringMvcBusinessController {
	@Resource
	private ProjectService projectService;

	@Resource
	private BusinessConfigManager businessConfigManager;

	@Resource
	private BusinessTagConfigManager businessTagConfigManager;

	@Resource
	private BusinessRuleConfigManager businessRuleConfigManager;

	@Resource
	private RuleFTLDecorator ruleDecorator;

	@Resource
	private ConfigHtmlParser configHtmlParser;

	@Resource
	private SigninService signinService;

	@RequestMapping(value = "/s/business", method = { RequestMethod.GET, RequestMethod.POST })
	public void business(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = action(request);
		Session session = signinService.validate(new SigninContext(request, response));

		if (session == null && "vueData".equals(action)) {
			writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, JSON.toJSONString(vueUnauthorized(request)));
			return;
		}

		if ("vueData".equals(action)) {
			writeJson(response, JSON.toJSONString(vueBusinessConfig(request)));
			return;
		}

		if (!isSupported(action)) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		Map<String, Object> model = businessModel(request, action);

		if (isVueMutationRequest(request, action)) {
			response.sendRedirect(vueBusinessConfigUrl(request, vueConfigAction(action), domain(request),
					String.valueOf(model.get("opState"))));
			return;
		}

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(jsp(action));

		dispatcher.forward(request, response);
	}

	String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		if (action == null || action.length() == 0) {
			return "list";
		}
		return action;
	}

	Map<String, Object> businessModel(HttpServletRequest request, String action) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String domain = domain(request);
		BusinessReportConfig config = businessConfigManager.queryConfigByDomain(domain);

		model.put("contextPath", request.getContextPath());
		model.put("actionName", action);
		model.put("domain", domain);
		model.put("businessUrl", request.getContextPath() + "/mvc/s/business");
		model.put("domains", projectService.findAllDomains());
		if ("tagConfig".equals(action)) {
			String tagConfig = request.getParameter("content");

			if (tagConfig != null && tagConfig.length() > 0) {
				model.put("opState", businessTagConfigManager.store(tagConfig) ? "Success" : "Failure");
			}
			model.put("content", configHtmlParser.parse(businessTagConfigManager.getConfig().toString()));
		} else if ("add".equals(action)) {
			BusinessItemConfig itemConfig = config.findBusinessItemConfig(key(request));

			model.put("businessItemConfig", itemConfig == null ? new BusinessItemConfig() : itemConfig);
		} else if ("addSubmit".equals(action)) {
			model.put("opState", updateBusinessItem(request, domain) ? "Success" : "Fail");
			listModel(model, domain, businessConfigManager.queryConfigByDomain(domain));
		} else if ("delete".equals(action)) {
			model.put("opState", businessConfigManager.deleteBusinessItem(domain, key(request)) ? "Success" : "Fail");
			listModel(model, domain, businessConfigManager.queryConfigByDomain(domain));
		} else if ("alertRuleAdd".equals(action)) {
			alertRuleModel(model, request, domain);
		} else if ("alertRuleAddSubmit".equals(action)) {
			businessRuleConfigManager.updateRule(domain, key(request), parameter(request, "content", ""),
					parameter(request, "attributes", ""));
			listModel(model, domain, businessConfigManager.queryConfigByDomain(domain));
		} else if ("customAdd".equals(action)) {
			CustomConfig customConfig = config == null ? null : config.findCustomConfig(key(request));

			model.put("customConfig", customConfig == null ? new CustomConfig() : customConfig);
		} else if ("customAddSubmit".equals(action)) {
			model.put("opState", updateCustomItem(request, domain) ? "Success" : "Fail");
			listModel(model, domain, businessConfigManager.queryConfigByDomain(domain));
		} else if ("customDelete".equals(action)) {
			model.put("opState", businessConfigManager.deleteCustomItem(domain, key(request)) ? "Success" : "Fail");
			listModel(model, domain, businessConfigManager.queryConfigByDomain(domain));
		} else {
			listModel(model, domain, config);
		}
		return model;
	}

	private Map<String, Object> vueBusinessConfig(HttpServletRequest request) {
		String vueAction = parameter(request, "vueAction", "list");
		Map<String, Object> model = businessModel(request, vueAction);
		@SuppressWarnings("unchecked")
		List<BusinessItemConfig> configs = (List<BusinessItemConfig>) model.get("configs");
		@SuppressWarnings("unchecked")
		List<CustomConfig> customConfigs = (List<CustomConfig>) model.get("customConfigs");
		@SuppressWarnings("unchecked")
		Map<String, Set<String>> tags = (Map<String, Set<String>>) model.get("tags");
		Map<String, Object> json = new LinkedHashMap<String, Object>();

		json.put("contextPath", request.getContextPath());
		json.put("actionName", "businessList");
		if ("customAdd".equals(vueAction)) {
			json.put("actionName", "businessCustomAdd");
			json.put("customConfig", customItem((CustomConfig) model.get("customConfig"), tags));
		} else if ("tagConfig".equals(vueAction)) {
			json.put("actionName", "businessTagConfig");
			json.put("content", businessTagConfigManager.getConfig().toString());
		}
		json.put("businessActionName", vueAction);
		json.put("domain", model.get("domain"));
		json.put("domains", model.get("domains"));
		json.put("configs", businessItems(configs, tags));
		json.put("customConfigs", customItems(customConfigs, tags));
		json.put("opState", model.get("opState") == null ? request.getParameter("opState") : model.get("opState"));
		return json;
	}

	private Map<String, Object> vueUnauthorized(HttpServletRequest request) {
		Map<String, Object> json = new LinkedHashMap<String, Object>();
		String rtnUrl = requestUrl(request);
		String loginUrl = request.getContextPath() + "/mvc/vue/s/login?rtnUrl="
				+ URLEncoder.encode(rtnUrl, StandardCharsets.UTF_8);

		json.put("loginUrl", loginUrl);
		json.put("message", "请先登录后查看业务监控配置。");
		return json;
	}

	private List<Map<String, Object>> businessItems(List<BusinessItemConfig> configs, Map<String, Set<String>> tags) {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

		if (configs == null) {
			return items;
		}
		for (BusinessItemConfig config : configs) {
			Map<String, Object> item = businessItem(config, tags);

			item.put("showCount", config.getShowCount());
			item.put("showAvg", config.getShowAvg());
			item.put("showSum", config.getShowSum());
			item.put("custom", false);
			items.add(item);
		}
		return items;
	}

	private List<Map<String, Object>> customItems(List<CustomConfig> configs, Map<String, Set<String>> tags) {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

		if (configs == null) {
			return items;
		}
		for (CustomConfig config : configs) {
			Map<String, Object> item = customItem(config, tags);

			item.put("showCount", false);
			item.put("showAvg", true);
			item.put("showSum", false);
			item.put("custom", true);
			item.put("tags", tagList(tags, config.getId()));
			items.add(item);
		}
		return items;
	}

	private Map<String, Object> customItem(CustomConfig config, Map<String, Set<String>> tags) {
		Map<String, Object> item = new LinkedHashMap<String, Object>();

		if (config == null) {
			config = new CustomConfig();
		}
		item.put("id", config.getId());
		item.put("title", config.getTitle());
		item.put("viewOrder", config.getViewOrder());
		item.put("alarm", config.getAlarm());
		item.put("privilege", config.getPrivilege());
		item.put("pattern", config.getPattern());
		item.put("tags", tagList(tags, config.getId()));
		return item;
	}

	private Map<String, Object> businessItem(BusinessItemConfig config, Map<String, Set<String>> tags) {
		Map<String, Object> item = new LinkedHashMap<String, Object>();

		item.put("id", config.getId());
		item.put("title", config.getTitle());
		item.put("viewOrder", config.getViewOrder());
		item.put("alarm", config.getAlarm());
		item.put("privilege", config.getPrivilege());
		item.put("tags", tagList(tags, config.getId()));
		return item;
	}

	private List<String> tagList(Map<String, Set<String>> tags, String id) {
		Set<String> values = tags == null ? null : tags.get(id);

		if (values == null) {
			return new ArrayList<String>();
		}
		return new ArrayList<String>(values);
	}

	private void alertRuleModel(Map<String, Object> model, HttpServletRequest request, String domain) {
		String type = parameter(request, "attributes", "");
		Rule rule = businessRuleConfigManager.queryRule(domain, key(request), type);
		String id = "";
		String configs = "";

		if (rule != null) {
			id = rule.getId();
			configs = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
		}

		model.put("id", id);
		model.put("attributes", type);
		model.put("key", key(request));
		model.put("content", ruleDecorator.generateConfigsHtml(configs));
	}

	List<BusinessItemConfig> businessItemConfigs(BusinessReportConfig config) {
		if (config == null) {
			return new ArrayList<BusinessItemConfig>();
		}
		List<BusinessItemConfig> configs = new ArrayList<BusinessItemConfig>(config.getBusinessItemConfigs().values());

		Collections.sort(configs, new Comparator<BusinessItemConfig>() {
			@Override
			public int compare(BusinessItemConfig m1, BusinessItemConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});
		return configs;
	}

	List<CustomConfig> customConfigs(BusinessReportConfig config) {
		if (config == null) {
			return new ArrayList<CustomConfig>();
		}
		List<CustomConfig> configs = new ArrayList<CustomConfig>(config.getCustomConfigs().values());

		Collections.sort(configs, new Comparator<CustomConfig>() {
			@Override
			public int compare(CustomConfig m1, CustomConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});
		return configs;
	}

	private boolean booleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	private double doubleParameter(HttpServletRequest request, String name, double defaultValue) {
		String value = request.getParameter(name);

		if (value != null && value.length() > 0) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	private BusinessItemConfig businessItemConfig(HttpServletRequest request) {
		return new BusinessItemConfig().setId(parameter(request, "businessItemConfig.id", ""))
				.setTitle(parameter(request, "businessItemConfig.title", ""))
				.setViewOrder(doubleParameter(request, "businessItemConfig.viewOrder", 0))
				.setAlarm(booleanParameter(request, "businessItemConfig.alarm", false))
				.setShowCount(booleanParameter(request, "businessItemConfig.showCount", false))
				.setShowAvg(booleanParameter(request, "businessItemConfig.showAvg", false))
				.setShowSum(booleanParameter(request, "businessItemConfig.showSum", false))
				.setPrivilege(booleanParameter(request, "businessItemConfig.privilege", false));
	}

	private CustomConfig customConfig(HttpServletRequest request) {
		return new CustomConfig().setId(parameter(request, "customConfig.id", ""))
				.setTitle(parameter(request, "customConfig.title", ""))
				.setViewOrder(doubleParameter(request, "customConfig.viewOrder", 0))
				.setAlarm(booleanParameter(request, "customConfig.alarm", false))
				.setPrivilege(booleanParameter(request, "customConfig.privilege", false))
				.setPattern(parameter(request, "customConfig.pattern", ""));
	}

	void setConfigManager(BusinessConfigManager configManager) {
		this.businessConfigManager = configManager;
	}

	void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	void setTagConfigManager(BusinessTagConfigManager tagConfigManager) {
		this.businessTagConfigManager = tagConfigManager;
	}

	void setBusinessRuleConfigManager(BusinessRuleConfigManager businessRuleConfigManager) {
		this.businessRuleConfigManager = businessRuleConfigManager;
	}

	void setRuleDecorator(RuleFTLDecorator ruleDecorator) {
		this.ruleDecorator = ruleDecorator;
	}

	void setConfigHtmlParser(ConfigHtmlParser configHtmlParser) {
		this.configHtmlParser = configHtmlParser;
	}

	void setSigninService(SigninService signinService) {
		this.signinService = signinService;
	}

	private String domain(HttpServletRequest request) {
		String domain = request.getParameter("domain");

		if (domain == null || domain.length() == 0) {
			return Constants.CAT;
		}
		return domain;
	}

	private boolean isSupported(String action) {
		return "list".equals(action) || "add".equals(action) || "addSubmit".equals(action) || "delete".equals(action)
				|| "vueData".equals(action) || "tagConfig".equals(action) || "alertRuleAdd".equals(action)
				|| "alertRuleAddSubmit".equals(action) || "customAdd".equals(action)
				|| "customAddSubmit".equals(action) || "customDelete".equals(action);
	}

	private boolean isVueMutationRequest(HttpServletRequest request, String action) {
		if (!"true".equals(request.getParameter("vue"))) {
			return false;
		}
		return "customAddSubmit".equals(action) || "customDelete".equals(action) || "delete".equals(action)
				|| "addSubmit".equals(action) || ("tagConfig".equals(action) && request.getParameter("content") != null);
	}

	private String jsp(String action) {
		if ("tagConfig".equals(action)) {
			return "/jsp/spring/report/config/businessTag.jsp";
		}
		if ("add".equals(action)) {
			return "/jsp/spring/report/config/businessAdd.jsp";
		}
		if ("alertRuleAdd".equals(action)) {
			return "/jsp/spring/report/config/businessAlertAdd.jsp";
		}
		if ("customAdd".equals(action)) {
			return "/jsp/spring/report/config/businessCustomAdd.jsp";
		}
		return "/jsp/spring/report/config/businessList.jsp";
	}

	private String key(HttpServletRequest request) {
		return parameter(request, "key", "");
	}

	private void listModel(Map<String, Object> model, String domain, BusinessReportConfig config) {
		model.put("configs", businessItemConfigs(config));
		model.put("customConfigs", customConfigs(config));
		model.put("tags", businessTagConfigManager.findTagByDomain(domain));
	}

	private void writeJson(HttpServletResponse response, String body) throws IOException {
		writeJson(response, HttpServletResponse.SC_OK, body);
	}

	private void writeJson(HttpServletResponse response, int status, String body) throws IOException {
		response.setStatus(status);
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(body);
	}

	private String requestUrl(HttpServletRequest request) {
		StringBuilder url = new StringBuilder();

		url.append(request.getRequestURI());

		if (request.getQueryString() != null && request.getQueryString().length() > 0) {
			url.append('?').append(request.getQueryString());
		}
		return url.toString();
	}

	private String vueBusinessConfigUrl(HttpServletRequest request, String action, String domain, String opState) {
		StringBuilder url = new StringBuilder(request.getContextPath()).append("/mvc/vue/s/config?op=").append(action);

		url.append("&domain=").append(URLEncoder.encode(domain, StandardCharsets.UTF_8));
		if (opState != null && opState.length() > 0 && !"null".equals(opState)) {
			url.append("&opState=").append(URLEncoder.encode(opState, StandardCharsets.UTF_8));
		}
		return url.toString();
	}

	private String vueConfigAction(String action) {
		if ("tagConfig".equals(action)) {
			return "businessTagConfig";
		}
		return "businessList";
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	private boolean updateBusinessItem(HttpServletRequest request, String domain) {
		BusinessItemConfig itemConfig = businessItemConfig(request);
		String key = itemConfig.getId();
		BusinessReportConfig config = businessConfigManager.queryConfigByDomain(domain);
		boolean isModify = false;

		if (config != null && StringUtils.isNotEmpty(key)) {
			Map<String, BusinessItemConfig> itemConfigs = config.getBusinessItemConfigs();
			BusinessItemConfig origin = itemConfigs.get(key);

			if (origin != null) {
				isModify = true;
				config.addBusinessItemConfig(itemConfig);
				return businessConfigManager.updateConfigByDomain(config);
			}
		}
		if (!isModify && StringUtils.isNotEmpty(key)) {
			ConfigItem item = new ConfigItem();

			item.setShowAvg(itemConfig.getShowAvg());
			item.setShowCount(itemConfig.getShowCount());
			item.setShowSum(itemConfig.getShowSum());
			item.setTitle(itemConfig.getTitle());
			item.setViewOrder(itemConfig.getViewOrder());
			return businessConfigManager.insertBusinessConfigIfNotExist(domain, key, item);
		}
		return false;
	}

	private boolean updateCustomItem(HttpServletRequest request, String domain) {
		CustomConfig itemConfig = customConfig(request);
		BusinessReportConfig config = businessConfigManager.queryConfigByDomain(domain);

		if (StringUtils.isEmpty(itemConfig.getId())) {
			return false;
		}
		if (config == null) {
			config = new BusinessReportConfig();
		}
		if (config.getId() != null) {
			config.addCustomConfig(itemConfig);
			return businessConfigManager.updateConfigByDomain(config);
		}

		config.setId(domain);
		config.addCustomConfig(itemConfig);
		return businessConfigManager.insertConfigByDomain(config);
	}
}
