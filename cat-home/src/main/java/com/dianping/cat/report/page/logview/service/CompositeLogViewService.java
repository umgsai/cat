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
package com.dianping.cat.report.page.logview.service;

import java.util.Arrays;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Component("logviewModelService")
public class CompositeLogViewService extends BaseCompositeModelService<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompositeLogViewService.class);

	@Resource(name = "localMessageService")
	private ModelService<String> localMessageService;

	@Resource(name = "historicalMessageService")
	private ModelService<String> historicalMessageService;

	public CompositeLogViewService() {
		super("logview");
	}

	@Override
	@PostConstruct
	public synchronized void initialize() {
		if (localMessageService != null && historicalMessageService != null) {
			setServices(Arrays.asList(localMessageService, historicalMessageService));
		}
		super.initialize();
	}

	@Override
	protected BaseRemoteModelService<String> createRemoteService() {
		RemoteLogViewService service = new RemoteLogViewService();

		service.setManager(serverConfigManager);
		return service;
	}

	@Override
	protected String merge(ModelRequest request, List<ModelResponse<String>> responses) {
		for (ModelResponse<String> response : responses) {
			if (response != null) {
				String model = response.getModel();

				if (model != null) {
					return model;
				}
			}
		}

		LOGGER.warn("Composite logview service returned empty result, messageId={}, domain={}, period={}, responseCount={}.",
				request.getProperty("messageId"), request.getDomain(), request.getPeriod(), responses.size());
		return null;
	}
}
