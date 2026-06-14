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
import com.dianping.cat.spring.CatSpringContext;

public abstract class BaseHistoricalModelService<T> extends ModelServiceWithCalSupport
						implements ModelService<T> {

	protected ServerConfigManager m_configManager;

	private boolean m_localMode = true;

	private String m_name;

	private volatile boolean m_initialized;

	public BaseHistoricalModelService(String name) {
		m_name = name;
	}

	protected abstract T buildModel(ModelRequest request) throws Exception;

	@Override
	public String getName() {
		return m_name;
	}

	private void ensureInitialized() {
		if (!m_initialized) {
			initialize();
		}
	}

	public synchronized void initialize() {
		if (m_initialized) {
			return;
		}

		ServerConfigManager configManager = CatSpringContext.getBeanIfAvailable(ServerConfigManager.class);

		if (configManager != null) {
			m_configManager = configManager;
		}
		m_localMode = m_configManager.isLocalMode();
		m_initialized = true;
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
	public boolean isEligable(ModelRequest request) {
		ensureInitialized();

		return request.getPeriod().isHistorical();
	}

	protected boolean isLocalMode() {
		return m_localMode;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		m_configManager = configManager;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name).append(']');

		return sb.toString();
	}
}
