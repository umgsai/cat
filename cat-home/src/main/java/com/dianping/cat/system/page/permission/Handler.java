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

import javax.servlet.ServletException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.spring.CatSpringContext;

public class Handler implements PageHandler<Context> {

	private JspViewer m_jspViewer;

	private UserConfigManager m_userConfigManager;

	private ResourceConfigManager m_resourceConfigManager;

	private ConfigHtmlParser m_configHtmlParser;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "permission")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "permission")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		refreshSpringBeans();

		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		model.setAction(action);
		model.setPage(SystemPage.PERMISSION);

		switch (action) {
		case USER:
			String userConfig = payload.getContent();
			if (!StringUtils.isEmpty(userConfig)) {
				model.setOpState(m_userConfigManager.insert(userConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_userConfigManager.getConfig().toString()));
			break;
		case RESOURCE:
			String resourceConfig = payload.getContent();
			if (!StringUtils.isEmpty(resourceConfig)) {
				model.setOpState(m_resourceConfigManager.insert(resourceConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_resourceConfigManager.getConfig().toString()));
			break;
		case ERROR:
			break;
		}
		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void refreshSpringBeans() {
		UserConfigManager userConfigManager = CatSpringContext.getBeanIfAvailable(UserConfigManager.class);
		ResourceConfigManager resourceConfigManager = CatSpringContext.getBeanIfAvailable(ResourceConfigManager.class);
		ConfigHtmlParser configHtmlParser = CatSpringContext.getBeanIfAvailable(ConfigHtmlParser.class);

		if (userConfigManager != null) {
			m_userConfigManager = userConfigManager;
		}
		if (resourceConfigManager != null) {
			m_resourceConfigManager = resourceConfigManager;
		}
		if (configHtmlParser != null) {
			m_configHtmlParser = configHtmlParser;
		}
	}

	public void setConfigHtmlParser(ConfigHtmlParser configHtmlParser) {
		m_configHtmlParser = configHtmlParser;
	}

	public void setJspViewer(JspViewer jspViewer) {
		m_jspViewer = jspViewer;
	}

	public void setResourceConfigManager(ResourceConfigManager resourceConfigManager) {
		m_resourceConfigManager = resourceConfigManager;
	}

	public void setUserConfigManager(UserConfigManager userConfigManager) {
		m_userConfigManager = userConfigManager;
	}
}
