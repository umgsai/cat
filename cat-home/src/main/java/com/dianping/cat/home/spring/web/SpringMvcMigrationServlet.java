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
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;

import com.dianping.cat.home.spring.CatHomeSpringContextListener;

public class SpringMvcMigrationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Resource
	private SpringMvcHealthController springMvcHealthController;

	@Resource
	private SpringMvcAlterationController springMvcAlterationController;

	@Resource
	private SpringMvcAlertController springMvcAlertController;

	@Resource
	private SpringMvcHeartbeatController springMvcHeartbeatController;

	@Resource
	private SpringMvcBusinessController springMvcBusinessController;

	@Resource
	private SpringMvcBusinessReportController springMvcBusinessReportController;

	@Resource
	private SpringMvcCacheController springMvcCacheController;

	@Resource
	private SpringMvcConfigController springMvcConfigController;

	@Resource
	private SpringMvcCrossController springMvcCrossController;

	@Resource
	private SpringMvcDependencyController springMvcDependencyController;

	@Resource
	private SpringMvcEventController springMvcEventController;

	@Resource
	private SpringMvcHomeController springMvcHomeController;

	@Resource
	private SpringMvcLoginController springMvcLoginController;

	@Resource
	private SpringMvcLogviewController springMvcLogviewController;

	@Resource
	private SpringMvcMatrixController springMvcMatrixController;

	@Resource
	private SpringMvcModelController springMvcModelController;

	@Resource
	private SpringMvcMonitorController springMvcMonitorController;

	@Resource
	private SpringMvcOverloadController springMvcOverloadController;

	@Resource
	private SpringMvcPermissionController springMvcPermissionController;

	@Resource
	private SpringMvcPluginController springMvcPluginController;

	@Resource
	private SpringMvcProjectController springMvcProjectController;

	@Resource
	private SpringMvcProblemController springMvcProblemController;

	@Resource
	private SpringMvcRouterController springMvcRouterController;

	@Resource
	private SpringMvcStateController springMvcStateController;

	@Resource
	private SpringMvcStatisticsController springMvcStatisticsController;

	@Resource
	private SpringMvcStorageController springMvcStorageController;

	@Resource
	private SpringMvcTopController springMvcTopController;

	@Resource
	private SpringMvcTransactionController springMvcTransactionController;

	private Map<RouteKey, RouteHandler> routes = Collections.emptyMap();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ApplicationContext context = (ApplicationContext) config.getServletContext()
				.getAttribute(CatHomeSpringContextListener.ATTRIBUTE_NAME);

		if (context == null) {
			throw new ServletException("CAT home Spring context is not initialized.");
		}
		context.getAutowireCapableBeanFactory().autowireBean(this);
		routes = buildRoutes();
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

		register(routes, "GET", "/", (request, response) -> writeJson(response, springMvcHealthController.health()));
		register(routes, "GET", "/health",
				(request, response) -> writeJson(response, springMvcHealthController.health()));
		register(routes, "GET", "/r/home", springMvcHomeController::home);
		register(routes, "GET", "/s/login", springMvcLoginController::login);
		register(routes, "GET", "/s/business", springMvcBusinessController::business);
		register(routes, "GET", "/s/config", springMvcConfigController::config);
		register(routes, "GET", "/s/permission", springMvcPermissionController::permission);
		register(routes, "GET", "/s/plugin", springMvcPluginController::plugin);
		register(routes, "GET", "/s/plugin/chrome", springMvcPluginController::chrome);
		register(routes, "GET", "/s/project", springMvcProjectController::project);
		register(routes, "GET", "/s/router", springMvcRouterController::router);
		register(routes, "GET", "/r/alteration", springMvcAlterationController::alteration);
		register(routes, "GET", "/r/alert", springMvcAlertController::alert);
		register(routes, "GET", "/r/business", springMvcBusinessReportController::business);
		register(routes, "GET", "/r/cache", springMvcCacheController::cache);
		register(routes, "GET", "/r/cross", springMvcCrossController::cross);
		register(routes, "GET", "/r/dependency", springMvcDependencyController::dependency);
		register(routes, "GET", "/r/m/*", springMvcLogviewController::logview);
		register(routes, "GET", "/r/matrix", springMvcMatrixController::matrix);
		register(routes, "GET", "/r/model", springMvcModelController::model);
		register(routes, "GET", "/r/model/*", springMvcModelController::model);
		register(routes, "GET", "/r/monitor", springMvcMonitorController::monitor);
		register(routes, "POST", "/r/monitor", springMvcMonitorController::monitor);
		register(routes, "GET", "/r/overload", springMvcOverloadController::overload);
		register(routes, "GET", "/r/top", springMvcTopController::top);
		register(routes, "GET", "/r/t", springMvcTransactionController::transaction);
		register(routes, "GET", "/r/e", springMvcEventController::event);
		register(routes, "GET", "/r/h", springMvcHeartbeatController::heartbeat);
		register(routes, "GET", "/r/p", springMvcProblemController::problem);
		register(routes, "GET", "/r/state", springMvcStateController::state);
		register(routes, "GET", "/r/statistics", springMvcStatisticsController::statistics);
		register(routes, "GET", "/r/storage", springMvcStorageController::storage);
		register(routes, "POST", "/s/business", springMvcBusinessController::business);
		register(routes, "POST", "/s/config", springMvcConfigController::config);
		register(routes, "POST", "/s/login", springMvcLoginController::submit);
		register(routes, "POST", "/s/permission", springMvcPermissionController::permission);
		register(routes, "POST", "/r/alteration", springMvcAlterationController::alteration);
		register(routes, "POST", "/r/alert", springMvcAlertController::alert);
		register(routes, "POST", "/r/statistics", springMvcStatisticsController::submit);

		return Collections.unmodifiableMap(routes);
	}

	private void handle(String method, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = normalizePath(request.getPathInfo());
		RouteHandler handler = routes.get(new RouteKey(method, path));

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
		for (Map.Entry<RouteKey, RouteHandler> entry : routes.entrySet()) {
			RouteKey key = entry.getKey();
			String routePath = key.path;

			if (key.method.equals(method) && routePath.endsWith("/*")
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
		private final String method;

		private final String path;

		RouteKey(String method, String path) {
			this.method = method;
			this.path = path;
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

			return method.equals(other.method) && path.equals(other.path);
		}

		@Override
		public int hashCode() {
			return method.hashCode() * 31 + path.hashCode();
		}
	}
}
