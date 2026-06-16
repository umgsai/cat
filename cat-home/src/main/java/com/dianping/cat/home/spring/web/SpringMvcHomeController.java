package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;

public class SpringMvcHomeController {
	@GetMapping("/r/home")
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
		String docName = request.getParameter("docName");

		if (docName == null || docName.length() == 0) {
			docName = "index";
		}

		model.put("docName", docName);
		model.put("runtime", "spring-mvc-migration");
		model.put("legacyHomeUrl", request.getContextPath() + "/r/home");
		model.put("loginUrl", request.getContextPath() + "/mvc/s/login");

		return model;
	}
}
