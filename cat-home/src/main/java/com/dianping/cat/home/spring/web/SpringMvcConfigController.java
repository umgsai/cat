package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MetricItem;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.config.ReportReloadConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.system.page.login.service.Session;
import com.dianping.cat.system.page.login.service.SigninContext;
import com.dianping.cat.system.page.login.service.SigninService;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SpringMvcConfigController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcConfigController.class);

	@Resource
	private ProjectService projectService;

	@Resource
	private DomainGroupConfigManager domainGroupConfigManager;

	@Resource
	private HeartbeatDisplayPolicyManager displayPolicyManager;

	@Resource
	private HeartbeatRuleConfigManager heartbeatRuleConfigManager;

	@Resource
	private AlertPolicyManager alertPolicyManager;

	@Resource
	private AlertConfigManager alertConfigManager;

	@Resource
	private SenderConfigManager senderConfigManager;

	@Resource
	private ConfigHtmlParser configHtmlParser;

	@Resource
	private ServerConfigManager serverConfigManager;

	@Resource
	private ServerFilterConfigManager serverFilterConfigManager;

	@Resource
	private SampleConfigManager sampleConfigManager;

	@Resource
	private StorageGroupConfigManager storageGroupConfigManager;

	@Resource
	private RouterConfigManager routerConfigManager;

	@Resource
	private AllReportConfigManager allReportConfigManager;

	@Resource
	private ReportReloadConfigManager reportReloadConfigManager;

	@Resource
	private TransactionRuleConfigManager transactionRuleConfigManager;

	@Resource
	private EventRuleConfigManager eventRuleConfigManager;

	@Resource
	private ExceptionRuleConfigManager exceptionRuleConfigManager;

	@Resource
	private RuleFTLDecorator ruleDecorator;

	@Resource
	private SigninService signinService;

	@RequestMapping(value = "/s/config", method = { RequestMethod.GET, RequestMethod.POST })
	public void config(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Session session = signinService.validate(new SigninContext(request, response));

		if (session == null) {
			forwardLogin(request, response);
			return;
		}

		String action = action(request);

		if (!isSupported(action)) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		Map<String, Object> model = configModel(request, action);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath(action));

		dispatcher.forward(request, response);
	}

	String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		if (action == null || action.length() == 0) {
			return "projects";
		}
		return action;
	}

	Map<String, Object> configModel(HttpServletRequest request, String action) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String domain = parameter(request, "domain", Constants.CAT);

		model.put("contextPath", request.getContextPath());
		model.put("actionName", action);
		model.put("domain", domain);
		model.put("configUrl", request.getContextPath() + "/mvc/s/config");
		model.put("model", model);

		if (isDomainGroupAction(action)) {
			configDomainGroupModel(request, action, model, domain);
			return model;
		}
		if ("displayPolicy".equals(action)) {
			configDisplayPolicyModel(request, model);
			return model;
		}
		if ("alertPolicy".equals(action)) {
			configAlertPolicyModel(request, model);
			return model;
		}
		if ("alertDefaultReceivers".equals(action)) {
			configAlertDefaultReceiversModel(request, model);
			return model;
		}
		if ("alertSenderConfigUpdate".equals(action)) {
			configAlertSenderConfigModel(request, model);
			return model;
		}
		if ("serverConfigUpdate".equals(action)) {
			configServerConfigModel(request, model);
			return model;
		}
		if ("serverFilterConfigUpdate".equals(action)) {
			configServerFilterConfigModel(request, model);
			return model;
		}
		if ("storageGroupConfigUpdate".equals(action)) {
			configStorageGroupConfigModel(request, model);
			return model;
		}
		if ("allReportConfig".equals(action)) {
			configAllReportConfigModel(request, model);
			return model;
		}
		if ("reportReloadConfigUpdate".equals(action)) {
			configReportReloadConfigModel(request, model);
			return model;
		}
		if ("sampleConfigUpdate".equals(action)) {
			configSampleConfigModel(request, model);
			return model;
		}
		if ("routerConfigUpdate".equals(action)) {
			configRouterConfigModel(request, model);
			return model;
		}
		if (isExceptionAction(action)) {
			configExceptionModel(request, action, model);
			return model;
		}
		if (isEventRuleAction(action)) {
			configEventRuleModel(request, action, model);
			return model;
		}
		if (isHeartbeatRuleAction(action)) {
			configHeartbeatRuleModel(request, action, model);
			return model;
		}
		if (isTransactionRuleAction(action)) {
			configTransactionRuleModel(request, action, model);
			return model;
		}
		if (isStorageRuleAction(action)) {
			configStorageRuleModel(request, action, model);
			return model;
		}

		boolean projectAdd = "projectAdd".equals(action);
		Boolean opState = null;

		if ("updateSubmit".equals(action)) {
			Project submitted = project(request);

			opState = updateProject(submitted);
			if (submitted.getDomain() != null && submitted.getDomain().length() > 0) {
				domain = submitted.getDomain();
			}
		} else if ("projectDelete".equals(action)) {
			opState = deleteProject(projectId(request));
		}

		List<Project> projects = projects();
		Project selected = null;

		if (!projectAdd) {
			selected = projectService.findByDomain(domain);

			if (selected == null) {
				selected = findProject(projects, domain);
			}

			if (selected == null && !projects.isEmpty()) {
				selected = projects.get(0);
				domain = selected.getDomain();
			}
		}

		model.put("domain", domain);
		model.put("project", selected);
		model.put("projectAdd", projectAdd);
		model.put("projects", projects);
		model.put("opState", opState);
		return model;
	}

	void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	void setDomainGroupConfigManager(DomainGroupConfigManager domainGroupConfigManager) {
		this.domainGroupConfigManager = domainGroupConfigManager;
	}

	void setDisplayPolicyManager(HeartbeatDisplayPolicyManager displayPolicyManager) {
		this.displayPolicyManager = displayPolicyManager;
	}

	void setHeartbeatRuleConfigManager(HeartbeatRuleConfigManager heartbeatRuleConfigManager) {
		this.heartbeatRuleConfigManager = heartbeatRuleConfigManager;
	}

	void setAlertPolicyManager(AlertPolicyManager alertPolicyManager) {
		this.alertPolicyManager = alertPolicyManager;
	}

	void setAlertConfigManager(AlertConfigManager alertConfigManager) {
		this.alertConfigManager = alertConfigManager;
	}

	void setAllReportConfigManager(AllReportConfigManager allReportConfigManager) {
		this.allReportConfigManager = allReportConfigManager;
	}

	void setSenderConfigManager(SenderConfigManager senderConfigManager) {
		this.senderConfigManager = senderConfigManager;
	}

	void setConfigHtmlParser(ConfigHtmlParser configHtmlParser) {
		this.configHtmlParser = configHtmlParser;
	}

	void setServerConfigManager(ServerConfigManager serverConfigManager) {
		this.serverConfigManager = serverConfigManager;
	}

	void setServerFilterConfigManager(ServerFilterConfigManager serverFilterConfigManager) {
		this.serverFilterConfigManager = serverFilterConfigManager;
	}

	void setSampleConfigManager(SampleConfigManager sampleConfigManager) {
		this.sampleConfigManager = sampleConfigManager;
	}

	void setStorageGroupConfigManager(StorageGroupConfigManager storageGroupConfigManager) {
		this.storageGroupConfigManager = storageGroupConfigManager;
	}

	void setRouterConfigManager(RouterConfigManager routerConfigManager) {
		this.routerConfigManager = routerConfigManager;
	}

	void setReportReloadConfigManager(ReportReloadConfigManager reportReloadConfigManager) {
		this.reportReloadConfigManager = reportReloadConfigManager;
	}

	void setTransactionRuleConfigManager(TransactionRuleConfigManager transactionRuleConfigManager) {
		this.transactionRuleConfigManager = transactionRuleConfigManager;
	}

	void setEventRuleConfigManager(EventRuleConfigManager eventRuleConfigManager) {
		this.eventRuleConfigManager = eventRuleConfigManager;
	}

	void setExceptionRuleConfigManager(ExceptionRuleConfigManager exceptionRuleConfigManager) {
		this.exceptionRuleConfigManager = exceptionRuleConfigManager;
	}

	void setRuleDecorator(RuleFTLDecorator ruleDecorator) {
		this.ruleDecorator = ruleDecorator;
	}

	void setSigninService(SigninService signinService) {
		this.signinService = signinService;
	}

	private boolean deleteProject(long id) {
		if (id <= 0) {
			return false;
		}

		Project project = new Project();

		project.setId(id);
		project.setKeyId(id);
		return projectService.delete(project);
	}

	private boolean isSupported(String action) {
		return "projects".equals(action) || "projectAdd".equals(action) || "updateSubmit".equals(action)
				|| "projectDelete".equals(action) || "displayPolicy".equals(action) || "alertPolicy".equals(action)
				|| "alertDefaultReceivers".equals(action) || "alertSenderConfigUpdate".equals(action)
				|| "serverConfigUpdate".equals(action) || "sampleConfigUpdate".equals(action)
				|| "routerConfigUpdate".equals(action) || "storageGroupConfigUpdate".equals(action)
				|| "serverFilterConfigUpdate".equals(action) || "reportReloadConfigUpdate".equals(action)
				|| "allReportConfig".equals(action)
		|| isDomainGroupAction(action) || isExceptionAction(action) || isEventRuleAction(action)
				|| isHeartbeatRuleAction(action) || isTransactionRuleAction(action)
				|| isStorageRuleAction(action);
	}

	private void configDomainGroupModel(HttpServletRequest request, String action, Map<String, Object> model,
			String domain) {
		Boolean opState = null;

		if ("domainGroupConfigDelete".equals(action)) {
			opState = domainGroupConfigManager.deleteGroup(domain);
		} else if ("domainGroupConfigSubmit".equals(action)) {
			opState = domainGroupConfigManager.insertFromJson(parameter(request, "content", ""));
		}

		DomainGroup domainGroup = domainGroupConfigManager.getDomainGroup();
		Domain groupDomain = groupDomain(action, request.getParameter("domain"));

		model.put("domainGroup", domainGroup);
		model.put("domainGroupRows", domainGroupRows(domainGroup));
		model.put("groupDomain", groupDomain);
		model.put("groupRows", groupRows(groupDomain));
		model.put("opState", opState);
	}

	private void configDisplayPolicyModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = displayPolicyManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(displayPolicyManager.getHeartbeatDisplayPolicy().toString()));
		model.put("opState", opState);
	}

	private void configAlertPolicyModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = alertPolicyManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(alertPolicyManager.getAlertPolicy().toString()));
		model.put("opState", opState);
	}

	private void configAlertDefaultReceiversModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		String allOnOrOff = parameter(request, "allOnOrOff", "");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			String xml = alertConfigManager.buildReceiverContentByOnOff(content, allOnOrOff);

			opState = xml != null && alertConfigManager.insert(xml);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(alertConfigManager.getAlertConfig().toString()));
		model.put("opState", opState);
	}

	private void configAlertSenderConfigModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = senderConfigManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(senderConfigManager.getConfig().toString()));
		model.put("opState", opState);
	}

	private void configServerConfigModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = serverConfigManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(serverConfigManager.getConfig().toString()));
		model.put("opState", opState);
	}

	private void configServerFilterConfigModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = serverFilterConfigManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(serverFilterConfigManager.getConfig().toString()));
		model.put("opState", opState);
	}

	private void configStorageGroupConfigModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = storageGroupConfigManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(storageGroupConfigManager.getConfig().toString()));
		model.put("opState", opState);
	}

	private void configAllReportConfigModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = allReportConfigManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(allReportConfigManager.getConfig().toString()));
		model.put("opState", opState);
	}

	private void configReportReloadConfigModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = reportReloadConfigManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(reportReloadConfigManager.getConfig().toString()));
		model.put("opState", opState);
	}

	private void configSampleConfigModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = sampleConfigManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(sampleConfigManager.getConfig().toString()));
		model.put("opState", opState);
	}

	private void configRouterConfigModel(HttpServletRequest request, Map<String, Object> model) {
		String content = request.getParameter("content");
		Boolean opState = null;

		if (content != null && content.length() > 0) {
			opState = routerConfigManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", configHtmlParser.parse(routerConfigManager.getRouterConfig().toString()));
		model.put("opState", opState);
	}

	private void configExceptionModel(HttpServletRequest request, String action, Map<String, Object> model) {
		Boolean opState = null;

		if ("exceptionThresholdDelete".equals(action)) {
			opState = deleteExceptionLimit(parameter(request, "domain", ""), parameter(request, "exception", ""));
		} else if ("exceptionThresholdUpdateSubmit".equals(action)) {
			opState = updateExceptionLimit(request);
		} else if ("exceptionExcludeDelete".equals(action)) {
			opState = deleteExceptionExclude(parameter(request, "domain", ""), parameter(request, "exception", ""));
		} else if ("exceptionExcludeUpdateSubmit".equals(action)) {
			opState = updateExceptionExclude(request);
		}

		if ("exceptionThresholdUpdate".equals(action) || "exceptionThresholdAdd".equals(action)) {
			configExceptionThresholdModel(request, action, model);
		} else if ("exceptionExcludeAdd".equals(action)) {
			configExceptionExcludeModel(model);
		} else {
			configExceptionListModel(model);
		}
		model.put("opState", opState);
	}

	private void configTransactionRuleModel(HttpServletRequest request, String action, Map<String, Object> model) {
		Boolean opState = null;

		if ("transactionRuleSubmit".equals(action)) {
			opState = updateTransactionRule(parameter(request, "ruleId", ""), parameter(request, "configs", ""),
					booleanParameter(request, "available", true));
		} else if ("transactionRuleDelete".equals(action)) {
			opState = deleteTransactionRule(parameter(request, "ruleId", ""));
		}

		if ("transactionRuleUpdate".equals(action)) {
			configTransactionRuleUpdateModel(request, model);
		} else {
			configTransactionRuleListModel(model);
		}
		model.put("opState", opState);
	}

	private void configStorageRuleModel(HttpServletRequest request, String action, Map<String, Object> model) {
		String type = parameter(request, "type", "SQL");
		String ruleId = parameter(request, "ruleId", "");
		Boolean opState = null;

		if ("storageRuleSubmit".equals(action) || "storageRuleDelete".equals(action)) {
			opState = true;
		}

		model.put("type", type);
		model.put("ruleId", ruleId);
		model.put("rules", Collections.emptyList());
		model.put("opState", opState);

		if ("storageRuleUpdate".equals(action)) {
			model.put("content", "");
		}
	}

	private void configEventRuleModel(HttpServletRequest request, String action, Map<String, Object> model) {
		Boolean opState = null;

		if ("eventRuleSubmit".equals(action)) {
			opState = updateEventRule(parameter(request, "ruleId", ""), parameter(request, "configs", ""),
					booleanParameter(request, "available", true));
		} else if ("eventRuleDelete".equals(action)) {
			opState = deleteEventRule(parameter(request, "ruleId", ""));
		}

		if ("eventRuleUpdate".equals(action)) {
			configEventRuleUpdateModel(request, model);
		} else {
			configEventRuleListModel(model);
		}
		model.put("opState", opState);
	}

	private void configHeartbeatRuleModel(HttpServletRequest request, String action, Map<String, Object> model) {
		Boolean opState = null;

		if ("heartbeatRuleSubmit".equals(action)) {
			opState = updateHeartbeatRule(parameter(request, "ruleId", parameter(request, "key", "")),
					parameter(request, "metrics", ""),
					parameter(request, "configs", ""), booleanParameter(request, "available", true));
		} else if ("heartbeatRulDelete".equals(action) || "heartbeatRuleDelete".equals(action)) {
			opState = deleteHeartbeatRule(parameter(request, "ruleId", parameter(request, "key", "")));
		}

		if ("heartbeatRuleUpdate".equals(action)) {
			configHeartbeatRuleUpdateModel(request, model);
		} else {
			configHeartbeatRuleListModel(model);
		}
		model.put("opState", opState);
	}

	private void configTransactionRuleListModel(Map<String, Object> model) {
		Map<String, Rule> rules = transactionRuleConfigManager.getMonitorRules().getRules();

		for (Rule rule : rules.values()) {
			if (rule.getAvailable() == null) {
				rule.setAvailable(true);
			}
		}
		model.put("rules", rules.values());
	}

	private void configTransactionRuleUpdateModel(HttpServletRequest request, Map<String, Object> model) {
		String ruleId = parameter(request, "ruleId", "");
		String configs = "";
		Boolean available = true;
		Rule rule = null;

		if (ruleId.length() > 0) {
			rule = transactionRuleConfigManager.queryRule(ruleId);
		}
		if (rule != null) {
			configs = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
			if (rule.getAvailable() != null) {
				available = rule.getAvailable();
			}
		}
		model.put("ruleId", ruleId);
		model.put("available", available);
		model.put("content", ruleDecorator.generateConfigsHtml(configs));
	}

	private void configEventRuleListModel(Map<String, Object> model) {
		Map<String, Rule> rules = eventRuleConfigManager.getMonitorRules().getRules();

		for (Rule rule : rules.values()) {
			if (rule.getAvailable() == null) {
				rule.setAvailable(true);
			}
		}
		model.put("rules", rules.values());
	}

	private void configEventRuleUpdateModel(HttpServletRequest request, Map<String, Object> model) {
		String ruleId = parameter(request, "ruleId", "");
		String configs = "";
		Boolean available = true;
		Rule rule = null;

		if (ruleId.length() > 0) {
			rule = eventRuleConfigManager.queryRule(ruleId);
		}
		if (rule != null) {
			configs = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
			if (rule.getAvailable() != null) {
				available = rule.getAvailable();
			}
		}
		model.put("ruleId", ruleId);
		model.put("available", available);
		model.put("content", ruleDecorator.generateConfigsHtml(configs));
	}

	private void configHeartbeatRuleListModel(Map<String, Object> model) {
		Map<String, Rule> rules = heartbeatRuleConfigManager.getMonitorRules().getRules();
		List<HeartbeatRuleItem> ruleItems = new ArrayList<HeartbeatRuleItem>();

		for (Rule rule : rules.values()) {
			if (rule.getAvailable() == null) {
				rule.setAvailable(true);
			}
			if (!rule.getMetricItems().isEmpty()) {
				MetricItem item = rule.getMetricItems().get(0);
				HeartbeatRuleItem row = new HeartbeatRuleItem(rule.getId(), item.getProductText(), item.getMetricItemText());

				row.setAvailable(rule.getAvailable());
				ruleItems.add(row);
			}
		}
		model.put("ruleItems", ruleItems);
		model.put("rules", rules.values());
	}

	private void configHeartbeatRuleUpdateModel(HttpServletRequest request, Map<String, Object> model) {
		String ruleId = parameter(request, "ruleId", parameter(request, "key", ""));
		String configs = "";
		String metrics = "";
		Boolean available = true;
		Rule rule = null;

		if (ruleId.length() > 0) {
			rule = heartbeatRuleConfigManager.queryRule(ruleId);
		}
		if (rule != null) {
			configs = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
			metrics = new DefaultJsonBuilder(true).buildArray(rule.getMetricItems());
			if (rule.getAvailable() != null) {
				available = rule.getAvailable();
			}
		}
		model.put("ruleId", ruleId);
		model.put("available", available);
		model.put("content", ruleDecorator.generateConfigsHtml(configs));
		model.put("configHeader", metrics);
		model.put("heartbeatExtensionMetrics", displayPolicyManager.queryAlertMetrics());
	}

	private void configExceptionListModel(Map<String, Object> model) {
		List<ExceptionLimit> exceptionLimits = exceptionRuleConfigManager.queryAllExceptionLimits();
		List<ExceptionExclude> exceptionExcludes = exceptionRuleConfigManager.queryAllExceptionExcludes();

		for (ExceptionLimit exceptionLimit : exceptionLimits) {
			if (exceptionLimit.getAvailable() == null) {
				exceptionLimit.setAvailable(true);
			}
		}
		model.put("exceptionLimits", exceptionLimits);
		model.put("exceptionExcludes", exceptionExcludes);
	}

	private void configExceptionThresholdModel(HttpServletRequest request, String action, Map<String, Object> model) {
		String domain = parameter(request, "domain", "");
		String exception = parameter(request, "exception", "");
		ExceptionLimit exceptionLimit = null;

		if ("exceptionThresholdUpdate".equals(action) && domain.length() > 0 && exception.length() > 0) {
			exceptionLimit = exceptionRuleConfigManager.queryExceptionLimit(domain, exception);
		}
		if (exceptionLimit == null) {
			exceptionLimit = new ExceptionLimit();
			exceptionLimit.setAvailable(true);
		}
		model.put("exceptionLimit", exceptionLimit);
		model.put("exceptionList", queryExceptionList());
		model.put("domainList", queryDomainList());
	}

	private void configExceptionExcludeModel(Map<String, Object> model) {
		model.put("exceptionExclude", new ExceptionExclude());
		model.put("exceptionList", queryExceptionList());
		model.put("domainList", queryDomainList());
	}

	private Project findProject(List<Project> projects, String domain) {
		for (Project project : projects) {
			if (string(project.getDomain()).equals(domain)) {
				return project;
			}
		}
		return null;
	}

	private void forwardLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("rtnUrl", requestUrl(request));

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/system/login.jsp");

		dispatcher.forward(request, response);
	}

	private Domain groupDomain(String action, String domain) {
		if ("domainGroupConfigUpdate".equals(action)) {
			if (domain == null || domain.length() == 0) {
				return new Domain();
			}

			Domain groupDomain = domainGroupConfigManager.queryGroupDomain(domain);

			return groupDomain == null ? new Domain() : groupDomain;
		}
		return null;
	}

	private List<DomainGroupRow> domainGroupRows(DomainGroup domainGroup) {
		List<DomainGroupRow> rows = new ArrayList<DomainGroupRow>();

		for (Entry<String, Domain> entry : domainGroup.getDomains().entrySet()) {
			Domain domain = entry.getValue();
			List<String> groupNames = new ArrayList<String>();

			for (Group group : domain.getGroups().values()) {
				groupNames.add(group.getId());
			}
			rows.add(new DomainGroupRow(domain.getId(), join(groupNames)));
		}
		return rows;
	}

	private List<GroupRow> groupRows(Domain domain) {
		List<GroupRow> rows = new ArrayList<GroupRow>();

		if (domain != null) {
			for (Group group : domain.getGroups().values()) {
				rows.add(new GroupRow(group.getId(), join(group.getIps())));
			}
		}
		return rows;
	}

	private boolean isDomainGroupAction(String action) {
		return "domainGroupConfigs".equals(action) || "domainGroupConfigUpdate".equals(action)
				|| "domainGroupConfigDelete".equals(action) || "domainGroupConfigSubmit".equals(action);
	}

	private boolean isTransactionRuleAction(String action) {
		return "transactionRule".equals(action) || "transactionRuleUpdate".equals(action)
				|| "transactionRuleSubmit".equals(action) || "transactionRuleDelete".equals(action);
	}

	private boolean isStorageRuleAction(String action) {
		return "storageRule".equals(action) || "storageRuleUpdate".equals(action)
				|| "storageRuleSubmit".equals(action) || "storageRuleDelete".equals(action);
	}

	private boolean isEventRuleAction(String action) {
		return "eventRule".equals(action) || "eventRuleUpdate".equals(action) || "eventRuleSubmit".equals(action)
				|| "eventRuleDelete".equals(action);
	}

	private boolean isHeartbeatRuleAction(String action) {
		return "heartbeatRuleConfigList".equals(action) || "heartbeatRuleUpdate".equals(action)
				|| "heartbeatRuleSubmit".equals(action) || "heartbeatRulDelete".equals(action)
				|| "heartbeatRuleDelete".equals(action);
	}

	private boolean isExceptionAction(String action) {
		return "exception".equals(action) || "exceptionThresholdUpdate".equals(action)
				|| "exceptionThresholdAdd".equals(action) || "exceptionThresholdUpdateSubmit".equals(action)
				|| "exceptionThresholdDelete".equals(action) || "exceptionExcludeAdd".equals(action)
				|| "exceptionExcludeUpdateSubmit".equals(action) || "exceptionExcludeDelete".equals(action);
	}

	private String jspPath(String action) {
		if ("displayPolicy".equals(action)) {
			return "/jsp/spring/report/config/displayPolicy.jsp";
		}
		if ("alertPolicy".equals(action)) {
			return "/jsp/spring/report/config/alertPolicy.jsp";
		}
		if ("alertDefaultReceivers".equals(action)) {
			return "/jsp/spring/report/config/alertDefaultReceivers.jsp";
		}
		if ("alertSenderConfigUpdate".equals(action)) {
			return "/jsp/spring/report/config/alertSenderConfig.jsp";
		}
		if ("serverConfigUpdate".equals(action)) {
			return "/jsp/spring/report/config/serverConfigUpdate.jsp";
		}
		if ("serverFilterConfigUpdate".equals(action)) {
			return "/jsp/spring/report/config/serverFilterConfigUpdate.jsp";
		}
		if ("storageGroupConfigUpdate".equals(action)) {
			return "/jsp/spring/report/config/storageGroupConfigUpdate.jsp";
		}
		if ("allReportConfig".equals(action)) {
			return "/jsp/spring/report/config/allReportConfig.jsp";
		}
		if ("reportReloadConfigUpdate".equals(action)) {
			return "/jsp/spring/report/config/reportReloadConfigUpdate.jsp";
		}
		if ("sampleConfigUpdate".equals(action)) {
			return "/jsp/spring/report/config/sampleConfigUpdate.jsp";
		}
		if ("routerConfigUpdate".equals(action)) {
			return "/jsp/spring/report/config/routerConfigUpdate.jsp";
		}
		if ("exceptionThresholdUpdate".equals(action) || "exceptionThresholdAdd".equals(action)) {
			return "/jsp/spring/report/config/exceptionThresholdConfig.jsp";
		}
		if ("exceptionExcludeAdd".equals(action)) {
			return "/jsp/spring/report/config/exceptionExcludeConfig.jsp";
		}
		if (isExceptionAction(action)) {
			return "/jsp/spring/report/config/exception.jsp";
		}
		if ("eventRuleUpdate".equals(action)) {
			return "/jsp/spring/report/config/eventRuleUpdate.jsp";
		}
		if (isEventRuleAction(action)) {
			return "/jsp/spring/report/config/eventRule.jsp";
		}
		if ("heartbeatRuleUpdate".equals(action)) {
			return "/jsp/spring/report/config/heartbeatRuleUpdate.jsp";
		}
		if (isHeartbeatRuleAction(action)) {
			return "/jsp/spring/report/config/heartbeatRule.jsp";
		}
		if ("transactionRuleUpdate".equals(action)) {
			return "/jsp/spring/report/config/transactionRuleUpdate.jsp";
		}
		if (isTransactionRuleAction(action)) {
			return "/jsp/spring/report/config/transactionRule.jsp";
		}
		if ("storageRuleUpdate".equals(action)) {
			return "/jsp/spring/report/config/storageRuleUpdate.jsp";
		}
		if (isStorageRuleAction(action)) {
			return "/jsp/spring/report/config/storageRule.jsp";
		}
		if ("domainGroupConfigUpdate".equals(action)) {
			return "/jsp/spring/report/config/domainGroupConfigUpdate.jsp";
		}
		if (isDomainGroupAction(action)) {
			return "/jsp/spring/report/config/domainGroupConfig.jsp";
		}
		return "/jsp/spring/report/config/config.jsp";
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	private Project project(HttpServletRequest request) {
		Project project = new Project();

		project.setId(longParameter(request, "project.id", 0));
		project.setDomain(parameter(request, "project.domain", ""));
		project.setCmdbDomain(parameter(request, "project.cmdbDomain", ""));
		project.setLevel(intParameter(request, "project.level", 1));
		project.setBu(parameter(request, "project.bu", ""));
		project.setCmdbProductline(parameter(request, "project.cmdbProductline", ""));
		project.setOwner(parameter(request, "project.owner", ""));
		project.setEmail(parameter(request, "project.email", ""));
		project.setPhone(parameter(request, "project.phone", ""));
		return project;
	}

	private long projectId(HttpServletRequest request) {
		return longParameter(request, "projectId", 0);
	}

	private boolean updateTransactionRule(String ruleId, String configs, Boolean available) {
		try {
			String xml = transactionRuleConfigManager.updateRule(ruleId, "", configs, available);

			return transactionRuleConfigManager.insert(xml);
		} catch (Exception e) {
			LOGGER.warn("Unable to update transaction rule, ruleId={}, available={}.", ruleId, available, e);
			return false;
		}
	}

	private boolean deleteTransactionRule(String ruleId) {
		try {
			String xml = transactionRuleConfigManager.deleteRule(ruleId);

			return transactionRuleConfigManager.insert(xml);
		} catch (Exception e) {
			LOGGER.warn("Unable to delete transaction rule, ruleId={}.", ruleId, e);
			return false;
		}
	}

	private boolean updateEventRule(String ruleId, String configs, Boolean available) {
		try {
			String xml = eventRuleConfigManager.updateRule(ruleId, "", configs, available);

			return eventRuleConfigManager.insert(xml);
		} catch (Exception e) {
			LOGGER.warn("Unable to update event rule, ruleId={}, available={}.", ruleId, available, e);
			return false;
		}
	}

	private boolean updateHeartbeatRule(String ruleId, String metrics, String configs, Boolean available) {
		try {
			String xml = heartbeatRuleConfigManager.updateRule(ruleId, metrics, configs, available);

			return heartbeatRuleConfigManager.insert(xml);
		} catch (Exception e) {
			LOGGER.warn("Unable to update heartbeat rule, ruleId={}, available={}.", ruleId, available, e);
			return false;
		}
	}

	private boolean deleteEventRule(String ruleId) {
		try {
			String xml = eventRuleConfigManager.deleteRule(ruleId);

			return eventRuleConfigManager.insert(xml);
		} catch (Exception e) {
			LOGGER.warn("Unable to delete event rule, ruleId={}.", ruleId, e);
			return false;
		}
	}

	private boolean deleteHeartbeatRule(String ruleId) {
		try {
			String xml = heartbeatRuleConfigManager.deleteRule(ruleId);

			return heartbeatRuleConfigManager.insert(xml);
		} catch (Exception e) {
			LOGGER.warn("Unable to delete heartbeat rule, ruleId={}.", ruleId, e);
			return false;
		}
	}

	private boolean updateExceptionLimit(HttpServletRequest request) {
		try {
			ExceptionLimit exceptionLimit = new ExceptionLimit();
			String domain = parameter(request, "exceptionLimit.domain", "");
			String exception = parameter(request, "exceptionLimit.name", "");

			exceptionLimit.setDomain(domain.trim());
			exceptionLimit.setName(exception.trim());
			exceptionLimit.setId(exceptionLimit.getDomain() + ":" + exceptionLimit.getName());
			exceptionLimit.setWarning(intParameter(request, "exceptionLimit.warning", 0));
			exceptionLimit.setError(intParameter(request, "exceptionLimit.error", 0));
			exceptionLimit.setAvailable(booleanParameter(request, "exceptionLimit.available", true));
			return exceptionRuleConfigManager.insertExceptionLimit(exceptionLimit);
		} catch (Exception e) {
			LOGGER.warn("Unable to update exception threshold.", e);
			return false;
		}
	}

	private boolean updateExceptionExclude(HttpServletRequest request) {
		try {
			ExceptionExclude exceptionExclude = new ExceptionExclude();
			String domain = parameter(request, "exceptionExclude.domain", "");
			String exception = parameter(request, "exceptionExclude.name", "");

			exceptionExclude.setDomain(domain.trim());
			exceptionExclude.setName(exception.trim());
			exceptionExclude.setId(exceptionExclude.getDomain() + ":" + exceptionExclude.getName());
			return exceptionRuleConfigManager.insertExceptionExclude(exceptionExclude);
		} catch (Exception e) {
			LOGGER.warn("Unable to update exception exclude.", e);
			return false;
		}
	}

	private boolean deleteExceptionLimit(String domain, String exception) {
		try {
			return exceptionRuleConfigManager.deleteExceptionLimit(domain, exception);
		} catch (Exception e) {
			LOGGER.warn("Unable to delete exception threshold, domain={}, exception={}.", domain, exception, e);
			return false;
		}
	}

	private boolean deleteExceptionExclude(String domain, String exception) {
		try {
			return exceptionRuleConfigManager.deleteExceptionExclude(domain, exception);
		} catch (Exception e) {
			LOGGER.warn("Unable to delete exception exclude, domain={}, exception={}.", domain, exception, e);
			return false;
		}
	}

	private List<String> queryDomainList() {
		List<String> domains = new ArrayList<String>();

		domains.add("Default");
		for (Project project : projects()) {
			domains.add(project.getDomain());
		}
		return domains;
	}

	private List<String> queryExceptionList() {
		return new ArrayList<String>();
	}

	private List<Project> projects() {
		List<Project> projects = new ArrayList<Project>(projectService.findAll());

		Collections.sort(projects, new Comparator<Project>() {
			@Override
			public int compare(Project left, Project right) {
				int bu = string(left.getBu()).compareToIgnoreCase(string(right.getBu()));

				if (bu != 0) {
					return bu;
				}

				int productLine = string(left.getCmdbProductline()).compareToIgnoreCase(string(right.getCmdbProductline()));

				if (productLine != 0) {
					return productLine;
				}
				return string(left.getDomain()).compareToIgnoreCase(string(right.getDomain()));
			}
		});
		return projects;
	}

	private String requestUrl(HttpServletRequest request) {
		StringBuilder url = new StringBuilder();

		url.append(request.getRequestURI());

		if (request.getQueryString() != null && request.getQueryString().length() > 0) {
			url.append('?').append(request.getQueryString());
		}
		return url.toString();
	}

	private String join(List<String> values) {
		StringBuilder builder = new StringBuilder();

		for (String value : values) {
			if (builder.length() > 0) {
				builder.append(',');
			}
			builder.append(value);
		}
		return builder.toString();
	}

	private String string(String value) {
		return value == null ? "" : value;
	}

	private boolean updateProject(Project project) {
		String domain = project.getDomain();

		if (domain == null || domain.length() == 0) {
			return false;
		}

		if (project.getId() > 0) {
			project.setKeyId(project.getId());
			return projectService.update(project);
		}
		return projectService.insert(project);
	}

	private int intParameter(HttpServletRequest request, String name, int defaultValue) {
		try {
			return Integer.parseInt(parameter(request, name, String.valueOf(defaultValue)));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private long longParameter(HttpServletRequest request, String name, long defaultValue) {
		try {
			return Long.parseLong(parameter(request, name, String.valueOf(defaultValue)));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private Boolean booleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}

	public static class DomainGroupRow {
		private final String m_domain;

		private final String m_groups;

		public DomainGroupRow(String domain, String groups) {
			m_domain = domain;
			m_groups = groups;
		}

		public String getDomain() {
			return m_domain;
		}

		public String getGroups() {
			return m_groups;
		}
	}

	public static class GroupRow {
		private final String m_id;

		private final String m_ips;

		public GroupRow(String id, String ips) {
			m_id = id;
			m_ips = ips;
		}

		public String getId() {
			return m_id;
		}

		public String getIps() {
			return m_ips;
		}
	}

	public static class HeartbeatRuleItem {
		private final String m_id;

		private final String m_productlineText;

		private final String m_metricText;

		private Boolean m_available = true;

		public HeartbeatRuleItem(String id, String productlineText, String metricText) {
			m_id = id;
			m_productlineText = productlineText;
			m_metricText = metricText;
		}

		public Boolean getAvailable() {
			return m_available;
		}

		public void setAvailable(Boolean available) {
			m_available = available;
		}

		public String getId() {
			return m_id;
		}

		public String getMetricText() {
			return m_metricText;
		}

		public String getProductlineText() {
			return m_productlineText;
		}
	}
}
