package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
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

		if ("vueData".equals(action)) {
			vueData(request, response);
			return;
		}
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
		if (isVueMutationRequest(request, action)) {
			response.sendRedirect(vueRedirectUrl(request, action, request.getAttribute("opState")));
			return;
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

	private String content(String action) {
		if ("resource".equals(action)) {
			return m_resourceConfigManager.getConfig().toString();
		}
		if ("user".equals(action)) {
			return m_userConfigManager.getConfig().toString();
		}
		return "";
	}

	private boolean isVueMutationRequest(HttpServletRequest request, String action) {
		return "true".equals(request.getParameter("vue"))
				&& ("resource".equals(action) || "user".equals(action))
				&& request.getParameter("content") != null;
	}

	private String vueAction(HttpServletRequest request) {
		String action = request.getParameter("vueAction");

		if (action == null || action.length() == 0) {
			action = request.getParameter("op");
		}
		if (!"resource".equals(action) && !"user".equals(action) && !"error".equals(action)) {
			return "resource";
		}
		return action;
	}

	private void vueData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String action = vueAction(request);
		Map<String, Object> json = new LinkedHashMap<String, Object>();

		json.put("contextPath", request.getContextPath());
		json.put("actionName", action);
		json.put("content", content(action));
		json.put("opState", request.getParameter("opState"));

		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(JSON.toJSONString(json));
	}

	private String vueRedirectUrl(HttpServletRequest request, String action, Object opState) {
		StringBuilder url = new StringBuilder(request.getContextPath()).append("/mvc/vue/s/permission?op=")
				.append(URLEncoder.encode(action, StandardCharsets.UTF_8));

		if (opState != null) {
			url.append("&opState=").append(URLEncoder.encode(String.valueOf(opState), StandardCharsets.UTF_8));
		}
		return url.toString();
	}
}
