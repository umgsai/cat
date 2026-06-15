package com.dianping.cat.mvc;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.dianping.cat.home.spring.CatHomeSpringContextListener;

public class SpringMvcServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcServlet.class);

	private static final long serialVersionUID = 1L;

	private SpringMvcRuntime m_runtime;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ApplicationContext context = (ApplicationContext) config.getServletContext()
		      .getAttribute(CatHomeSpringContextListener.ATTRIBUTE_NAME);

		if (context == null) {
			throw new ServletException("CAT home Spring context is not initialized.");
		}

		try {
			m_runtime = new SpringMvcRuntime(context, config.getServletContext());
			config.getServletContext().setAttribute("mvc-servlet", this);
			LOGGER.info("Spring MVC bridge servlet initialized.");
		} catch (RuntimeException e) {
			LOGGER.error("Spring MVC bridge servlet initializing failed.", e);
			throw e;
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getCharacterEncoding() == null) {
			request.setCharacterEncoding("UTF-8");
		}

		response.setContentType("text/html;charset=UTF-8");
		m_runtime.handle(request, response);
	}
}
