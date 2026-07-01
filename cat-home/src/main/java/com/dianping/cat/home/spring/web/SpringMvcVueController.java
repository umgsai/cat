package com.dianping.cat.home.spring.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcVueController {
	@GetMapping({ "/mvc/vue", "/mvc/vue/", "/mvc/vue/**" })
	public void vue(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/assets/vue/index.html");

		dispatcher.forward(request, response);
	}
}
