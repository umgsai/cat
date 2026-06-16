package com.dianping.cat.home.spring.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.unidal.webres.resource.runtime.ResourceConfigurator;
import org.unidal.webres.resource.runtime.ResourceInitializer;
import org.unidal.webres.resource.runtime.ResourceRuntime;
import org.unidal.webres.resource.runtime.ResourceRuntimeContext;
import org.unidal.webres.resource.spi.IResourceRegistry;
import org.unidal.webres.tag.resource.ResourceTagConfigurator;
import org.unidal.webres.taglib.basic.ResourceTagLibConfigurator;

public final class SpringMvcWebResourceInitializer {
	private SpringMvcWebResourceInitializer() {
	}

	public static void initialize(HttpServletRequest request) {
		String contextPath = request.getContextPath();

		synchronized (ResourceRuntime.INSTANCE) {
			if (!ResourceRuntime.INSTANCE.hasConfig(contextPath)) {
				ServletContext servletContext = request.getSession().getServletContext();
				File warRoot = new File(servletContext.getRealPath("/"));

				ResourceRuntime.INSTANCE.removeConfig(contextPath);
				ResourceInitializer.initialize(contextPath, warRoot);

				IResourceRegistry registry = ResourceRuntime.INSTANCE.getConfig(contextPath).getRegistry();

				new ResourceConfigurator().configure(registry);
				new ResourceTagConfigurator().configure(registry);
				new ResourceTagLibConfigurator().configure(registry);
				registry.lock();
			}

			ResourceRuntimeContext.setup(contextPath);
		}
	}
}
