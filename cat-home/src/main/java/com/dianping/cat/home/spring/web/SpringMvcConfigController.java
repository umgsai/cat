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
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.login.service.Session;
import com.dianping.cat.system.page.login.service.SigninContext;
import com.dianping.cat.system.page.login.service.SigninService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcConfigController {
	@Resource
	private ProjectService m_projectService;

	@Resource
	private DomainGroupConfigManager m_domainGroupConfigManager;

	@Resource
	private SigninService m_signinService;

	@GetMapping("/s/config")
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
				|| "projectDelete".equals(action) || isDomainGroupAction(action);
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

	private String jspPath(String action) {
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
