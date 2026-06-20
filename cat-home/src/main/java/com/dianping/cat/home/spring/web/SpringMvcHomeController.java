package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcHomeController {
	@GetMapping("/mvc/r/home")
	public void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = homeModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/home/home.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> homeModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String docName = docName(request);

		model.put("docName", docName);
		model.put("webapp", request.getContextPath());
		model.put("domain", parameter(request, "domain", "cat"));
		model.put("ipAddress", parameter(request, "ip", "All"));
		model.put("date", parameter(request, "date", ""));
		model.put("reportType", parameter(request, "reportType", "day"));
		model.put("actionName", parameter(request, "op", "view"));
		model.put("runtime", "spring-mvc-migration");
		model.put("homeUrl", request.getContextPath() + "/mvc/r/home");
		model.put("loginUrl", request.getContextPath() + "/mvc/s/login");
		model.put("model", model);
		return model;
	}

	private String docName(HttpServletRequest request) {
		String docName = request.getParameter("docName");

		if (docName == null || docName.length() == 0) {
			return "index";
		}
		return docName;
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}
}
