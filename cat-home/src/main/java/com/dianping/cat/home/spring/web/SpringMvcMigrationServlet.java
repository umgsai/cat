package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
import org.springframework.context.ApplicationContext;

import com.dianping.cat.home.spring.CatHomeSpringContextListener;

public class SpringMvcMigrationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private SpringMvcHealthController m_healthController;

	private SpringMvcHomeController m_homeController;

	private SpringMvcLoginController m_loginController;

	private Map<RouteKey, RouteHandler> m_routes = Collections.emptyMap();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ApplicationContext context = (ApplicationContext) config.getServletContext()
				.getAttribute(CatHomeSpringContextListener.ATTRIBUTE_NAME);

		if (context == null) {
			throw new ServletException("CAT home Spring context is not initialized.");
		}
		m_healthController = context.getBean(SpringMvcHealthController.class);
		m_homeController = context.getBean(SpringMvcHomeController.class);
		m_loginController = context.getBean(SpringMvcLoginController.class);
		m_routes = buildRoutes();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handle("GET", request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handle("POST", request, response);
	}

	private Map<RouteKey, RouteHandler> buildRoutes() {
		Map<RouteKey, RouteHandler> routes = new LinkedHashMap<RouteKey, RouteHandler>();

		register(routes, "GET", "/", (request, response) -> writeJson(response, m_healthController.health()));
		register(routes, "GET", "/health", (request, response) -> writeJson(response, m_healthController.health()));
		register(routes, "GET", "/r/home", m_homeController::home);
		register(routes, "GET", "/s/login", m_loginController::login);
		register(routes, "POST", "/s/login", m_loginController::submit);

		return Collections.unmodifiableMap(routes);
	}

	private void handle(String method, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RouteHandler handler = m_routes.get(new RouteKey(method, normalizePath(request.getPathInfo())));

		if (handler == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		handler.handle(request, response);
	}

	private String normalizePath(String path) {
		if (path == null || path.length() == 0) {
			return "/";
		}
		return path;
	}

	private void register(Map<RouteKey, RouteHandler> routes, String method, String path, RouteHandler handler) {
		routes.put(new RouteKey(method, path), handler);
	}

	private void writeJson(HttpServletResponse response, Map<String, Object> model) throws IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");

		try (PrintWriter writer = response.getWriter()) {
			writer.write(JSON.toJSONString(model));
		}
	}

	interface RouteHandler {
		void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	}

	static class RouteKey {
		private final String m_method;

		private final String m_path;

		RouteKey(String method, String path) {
			m_method = method;
			m_path = path;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof RouteKey)) {
				return false;
			}

			RouteKey other = (RouteKey) obj;

			return m_method.equals(other.m_method) && m_path.equals(other.m_path);
		}

		@Override
		public int hashCode() {
			return m_method.hashCode() * 31 + m_path.hashCode();
		}
	}
}
