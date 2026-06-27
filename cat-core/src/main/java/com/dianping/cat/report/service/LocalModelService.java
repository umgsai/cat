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
package com.dianping.cat.report.service;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.mvc.ApiPayload;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

public abstract class LocalModelService<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalModelService.class);

	public static final int DEFAULT_SIZE = 32 * 1024;

	@Resource(name = "serverConfigManager")
	protected ServerConfigManager serverConfigManager;

	@Resource(name = "messageConsumer")
	private MessageConsumer messageConsumer;

	private int analyzerCount = 2;

	private String defaultDomain = Constants.CAT;

	private String name;

	private volatile boolean initialized;

	public LocalModelService(String name) {
		this.name = name;
	}

	public abstract String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception;

	public int getAnalyzerCount() {
		return analyzerCount;
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	protected List<T> getReport(ModelPeriod period, String domain) throws Exception {
		ensureInitialized();

		List<MessageAnalyzer> analyzers = null;

		if (domain == null || domain.length() == 0) {
			domain = defaultDomain;
		}

		if (messageConsumer == null) {
			LOGGER.warn("Message consumer is not configured for local model service, service={}, period={}, domain={}.",
			      name, period, domain);
			return null;
		}
		if (period.isCurrent()) {
			analyzers = messageConsumer.getCurrentAnalyzer(name);
		} else if (period.isLast()) {
			analyzers = messageConsumer.getLastAnalyzer(name);
		}

		if (analyzers == null) {
			return null;
		} else {
			List<T> list = new ArrayList<T>();

			for (MessageAnalyzer a : analyzers) {
				list.add(((AbstractMessageAnalyzer<T>) a).getReport(domain));
			}
			return list;
		}
	}

	public String getReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)	throws Exception {
		ensureInitialized();

		try {
			return buildReport(request, period, domain, payload);
		} catch (ConcurrentModificationException e) {
			return buildReport(request, period, domain, payload);
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	@PostConstruct
	public synchronized void initialize() {
		if (initialized) {
			return;
		}

		if (serverConfigManager == null) {
			throw new IllegalStateException("ServerConfigManager is required for " + getClass().getSimpleName() + ".");
		}
		if (messageConsumer == null) {
			throw new IllegalStateException("MessageConsumer is required for " + getClass().getSimpleName() + ".");
		}
		defaultDomain = serverConfigManager.getConsoleDefaultDomain();
		analyzerCount = serverConfigManager.getThreadsOfRealtimeAnalyzer(name);
		initialized = true;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		serverConfigManager = configManager;
	}

	public void setConsumer(MessageConsumer consumer) {
		messageConsumer = consumer;
	}

	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		return !period.isHistorical();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(name);
		sb.append(']');

		return sb.toString();
	}
}
