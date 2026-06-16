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
package com.dianping.cat;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.servlet.GzipFilter;

@RunWith(JUnit4.class)
public class TestServer {
	private Server m_server;

	public static void main(String[] args) throws Exception {
		TestServer server = new TestServer();
		System.setProperty("devMode", "true");

		try {
			server.startServer();
			server.display("/cat/r/t");
			server.waitForAnyKey();
		} finally {
			server.stopServer();
		}
	}

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
	}

	protected String getContextPath() {
		return "/cat";
	}

	protected int getServerPort() {
		return 8080;
	}

	protected void postConfigure(WebAppContext context) {
		context.addFilter(GzipFilter.class, "/*", Handler.ALL);
	}

	protected void startServer() throws Exception {
		Server server = new Server(getServerPort());
		WebAppContext context = new WebAppContext();
		File warRoot = getWarRoot();

		context.getInitParams().put("org.mortbay.jetty.servlet.Default.dirAllowed", "false");
		context.setContextPath(getContextPath());
		context.setDescriptor(new File(warRoot, "WEB-INF/web.xml").getPath());
		context.setWar(warRoot.getPath());
		postConfigure(context);
		server.addHandler(context);
		server.start();

		m_server = server;
	}

	protected void stopServer() throws Exception {
		if (m_server != null) {
			m_server.stop();
			m_server = null;
		}
	}

	private void display(String requestUri) throws Exception {
		URI uri = new URI("http://localhost:" + getServerPort() + requestUri);

		System.out.println(uri);
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(uri);
		}
	}

	private File getWarRoot() {
		String warRoot = System.getProperty("warRoot");

		if (warRoot != null) {
			return new File(warRoot);
		}
		return new File("src/main/webapp");
	}

	private void waitForAnyKey() throws IOException {
		String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());

		System.out.println(String.format("[%s] [INFO] Press ENTER to stop server ... ", timestamp));
		System.in.read();
	}

	@Test
	public void startWebApp() throws Exception {
		try {
			startServer();
			display("/cat/r/t");
			waitForAnyKey();
		} finally {
			stopServer();
		}
	}

}
