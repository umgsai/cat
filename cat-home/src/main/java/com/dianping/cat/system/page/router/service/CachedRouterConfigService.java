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
package com.dianping.cat.system.page.router.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Constants;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

public class CachedRouterConfigService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CachedRouterConfigService.class);

	private RouterConfigService m_routerConfigService;

	private volatile RouterConfig m_routerConfig;

	private volatile boolean m_initialized;

	public synchronized void initialize() {
		if (m_initialized) {
			return;
		}

		refresh();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return "router-refresh-task";
			}

			@Override
			public void handle() throws Exception {
				refresh();
			}
		});
		m_initialized = true;
		LOGGER.info("Initialized cached router config service.");
	}

	public RouterConfig queryLastRouterConfig() {
		initialize();

		return m_routerConfig;
	}

	public void refresh() {
		if (m_routerConfigService == null) {
			LOGGER.warn("Skip router config refresh because RouterConfigService is unavailable.");
			return;
		}
		m_routerConfig = m_routerConfigService.queryLastReport(Constants.CAT);
	}

	public void setRouterConfigService(RouterConfigService routerConfigService) {
		m_routerConfigService = routerConfigService;
	}
}
