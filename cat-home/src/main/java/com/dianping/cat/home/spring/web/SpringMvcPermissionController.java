package com.dianping.cat.home.spring.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.permission.ResourceConfigManager;
import com.dianping.cat.system.page.permission.UserConfigManager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SpringMvcPermissionController {
	private static final String FAIL = "Fail";

	private static final String SUCCESS = "Success";

	@Resource
	private ConfigHtmlParser m_configHtmlParser;

	@Resource
	private ResourceConfigManager m_resourceConfigManager;

	@Resource
	private UserConfigManager m_userConfigManager;

	@RequestMapping(value = "/s/permission", method = { RequestMethod.GET, RequestMethod.POST })
	public void permission(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = action(request);

		if (!"user".equals(action) && !"resource".equals(action) && !"error".equals(action)) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		request.setAttribute("contextPath", request.getContextPath());
		request.setAttribute("permissionUrl", request.getContextPath() + "/mvc/s/permission");
		request.setAttribute("actionName", action);

		if ("user".equals(action)) {
			prepareUser(request);
		} else if ("resource".equals(action)) {
			prepareResource(request);
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(jsp(action));

		dispatcher.forward(request, response);
	}

	String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		if (action == null || action.length() == 0) {
			return "user";
		}
		return action;
	}

	void setConfigHtmlParser(ConfigHtmlParser configHtmlParser) {
		m_configHtmlParser = configHtmlParser;
	}

	void setResourceConfigManager(ResourceConfigManager resourceConfigManager) {
		m_resourceConfigManager = resourceConfigManager;
	}

	void setUserConfigManager(UserConfigManager userConfigManager) {
		m_userConfigManager = userConfigManager;
	}

	String jsp(String action) {
		if ("resource".equals(action)) {
			return "/jsp/spring/system/permission/resourceConfigUpdate.jsp";
		}
		if ("error".equals(action)) {
			return "/jsp/spring/system/permission/error.jsp";
		}
		return "/jsp/spring/system/permission/userConfigUpdate.jsp";
	}

	private void prepareResource(HttpServletRequest request) {
		String content = request.getParameter("content");

		if (StringUtils.isNotEmpty(content)) {
			request.setAttribute("opState", m_resourceConfigManager.insert(content) ? SUCCESS : FAIL);
		}
		request.setAttribute("content", m_configHtmlParser.parse(m_resourceConfigManager.getConfig().toString()));
	}

	private void prepareUser(HttpServletRequest request) {
		String content = request.getParameter("content");

		if (StringUtils.isNotEmpty(content)) {
			request.setAttribute("opState", m_userConfigManager.insert(content) ? SUCCESS : FAIL);
		}
		request.setAttribute("content", m_configHtmlParser.parse(m_userConfigManager.getConfig().toString()));
	}
}
