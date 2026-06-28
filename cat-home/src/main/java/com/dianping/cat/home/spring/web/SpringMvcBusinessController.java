package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	@RequestMapping(value = "/s/business", method = { RequestMethod.GET, RequestMethod.POST })
	public void business(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = action(request);

		if (!isSupported(action)) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		Map<String, Object> model = businessModel(request, action);

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
			CustomConfig customConfig = config.findCustomConfig(key(request));

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

	private String domain(HttpServletRequest request) {
		String domain = request.getParameter("domain");

		if (domain == null || domain.length() == 0) {
			return Constants.CAT;
		}
		return domain;
	}

	private boolean isSupported(String action) {
		return "list".equals(action) || "add".equals(action) || "addSubmit".equals(action) || "delete".equals(action)
				|| "tagConfig".equals(action) || "alertRuleAdd".equals(action) || "alertRuleAddSubmit".equals(action)
				|| "customAdd".equals(action) || "customAddSubmit".equals(action) || "customDelete".equals(action);
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
		if (config.getId() != null) {
			config.addCustomConfig(itemConfig);
			return businessConfigManager.updateConfigByDomain(config);
		}

		config.setId(domain);
		config.addCustomConfig(itemConfig);
		return businessConfigManager.insertConfigByDomain(config);
	}
}
