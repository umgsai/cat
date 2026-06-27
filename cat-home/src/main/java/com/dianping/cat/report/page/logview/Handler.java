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
package com.dianping.cat.report.page.logview;

import javax.servlet.ServletException;
import java.io.IOException;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.tree.MessageId;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Component("logviewHandler")
public class Handler implements PageHandler<Context> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

	@Resource
	private JspViewer jspViewer;

	@Resource(name = "logviewModelService")
	private ModelService<String> logviewModelService;

	@Resource
	private ServerConfigManager serverConfigManager;

	private boolean checkStorageTime(MessageId msg) {
		long time = msg.getTimestamp();
		long current = TimeHelper.getCurrentDay().getTime();

		if (time > current - TimeHelper.ONE_DAY * serverConfigManager.getHdfsMaxStorageTime()) {
			return true;
		} else {
			return false;
		}
	}

	private String getLogView(String messageId, boolean waterfall) {
		try {
			if (messageId != null) {
				MessageId id = MessageId.parse(messageId);
				long timestamp = id.getTimestamp();
				ModelRequest request = new ModelRequest(id.getDomain(), timestamp) //
										.setProperty("messageId", messageId) //
										.setProperty("waterfall", String.valueOf(waterfall)) //
										.setProperty("timestamp", String.valueOf(timestamp));

				if (logviewModelService.isEligable(request)) {
					ModelResponse<String> response = logviewModelService.invoke(request);
					String logview = response.getModel();

					return logview;
				} else {
					throw new RuntimeException("Internal error: no eligible logview service registered for " + request + "!");
				}
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load logview, messageId={}, waterfall={}.", messageId, waterfall, e);
			Cat.logError(e);
			return null;
		}

		return null;
	}

	private String getMessageId(Payload payload) {
		String[] path = payload.getPath();

		if (path != null && path.length > 0) {
			return path[0];
		} else {
			return null;
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "m")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "m")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(payload.getAction());
		model.setPage(ReportPage.LOGVIEW);
		model.setDomain(payload.getDomain());
		model.setDate(payload.getDate());

		String messageId = getMessageId(payload);
		String logView = null;
		MessageId msgId = MessageId.parse(messageId);

		LOGGER.info("Handling logview outbound, action={}, domain={}, messageId={}, waterfall={}.", payload.getAction(),
				payload.getDomain(), messageId, payload.isWaterfall());
		if (checkStorageTime(msgId)) {
			logView = getLogView(messageId, payload.isWaterfall());

			if (logView == null || logView.length() == 0) {
				LOGGER.warn("Logview not found, domain={}, messageId={}, waterfall={}.", msgId.getDomain(), messageId,
						payload.isWaterfall());
				Cat.logEvent("Logview", msgId.getDomain() + ":Fail", Event.SUCCESS, messageId);
			} else {
				LOGGER.info("Logview loaded, domain={}, messageId={}, length={}.", msgId.getDomain(), messageId,
						logView.length());
				Cat.logEvent("Logview", "Success", Event.SUCCESS, messageId);
			}
		} else {
			LOGGER.warn("Logview message is outside storage window, domain={}, messageId={}, timestamp={}.",
					msgId.getDomain(), messageId, msgId.getTimestamp());
			Cat.logEvent("Logview", "OldMessage", Event.SUCCESS, messageId);
		}

		switch (payload.getAction()) {
		case VIEW:
			model.setTable(logView);
			break;
		}

		jspViewer.view(ctx, model);
	}
}
