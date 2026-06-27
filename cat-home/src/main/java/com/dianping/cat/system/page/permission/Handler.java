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

import jakarta.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.springframework.stereotype.Component;

import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

@Component("systemPermissionHandler")
public class Handler implements PageHandler<Context> {

	@Resource
	private JspViewer jspViewer;

	@Resource
	private UserConfigManager userConfigManager;

	@Resource
	private ResourceConfigManager resourceConfigManager;

	@Resource
	private ConfigHtmlParser configHtmlParser;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "permission")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "permission")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		model.setAction(action);
		model.setPage(SystemPage.PERMISSION);

		switch (action) {
		case USER:
			String userConfig = payload.getContent();
			if (!StringUtils.isEmpty(userConfig)) {
				model.setOpState(userConfigManager.insert(userConfig));
			}
			model.setContent(configHtmlParser.parse(userConfigManager.getConfig().toString()));
			break;
		case RESOURCE:
			String resourceConfig = payload.getContent();
			if (!StringUtils.isEmpty(resourceConfig)) {
				model.setOpState(resourceConfigManager.insert(resourceConfig));
			}
			model.setContent(configHtmlParser.parse(resourceConfigManager.getConfig().toString()));
			break;
		case ERROR:
			break;
		}
		if (!ctx.isProcessStopped()) {
			jspViewer.view(ctx, model);
		}
	}
}
