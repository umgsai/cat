/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.system.page.permission;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.dianping.cat.home.spring.CatHomeSpringContextListener;
import com.dianping.cat.system.page.login.service.SigninContext;
import com.dianping.cat.system.page.login.service.Token;
import com.dianping.cat.system.page.login.service.TokenManager;

public class PermissionFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionFilter.class);

	private static final String LOG_IN_URL = "/cat/s/login";

	private static final String LOGIN = "login";

	private static final String OP = "op";

	private static final String DEFAULT_OP = "view";

	private UserConfigManager m_userConfigManager;

	private ResourceConfigManager m_resourceConfigManager;

	private TokenManager m_tokenManager;

	private String m_errorPage;

	private String m_loginPage;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		resolveSpringBeans(filterConfig);

		if (m_userConfigManager == null || m_resourceConfigManager == null || m_tokenManager == null) {
			throw new ServletException(String.format(
			      "PermissionFilter dependencies must be configured by Spring. userConfig=%s, resourceConfig=%s, tokenManager=%s",
			      m_userConfigManager != null, m_resourceConfigManager != null, m_tokenManager != null));
		} else {
			LOGGER.info("PermissionFilter dependencies resolved from Spring.");
		}

		m_errorPage = filterConfig.getInitParameter("errorPage");
		m_loginPage = filterConfig.getInitParameter(LOGIN);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
							throws IOException,	ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (m_userConfigManager == null || m_resourceConfigManager == null || m_tokenManager == null) {
			throw new ServletException("PermissionFilter dependencies must be configured by Spring.");
		}

		httpRequest.setCharacterEncoding("utf-8");

		SigninContext ctx = new SigninContext(httpRequest, httpResponse);
		String requestURI = httpRequest.getRequestURI();

		if (isLoginRequest(httpRequest) || isVueDataRequest(httpRequest)) {
			chain.doFilter(request, response);
		} else if (isVueSystemPageRequest(httpRequest)) {
			Token token = m_tokenManager.getToken(ctx, Token.TOKEN);

			if (token == null) {
				httpResponse.sendRedirect(loginUrl(httpRequest));
			} else {
				chain.doFilter(request, response);
			}
		} else {

			String op = httpRequest.getParameter(OP);

			if (op == null) {
				op = DEFAULT_OP;
			}

			int resourceRole = resourceRole(httpRequest, requestURI, op);

			if (resourceRole == ResourceConfigManager.DEFAULT_RESOURCE_ROLE) {
				chain.doFilter(request, response);
			} else {
				Token token = m_tokenManager.getToken(ctx, Token.TOKEN);

				if (token == null) {
					httpResponse.sendRedirect(loginUrl(httpRequest));
				} else {
					int userRole = m_userConfigManager.getRole(token.getUserName());

					if (userRole >= resourceRole) {
						chain.doFilter(request, response);
					} else {
						if (isVueSourceRequest(httpRequest)) {
							httpResponse.sendRedirect(vuePermissionErrorUrl(httpRequest));
						} else {
							request.getRequestDispatcher(m_errorPage).forward(request, response);
						}
					}
				}
			}
		}
	}

	private String currentUrl(HttpServletRequest request) {
		StringBuilder url = new StringBuilder(request.getRequestURI());
		String queryString = request.getQueryString();

		if (queryString != null && queryString.length() > 0) {
			url.append('?').append(queryString);
		}
		return url.toString();
	}

	private boolean isLoginRequest(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();

		return LOG_IN_URL.equals(requestURI) || (contextPath + "/s/login").equals(requestURI)
				|| (contextPath + "/mvc/s/login").equals(requestURI)
				|| (contextPath + "/mvc/vue/s/login").equals(requestURI);
	}

	private boolean isVueSystemPageRequest(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();

		return "GET".equalsIgnoreCase(request.getMethod()) && requestURI.startsWith(contextPath + "/mvc/vue/s/");
	}

	private boolean isVueDataRequest(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();

		if (!"GET".equalsIgnoreCase(request.getMethod()) || !"vueData".equals(request.getParameter(OP))) {
			return false;
		}
		return (contextPath + "/mvc/s/config").equals(requestURI)
				|| (contextPath + "/mvc/s/business").equals(requestURI);
	}

	private boolean isVueSourceRequest(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String referer = request.getHeader("Referer");

		return "true".equals(request.getParameter("vue"))
				|| (referer != null && referer.contains(contextPath + "/mvc/vue/"));
	}

	private String loginUrl(HttpServletRequest request) {
		String rtnUrl = currentUrl(request);
		StringBuilder url = new StringBuilder(request.getContextPath()).append(m_loginPage);

		if (rtnUrl.length() > 0) {
			url.append("?rtnUrl=").append(URLEncoder.encode(rtnUrl, StandardCharsets.UTF_8));
		}
		return url.toString();
	}

	private String vuePermissionErrorUrl(HttpServletRequest request) {
		return request.getContextPath() + "/mvc/vue/s/permission?op=error";
	}

	private int resourceRole(HttpServletRequest request, String requestURI, String op) {
		int role = configuredRole(requestURI, op);

		if (role != ResourceConfigManager.DEFAULT_RESOURCE_ROLE) {
			return role;
		}

		String contextPath = request.getContextPath();
		String path = requestURI;

		if (contextPath != null && contextPath.length() > 0 && path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
			role = configuredRole(path, op);

			if (role != ResourceConfigManager.DEFAULT_RESOURCE_ROLE) {
				return role;
			}
		}

		String legacyPath = legacyPath(path);

		if (!legacyPath.equals(path)) {
			role = configuredRole(legacyPath, op);

			if (role != ResourceConfigManager.DEFAULT_RESOURCE_ROLE) {
				return role;
			}
			if (contextPath != null && contextPath.length() > 0) {
				return configuredRole(contextPath + legacyPath, op);
			}
		}
		return role;
	}

	private int configuredRole(String path, String op) {
		int role = m_resourceConfigManager.getRole(path, op);

		if (role == ResourceConfigManager.DEFAULT_RESOURCE_ROLE && !DEFAULT_OP.equals(op)) {
			return m_resourceConfigManager.getRole(path, DEFAULT_OP);
		}
		return role;
	}

	private String legacyPath(String path) {
		if (path.startsWith("/mvc/vue/s/")) {
			return "/s/" + path.substring("/mvc/vue/s/".length());
		}
		if (path.startsWith("/mvc/vue/r/")) {
			return "/r/" + path.substring("/mvc/vue/r/".length());
		}
		if (path.startsWith("/mvc/s/")) {
			return "/s/" + path.substring("/mvc/s/".length());
		}
		if (path.startsWith("/mvc/r/")) {
			return "/r/" + path.substring("/mvc/r/".length());
		}
		return path;
	}

	@Override
	public void destroy() {
	}

	private void resolveSpringBeans(FilterConfig filterConfig) throws ServletException {
		ApplicationContext context = (ApplicationContext) filterConfig.getServletContext()
		      .getAttribute(CatHomeSpringContextListener.ATTRIBUTE_NAME);

		if (context == null) {
			throw new ServletException("CAT home Spring context is not initialized.");
		}
		m_userConfigManager = context.getBean(UserConfigManager.class);
		m_resourceConfigManager = context.getBean(ResourceConfigManager.class);
		m_tokenManager = context.getBean(TokenManager.class);
	}

}
