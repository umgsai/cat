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

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

public abstract class BaseHistoricalModelService<T> extends ModelServiceWithCalSupport
						implements ModelService<T> {

	@Resource(name = "serverConfigManager")
	protected ServerConfigManager serverConfigManager;

	private boolean localMode = true;

	private String name;

	private volatile boolean initialized;

	public BaseHistoricalModelService(String name) {
		this.name = name;
	}

	protected abstract T buildModel(ModelRequest request) throws Exception;

	@Override
	public String getName() {
		return name;
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
		localMode = serverConfigManager.isLocalMode();
		initialized = true;
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		ensureInitialized();

		ModelResponse<T> response = new ModelResponse<T>();
		Transaction t = newTransaction("ModelService", getClass().getSimpleName());
		t.addData("thread", Thread.currentThread());

		try {
			T model = buildModel(request);

			response.setModel(model);
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}

		return response;
	}

	@Override
	public boolean isEligible(ModelRequest request) {
		ensureInitialized();

		return request.getPeriod().isHistorical();
	}

	protected boolean isLocalMode() {
		return localMode;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		serverConfigManager = configManager;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(name).append(']');

		return sb.toString();
	}
}
