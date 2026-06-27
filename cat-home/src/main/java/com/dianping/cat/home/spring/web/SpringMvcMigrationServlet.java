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

	private SpringMvcHeartbeatController m_heartbeatController;

	private SpringMvcBusinessController m_businessController;

	private SpringMvcBusinessReportController m_businessReportController;

	private SpringMvcConfigController m_configController;

	private SpringMvcCrossController m_crossController;

	private SpringMvcEventController m_eventController;

	private SpringMvcHomeController m_homeController;

	private SpringMvcLoginController m_loginController;

	private SpringMvcLogviewController m_logviewController;

	private SpringMvcPermissionController m_permissionController;

	private SpringMvcPluginController m_pluginController;

	private SpringMvcProjectController m_projectController;

	private SpringMvcProblemController m_problemController;

	private SpringMvcRouterController m_routerController;

	private SpringMvcStateController m_stateController;

	private SpringMvcTopController m_topController;

	private SpringMvcTransactionController m_transactionController;

	private Map<RouteKey, RouteHandler> m_routes = Collections.emptyMap();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ApplicationContext context = (ApplicationContext) config.getServletContext()
				.getAttribute(CatHomeSpringContextListener.ATTRIBUTE_NAME);

		if (context == null) {
			throw new ServletException("CAT home Spring context is not initialized.");
		}
		m_businessController = context.getBean(SpringMvcBusinessController.class);
		m_businessReportController = context.getBean(SpringMvcBusinessReportController.class);
		m_configController = context.getBean(SpringMvcConfigController.class);
		m_crossController = context.getBean(SpringMvcCrossController.class);
		m_eventController = context.getBean(SpringMvcEventController.class);
		m_healthController = context.getBean(SpringMvcHealthController.class);
		m_heartbeatController = context.getBean(SpringMvcHeartbeatController.class);
		m_homeController = context.getBean(SpringMvcHomeController.class);
		m_loginController = context.getBean(SpringMvcLoginController.class);
		m_logviewController = context.getBean(SpringMvcLogviewController.class);
		m_permissionController = context.getBean(SpringMvcPermissionController.class);
		m_pluginController = context.getBean(SpringMvcPluginController.class);
		m_projectController = context.getBean(SpringMvcProjectController.class);
		m_problemController = context.getBean(SpringMvcProblemController.class);
		m_routerController = context.getBean(SpringMvcRouterController.class);
		m_stateController = context.getBean(SpringMvcStateController.class);
		m_topController = context.getBean(SpringMvcTopController.class);
		m_transactionController = context.getBean(SpringMvcTransactionController.class);
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
		register(routes, "GET", "/s/business", m_businessController::business);
		register(routes, "GET", "/s/config", m_configController::config);
		register(routes, "GET", "/s/permission", m_permissionController::permission);
		register(routes, "GET", "/s/plugin", m_pluginController::plugin);
		register(routes, "GET", "/s/plugin/chrome", m_pluginController::chrome);
		register(routes, "GET", "/s/project", m_projectController::project);
		register(routes, "GET", "/s/router", m_routerController::router);
		register(routes, "GET", "/r/business", m_businessReportController::business);
		register(routes, "GET", "/r/cross", m_crossController::cross);
		register(routes, "GET", "/r/m/*", m_logviewController::logview);
		register(routes, "GET", "/r/top", m_topController::top);
		register(routes, "GET", "/r/t", m_transactionController::transaction);
		register(routes, "GET", "/r/e", m_eventController::event);
		register(routes, "GET", "/r/h", m_heartbeatController::heartbeat);
		register(routes, "GET", "/r/p", m_problemController::problem);
		register(routes, "GET", "/r/state", m_stateController::state);
		register(routes, "POST", "/s/business", m_businessController::business);
		register(routes, "POST", "/s/config", m_configController::config);
		register(routes, "POST", "/s/login", m_loginController::submit);
		register(routes, "POST", "/s/permission", m_permissionController::permission);

		return Collections.unmodifiableMap(routes);
	}

	private void handle(String method, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = normalizePath(request.getPathInfo());
		RouteHandler handler = m_routes.get(new RouteKey(method, path));

		if (handler == null) {
			handler = wildcardRoute(method, path);
		}

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

	private RouteHandler wildcardRoute(String method, String path) {
		for (Map.Entry<RouteKey, RouteHandler> entry : m_routes.entrySet()) {
			RouteKey key = entry.getKey();
			String routePath = key.m_path;

			if (key.m_method.equals(method) && routePath.endsWith("/*")
					&& path.startsWith(routePath.substring(0, routePath.length() - 1))) {
				return entry.getValue();
			}
		}
		return null;
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
