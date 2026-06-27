package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.service.ProjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcProjectController {
	@Resource
	private ProjectService projectService;

	@GetMapping("/s/project")
	public void project(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String action = action(request);

		if ("projectUpdate".equals(action)) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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

	void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	private void writeJson(HttpServletResponse response, String content) throws IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(content == null ? "" : content);
	}
}
