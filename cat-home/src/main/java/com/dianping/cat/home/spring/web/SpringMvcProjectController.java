package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.mybatis.data.ProjectDO;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.project.UpdateStatus;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcProjectController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcProjectController.class);

	@Resource
	private ProjectService projectService;

	@GetMapping("/s/project")
	public void project(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String action = action(request);

		if ("projectUpdate".equals(action)) {
			writeJson(response, updateProject(request));
			return;
		}

		writeJson(response, domainsJson());
	}

	String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		if (action == null || action.length() == 0) {
			return "domains";
		}
		return action;
	}

	String domainsJson() {
		JsonBuilder builder = new JsonBuilder();
		Set<String> domains = projectService.findAllDomains();
		Map<String, Object> jsons = new HashMap<String, Object>();

		jsons.put("domains", domains);
		return builder.toJson(jsons);
	}

	ProjectDO project(HttpServletRequest request) {
		ProjectDO project = new ProjectDO();

		project.setDomain(parameter(request, "project.domain", null));
		project.setCmdbDomain(parameter(request, "project.cmdbDomain", null));
		project.setLevel(intParameter(request, "project.level", 0));
		project.setBu(parameter(request, "project.bu", null));
		project.setCmdbProductline(parameter(request, "project.cmdbProductline", null));
		project.setOwner(parameter(request, "project.owner", null));
		project.setEmail(parameter(request, "project.email", null));
		project.setPhone(parameter(request, "project.phone", null));
		return project;
	}

	String updateProject(HttpServletRequest request) {
		ProjectDO project = project(request);

		try {
			if (project.getDomain() == null) {
				project.setDomain(Constants.CAT);
			}

			ProjectDO existing = projectService.findByDomain(project.getDomain());

			if (existing == null) {
				projectService.insert(project);
				LOGGER.info("Inserted project config, domain={}.", project.getDomain());
			} else {
				projectService.update(project);
				LOGGER.info("Updated project config, domain={}.", project.getDomain());
			}
			return UpdateStatus.SUCCESS.getStatusJson();
		} catch (Exception e) {
			LOGGER.error("Unable to update project config, domain={}.", project.getDomain(), e);
			Cat.logError(e);
			return UpdateStatus.INTERNAL_ERROR.getStatusJson();
		}
	}

	void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	private int intParameter(HttpServletRequest request, String name, int defaultValue) {
		String value = request.getParameter(name);

		if (value != null && value.length() > 0) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	private void writeJson(HttpServletResponse response, String content) throws IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(content == null ? "" : content);
	}
}
