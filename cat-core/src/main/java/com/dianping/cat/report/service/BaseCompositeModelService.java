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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.server.RemoteServersManager;
import jakarta.annotation.Resource;

public abstract class BaseCompositeModelService<T> extends ModelServiceWithCalSupport
						implements ModelService<T> {

	@Resource(name = "serverConfigManager")
	protected ServerConfigManager serverConfigManager;

	@Resource(name = "remoteServersManager")
	private RemoteServersManager remoteServersManager;

	private List<ModelService<T>> services;

	private List<ModelService<T>> allServices = new ArrayList<ModelService<T>>();

	private String name;

	private volatile boolean initialized;

	public BaseCompositeModelService(String name) {
		this.name = name;
	}

	protected abstract BaseRemoteModelService<T> createRemoteService();

	@Override
	public String getName() {
		return name;
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	public synchronized void initialize() {
		if (initialized) {
			return;
		}

		if (serverConfigManager == null) {
			throw new IllegalStateException("ServerConfigManager is required for " + getClass().getSimpleName() + ".");
		}
		if (remoteServersManager == null) {
			throw new IllegalStateException("RemoteServersManager is required for " + getClass().getSimpleName() + ".");
		}
		allServices.clear();

		if (services != null) {
			allServices.addAll(services);
		}

		String remoteServers = serverConfigManager.getConsoleRemoteServers();
		List<String> endpoints = splitEndpoints(remoteServers);

		for (String endpoint : endpoints) {
			int pos = endpoint.indexOf(':');
			String host = buildHost(endpoint, pos);
			int port = (pos > 0 ? Integer.parseInt(endpoint.substring(pos + 1)) : 2281);
			BaseRemoteModelService<T> remote = createRemoteService();

			remote.setHost(host);
			remote.setPort(port);
			remote.setServerConfigManager(serverConfigManager);
			remote.setRemoteServersManager(remoteServersManager);
			allServices.add(remote);
		}
		initialized = true;
	}

	private String buildHost(String endpoint, int pos) {
		String host = (pos > 0 ? endpoint.substring(0, pos) : endpoint);

		if ("127.0.0.1".equals(host)) {
			host = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		}
		return host;
	}

	private List<String> splitEndpoints(String remoteServers) {
		List<String> endpoints = new ArrayList<String>();

		if (remoteServers != null) {
			for (String endpoint : remoteServers.split(",")) {
				String value = endpoint.trim();

				if (value.length() > 0) {
					endpoints.add(value);
				}
			}
		}

		return endpoints;
	}

	@Override
	public ModelResponse<T> invoke(final ModelRequest request) {
		ensureInitialized();

		int requireSize = 0;
		final List<ModelResponse<T>> responses = Collections.synchronizedList(new ArrayList<ModelResponse<T>>());
		final Semaphore semaphore = new Semaphore(0);
		final Transaction t = Cat.newTransaction("ModelService", getClass().getSimpleName());
		int count = 0;

		t.setStatus(Message.SUCCESS);
		t.addData("request", request);
		t.addData("thread", Thread.currentThread());

		for (final ModelService<T> service : allServices) {
			if (!service.isEligible(request)) {
				continue;
			}

			// save current transaction so that child thread can access it
			if (service instanceof ModelServiceWithCalSupport) {
				((ModelServiceWithCalSupport) service).setParentTransaction(t);
			}
			requireSize++;

			serverConfigManager.getModelServiceExecutorService().submit(new Runnable() {
				@Override
				public void run() {
					try {
						ModelResponse<T> response = service.invoke(request);

						if (response.getException() != null) {
							logError(response.getException());
						}
						if (response != null && response.getModel() != null) {
							responses.add(response);
						}
					} catch (Exception e) {
						logError(e);
						t.setStatus(e);
					} finally {
						semaphore.release();
					}
				}
			});

			count++;
		}

		try {
			semaphore.tryAcquire(count, 10000, TimeUnit.MILLISECONDS); // 10 seconds timeout
		} catch (InterruptedException e) {
			// ignore it
			t.setStatus(e);
		} finally {
			t.complete();
		}

		String requireAll = request.getProperty("requireAll");

		if (requireAll != null && responses.size() != requireSize) {
			String data = "require:" + requireSize + " actual:" + responses.size();
			Cat.logEvent("FetchReportError:" + this.getClass().getSimpleName(), request.getDomain(), Event.SUCCESS, data);

			return null;
		}
		ModelResponse<T> aggregated = new ModelResponse<T>();
		T report = merge(request, responses);

		aggregated.setModel(report);
		return aggregated;
	}

	@Override
	public boolean isEligible(ModelRequest request) {
		ensureInitialized();

		for (ModelService<T> service : allServices) {
			if (service.isEligible(request)) {
				return true;
			}
		}

		return false;
	}

	protected abstract T merge(ModelRequest request, final List<ModelResponse<T>> responses);

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(name);
		sb.append(']');

		return sb.toString();
	}

	public void setServices(List<ModelService<T>> services) {
		this.services = services;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		serverConfigManager = configManager;
	}

	public void setServerManager(RemoteServersManager serverManager) {
		remoteServersManager = serverManager;
	}
}
