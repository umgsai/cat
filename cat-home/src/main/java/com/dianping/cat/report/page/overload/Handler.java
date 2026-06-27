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
package com.dianping.cat.report.page.overload;

import javax.servlet.ServletException;
import java.io.IOException;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.overload.task.TableCapacityService;

@Component("overloadHandler")
public class Handler implements PageHandler<Context> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

	@Resource
	private JspViewer jspViewer;

	@Resource
	private TableCapacityService tableCapacityService;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "overload")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "overload")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			try {
				model.setReports(tableCapacityService.queryOverloadReports(payload.getStartTime(), payload.getEndTime()));
			} catch (Exception e) {
				LOGGER.error("Unable to query overload reports, startTime={}, endTime={}.", payload.getStartTime(),
				      payload.getEndTime(), e);
				Cat.logError(e);
			}
			break;
		}

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.OVERLOAD);

		if (!ctx.isProcessStopped()) {
			jspViewer.view(ctx, model);
		}
	}

}
