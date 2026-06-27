package com.dianping.cat.home.spring.web;

import java.io.IOException;

import jakarta.annotation.Resource;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.dianping.cat.system.page.login.service.Credential;
import com.dianping.cat.system.page.login.service.Session;
import com.dianping.cat.system.page.login.service.SigninContext;
import com.dianping.cat.system.page.login.service.SigninService;

@Controller
public class SpringMvcLoginController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcLoginController.class);

	@Resource
	private SigninService signinService;

	@GetMapping("/s/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String op = request.getParameter("op");

		if ("logout".equals(op)) {
			signinService.signout(new SigninContext(request, response));
			response.sendRedirect(defaultReturnUrl(request));
		} else {
			forwardLogin(request, response, null);
		}
	}

	@PostMapping("/s/login")
	public void submit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String account = request.getParameter("account");
		String password = request.getParameter("password");

		if (account != null && account.length() > 0 && password != null) {
			Session session = signinService.signin(new SigninContext(request, response),
					new Credential(account, password));

			if (session != null) {
				LOGGER.info("User login succeeded on Spring MVC migration path, account={}.", account);
				response.sendRedirect(resolveReturnUrl(request));
				return;
			}
			LOGGER.warn("User login failed on Spring MVC migration path, account={}.", account);
		} else {
			LOGGER.warn("User login input is incomplete on Spring MVC migration path, accountPresent={}, passwordPresent={}.",
					account != null, password != null);
		}

		forwardLogin(request, response, "biz.login");
	}

	private String defaultReturnUrl(HttpServletRequest request) {
		return request.getContextPath() + "/mvc/r/home";
	}

	private void forwardLogin(HttpServletRequest request, HttpServletResponse response, String error)
			throws ServletException, IOException {
		if (error != null) {
			request.setAttribute("loginError", error);
		}
		if (request.getAttribute("rtnUrl") == null) {
			request.setAttribute("rtnUrl", request.getParameter("rtnUrl"));
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/system/login.jsp");

		dispatcher.forward(request, response);
	}

	private String resolveReturnUrl(HttpServletRequest request) {
		String rtnUrl = request.getParameter("rtnUrl");

		if (rtnUrl == null || rtnUrl.length() == 0 || rtnUrl.contains("/cat/s/login")
				|| rtnUrl.contains("/cat/mvc/s/login")) {
			return defaultReturnUrl(request);
		}
		return rtnUrl;
	}
}
