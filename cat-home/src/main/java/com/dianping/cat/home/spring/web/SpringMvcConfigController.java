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

import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.login.service.Session;
import com.dianping.cat.system.page.login.service.SigninContext;
import com.dianping.cat.system.page.login.service.SigninService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SpringMvcConfigController {
	@Resource
	private ProjectService m_projectService;

	@Resource
	private DomainGroupConfigManager m_domainGroupConfigManager;

	@Resource
	private HeartbeatDisplayPolicyManager m_displayPolicyManager;

	@Resource
	private ConfigHtmlParser m_configHtmlParser;

	@Resource
	private TransactionRuleConfigManager m_transactionRuleConfigManager;

	@Resource
	private EventRuleConfigManager m_eventRuleConfigManager;

	@Resource
	private ExceptionRuleConfigManager m_exceptionRuleConfigManager;

	@Resource
	private RuleFTLDecorator m_ruleDecorator;

	@Resource
	private SigninService m_signinService;

	@RequestMapping(value = "/s/config", method = { RequestMethod.GET, RequestMethod.POST })
	public void config(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Session session = m_signinService.validate(new SigninContext(request, response));

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
		if (isExceptionAction(action)) {
			configExceptionModel(request, action, model);
			return model;
		}
		if (isEventRuleAction(action)) {
			configEventRuleModel(request, action, model);
			return model;
		}
		if (isTransactionRuleAction(action)) {
			configTransactionRuleModel(request, action, model);
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
			selected = m_projectService.findByDomain(domain);

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
		m_projectService = projectService;
	}

	void setDomainGroupConfigManager(DomainGroupConfigManager domainGroupConfigManager) {
		m_domainGroupConfigManager = domainGroupConfigManager;
	}

	void setDisplayPolicyManager(HeartbeatDisplayPolicyManager displayPolicyManager) {
		m_displayPolicyManager = displayPolicyManager;
	}

	void setConfigHtmlParser(ConfigHtmlParser configHtmlParser) {
		m_configHtmlParser = configHtmlParser;
	}

	void setTransactionRuleConfigManager(TransactionRuleConfigManager transactionRuleConfigManager) {
		m_transactionRuleConfigManager = transactionRuleConfigManager;
	}

	void setEventRuleConfigManager(EventRuleConfigManager eventRuleConfigManager) {
		m_eventRuleConfigManager = eventRuleConfigManager;
	}

	void setExceptionRuleConfigManager(ExceptionRuleConfigManager exceptionRuleConfigManager) {
		m_exceptionRuleConfigManager = exceptionRuleConfigManager;
	}

	void setRuleDecorator(RuleFTLDecorator ruleDecorator) {
		m_ruleDecorator = ruleDecorator;
	}

	void setSigninService(SigninService signinService) {
		m_signinService = signinService;
	}

	private boolean deleteProject(long id) {
		if (id <= 0) {
			return false;
		}

		Project project = new Project();

		project.setId(id);
		project.setKeyId(id);
		return m_projectService.delete(project);
	}

	private boolean isSupported(String action) {
		return "projects".equals(action) || "projectAdd".equals(action) || "updateSubmit".equals(action)
				|| "projectDelete".equals(action) || "displayPolicy".equals(action) || isDomainGroupAction(action)
				|| isExceptionAction(action) || isEventRuleAction(action) || isTransactionRuleAction(action);
	}

	private void configDomainGroupModel(HttpServletRequest request, String action, Map<String, Object> model,
			String domain) {
		Boolean opState = null;

		if ("domainGroupConfigDelete".equals(action)) {
			opState = m_domainGroupConfigManager.deleteGroup(domain);
		} else if ("domainGroupConfigSubmit".equals(action)) {
			opState = m_domainGroupConfigManager.insertFromJson(parameter(request, "content", ""));
		}

		DomainGroup domainGroup = m_domainGroupConfigManager.getDomainGroup();
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
			opState = m_displayPolicyManager.insert(content);
		} else if (request.getParameter("submit") != null) {
			opState = true;
		}
		model.put("content", m_configHtmlParser.parse(m_displayPolicyManager.getHeartbeatDisplayPolicy().toString()));
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

	private void configTransactionRuleListModel(Map<String, Object> model) {
		Map<String, Rule> rules = m_transactionRuleConfigManager.getMonitorRules().getRules();

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
			rule = m_transactionRuleConfigManager.queryRule(ruleId);
		}
		if (rule != null) {
			configs = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
			if (rule.getAvailable() != null) {
				available = rule.getAvailable();
			}
		}
		model.put("ruleId", ruleId);
		model.put("available", available);
		model.put("content", m_ruleDecorator.generateConfigsHtml(configs));
	}

	private void configEventRuleListModel(Map<String, Object> model) {
		Map<String, Rule> rules = m_eventRuleConfigManager.getMonitorRules().getRules();

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
			rule = m_eventRuleConfigManager.queryRule(ruleId);
		}
		if (rule != null) {
			configs = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
			if (rule.getAvailable() != null) {
				available = rule.getAvailable();
			}
		}
		model.put("ruleId", ruleId);
		model.put("available", available);
		model.put("content", m_ruleDecorator.generateConfigsHtml(configs));
	}

	private void configExceptionListModel(Map<String, Object> model) {
		List<ExceptionLimit> exceptionLimits = m_exceptionRuleConfigManager.queryAllExceptionLimits();
		List<ExceptionExclude> exceptionExcludes = m_exceptionRuleConfigManager.queryAllExceptionExcludes();

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
			exceptionLimit = m_exceptionRuleConfigManager.queryExceptionLimit(domain, exception);
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

			Domain groupDomain = m_domainGroupConfigManager.queryGroupDomain(domain);

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

	private boolean isEventRuleAction(String action) {
		return "eventRule".equals(action) || "eventRuleUpdate".equals(action) || "eventRuleSubmit".equals(action)
				|| "eventRuleDelete".equals(action);
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
		if ("transactionRuleUpdate".equals(action)) {
			return "/jsp/spring/report/config/transactionRuleUpdate.jsp";
		}
		if (isTransactionRuleAction(action)) {
			return "/jsp/spring/report/config/transactionRule.jsp";
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
			String xml = m_transactionRuleConfigManager.updateRule(ruleId, "", configs, available);

			return m_transactionRuleConfigManager.insert(xml);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean deleteTransactionRule(String ruleId) {
		try {
			String xml = m_transactionRuleConfigManager.deleteRule(ruleId);

			return m_transactionRuleConfigManager.insert(xml);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean updateEventRule(String ruleId, String configs, Boolean available) {
		try {
			String xml = m_eventRuleConfigManager.updateRule(ruleId, "", configs, available);

			return m_eventRuleConfigManager.insert(xml);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean deleteEventRule(String ruleId) {
		try {
			String xml = m_eventRuleConfigManager.deleteRule(ruleId);

			return m_eventRuleConfigManager.insert(xml);
		} catch (Exception e) {
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
			return m_exceptionRuleConfigManager.insertExceptionLimit(exceptionLimit);
		} catch (Exception e) {
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
			return m_exceptionRuleConfigManager.insertExceptionExclude(exceptionExclude);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean deleteExceptionLimit(String domain, String exception) {
		try {
			return m_exceptionRuleConfigManager.deleteExceptionLimit(domain, exception);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean deleteExceptionExclude(String domain, String exception) {
		try {
			return m_exceptionRuleConfigManager.deleteExceptionExclude(domain, exception);
		} catch (Exception e) {
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
		List<Project> projects = new ArrayList<Project>(m_projectService.findAll());

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
			return m_projectService.update(project);
		}
		return m_projectService.insert(project);
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
}
