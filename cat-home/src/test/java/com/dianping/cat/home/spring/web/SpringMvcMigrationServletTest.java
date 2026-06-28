package com.dianping.cat.home.spring.web;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.dianping.cat.home.spring.CatHomeSpringContextListener;

public class SpringMvcMigrationServletTest {
	@Test
	public void shouldInjectControllersWithResourceAnnotationsDuringServletInit() throws Exception {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			registerControllerBeans(context);
			context.refresh();

			SpringMvcMigrationServlet servlet = new SpringMvcMigrationServlet();

			servlet.init(servletConfig(context));

			Map<?, ?> routes = routes(servlet);

			Assert.assertTrue(routes.containsKey(new SpringMvcMigrationServlet.RouteKey("GET", "/r/t")));
			Assert.assertTrue(routes.containsKey(new SpringMvcMigrationServlet.RouteKey("POST", "/s/config")));
			Assert.assertTrue(routes.containsKey(new SpringMvcMigrationServlet.RouteKey("GET", "/r/model/*")));
		}
	}

	private void registerControllerBeans(AnnotationConfigApplicationContext context) {
		context.getBeanFactory().registerSingleton("springMvcAlterationController", new SpringMvcAlterationController());
		context.getBeanFactory().registerSingleton("springMvcAlertController", new SpringMvcAlertController());
		context.getBeanFactory().registerSingleton("springMvcBusinessController", new SpringMvcBusinessController());
		context.getBeanFactory().registerSingleton("springMvcBusinessReportController",
				new SpringMvcBusinessReportController());
		context.getBeanFactory().registerSingleton("springMvcCacheController", new SpringMvcCacheController());
		context.getBeanFactory().registerSingleton("springMvcConfigController", new SpringMvcConfigController());
		context.getBeanFactory().registerSingleton("springMvcCrossController", new SpringMvcCrossController());
		context.getBeanFactory().registerSingleton("springMvcDependencyController", new SpringMvcDependencyController());
		context.getBeanFactory().registerSingleton("springMvcEventController", new SpringMvcEventController());
		context.getBeanFactory().registerSingleton("springMvcHealthController", new SpringMvcHealthController());
		context.getBeanFactory().registerSingleton("springMvcHeartbeatController", new SpringMvcHeartbeatController());
		context.getBeanFactory().registerSingleton("springMvcHomeController", new SpringMvcHomeController());
		context.getBeanFactory().registerSingleton("springMvcLoginController", new SpringMvcLoginController());
		context.getBeanFactory().registerSingleton("springMvcLogviewController", new SpringMvcLogviewController());
		context.getBeanFactory().registerSingleton("springMvcMatrixController", new SpringMvcMatrixController());
		context.getBeanFactory().registerSingleton("springMvcModelController", new SpringMvcModelController());
		context.getBeanFactory().registerSingleton("springMvcMonitorController", new SpringMvcMonitorController());
		context.getBeanFactory().registerSingleton("springMvcOverloadController", new SpringMvcOverloadController());
		context.getBeanFactory().registerSingleton("springMvcPermissionController", new SpringMvcPermissionController());
		context.getBeanFactory().registerSingleton("springMvcPluginController", new SpringMvcPluginController());
		context.getBeanFactory().registerSingleton("springMvcProjectController", new SpringMvcProjectController());
		context.getBeanFactory().registerSingleton("springMvcProblemController", new SpringMvcProblemController());
		context.getBeanFactory().registerSingleton("springMvcRouterController", new SpringMvcRouterController());
		context.getBeanFactory().registerSingleton("springMvcStateController", new SpringMvcStateController());
		context.getBeanFactory().registerSingleton("springMvcStatisticsController", new SpringMvcStatisticsController());
		context.getBeanFactory().registerSingleton("springMvcStorageController", new SpringMvcStorageController());
		context.getBeanFactory().registerSingleton("springMvcTopController", new SpringMvcTopController());
		context.getBeanFactory().registerSingleton("springMvcTransactionController", new SpringMvcTransactionController());
	}

	private ServletConfig servletConfig(AnnotationConfigApplicationContext context) {
		ServletContext servletContext = (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { ServletContext.class }, new ServletContextHandler(context));

		return (ServletConfig) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { ServletConfig.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getServletContext".equals(method.getName())) {
							return servletContext;
						}
						if ("getServletName".equals(method.getName())) {
							return "spring-mvc-migration-servlet";
						}
						if ("getInitParameterNames".equals(method.getName())) {
							return Collections.emptyEnumeration();
						}
						return null;
					}
				});
	}

	private Map<?, ?> routes(SpringMvcMigrationServlet servlet) throws Exception {
		Field field = SpringMvcMigrationServlet.class.getDeclaredField("routes");

		field.setAccessible(true);
		return (Map<?, ?>) field.get(servlet);
	}

	private static class ServletContextHandler implements InvocationHandler {
		private final AnnotationConfigApplicationContext context;

		ServletContextHandler(AnnotationConfigApplicationContext context) {
			this.context = context;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			if ("getAttribute".equals(method.getName()) && CatHomeSpringContextListener.ATTRIBUTE_NAME.equals(args[0])) {
				return context;
			}
			if ("getInitParameterNames".equals(method.getName())) {
				return Collections.emptyEnumeration();
			}
			return null;
		}
	}
}
