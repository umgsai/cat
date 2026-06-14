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
package com.dianping.cat.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.home.spring.CatHomeSpringContextListener;

public class CatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CatServlet.class);

	private Exception m_exception;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		try {
			ApplicationContext context = (ApplicationContext) servletConfig.getServletContext()
					.getAttribute(CatHomeSpringContextListener.ATTRIBUTE_NAME);

			if (context == null) {
				throw new IllegalStateException("CAT home Spring context is not initialized.");
			}
			LOGGER.info("CAT servlet initialized with Spring context, beanCount={}.", context.getBeanDefinitionCount());
		} catch (Exception e) {
			m_exception = e;
			LOGGER.error("Unable to initialize CAT servlet components.", e);
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setCharacterEncoding("utf-8");
		res.setContentType("text/plain");

		PrintWriter writer = res.getWriter();

		if (m_exception != null) {
			writer.write("Server has NOT been initialized successfully!\r\n\r\n");
			m_exception.printStackTrace(writer);
		} else {
			writer.write("Not implemented yet!");
		}
	}
}
