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
package com.dianping.cat.report.page.model;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.message.tree.MessageId;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

@SuppressWarnings("rawtypes")
@Component("modelHandler")
public class Handler implements PageHandler<Context> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

	@Resource(name = "localModelServices")
	private Map<String, LocalModelService> localServices;

	private volatile boolean initialized;

	private byte[] compress(String str) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 32);
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes(StandardCharsets.UTF_8));
		gzip.close();
		return out.toByteArray();
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "model")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "model")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		initialize();

		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		HttpServletResponse httpResponse = ctx.getHttpServletResponse();

		model.setAction(Action.XML);
		model.setPage(ReportPage.MODEL);

		try {
			String report = payload.getReport();
			String domain = payload.getDomain();
			ModelPeriod period = payload.getPeriod();
			ModelRequest request = null;

			if ("logview".equals(report)) {
				request = new ModelRequest(domain, MessageId.parse(payload.getMessageId()).getTimestamp());
			} else {
				request = new ModelRequest(domain, period.getStartTime());
			}
			String xml = "";
			LocalModelService service = localServices.get(report);

			if (service != null) {
				xml = service.getReport(request, period, domain, payload);
			} else {
				throw new RuntimeException("Unsupported report: " + report + "!");
			}

			if (xml != null) {
				ServletOutputStream outputStream = httpResponse.getOutputStream();
				byte[] compress = compress(xml);

				httpResponse.setContentType("application/xml;charset=utf-8");
				httpResponse.addHeader("Content-Encoding", "gzip");
				outputStream.write(compress);
			}
		} catch (Throwable e) {
			LOGGER.error("Unable to render model report, report={}, domain={}, period={}, messageId={}.",
			      payload.getReport(), payload.getDomain(), payload.getPeriod(), payload.getMessageId(), e);
			Cat.logError(e);
		}
	}

	public synchronized void initialize() {
		if (!initialized) {
			if (localServices == null || localServices.isEmpty()) {
				localServices = new HashMap<String, LocalModelService>();
				LOGGER.warn("Model page handler has no local model services configured.");
			} else {
				localServices = new HashMap<String, LocalModelService>(localServices);
				LOGGER.info("Initialized model page handler from Spring injection, localServiceCount={}.",
				      localServices.size());
			}
			initialized = true;
		}
	}

	public Map<String, LocalModelService> getLocalServices() {
		initialize();
		return Collections.unmodifiableMap(localServices);
	}

}
