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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;
import com.dianping.cat.home.resource.transform.DefaultSaxParser;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Component
public class ResourceConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceConfigManager.class);

	public static final int DEFAULT_RESOURCE_ROLE = 1;

	private static final String CONFIG_NAME = "resource-config";

	private static final String ALL = "*";

	@jakarta.annotation.Resource
	protected ConfigRepository configRepository;

	@jakarta.annotation.Resource
	protected ContentFetcher contentFetcher;

	private long configId;

	private long modifyTime;

	private ResourceConfig config;

	private volatile Map<String, Map<String, Integer>> permissions = new ConcurrentHashMap<String, Map<String, Integer>>();

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	public ResourceConfig getConfig() {
		ensureInitialized();

		return config;
	}

	public int getRole(String path, String op) {
		ensureInitialized();

		Map<String, Integer> pathPermission = permissions.get(path);

		if (pathPermission == null) {
			pathPermission = permissions.get(ALL);
		}

		if (pathPermission != null) {
			Integer role = pathPermission.get(op);

			if (role == null) {
				role = pathPermission.get(ALL);
			}

			if (role != null) {
				return role;
			}
		}

		return DEFAULT_RESOURCE_ROLE;
	}

	@PostConstruct
	public void initialize() {
		try {
			Config config = configRepository.findByName(CONFIG_NAME);
			String content = config.getContent();

			configId = config.getId();
			modifyTime = config.getModifyDate().getTime();
			this.config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded resource config from repository, configId={}, modifyTime={}.", configId,
					modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Resource config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				Config config = configRepository.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				configRepository.insert(config);
				configId = config.getId();
				this.config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized resource config from default content, configId={}.", configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize resource config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load resource config from repository.", e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new ResourceConfig();
			LOGGER.warn("Resource config is empty after initialization, using a new empty config.");
		}
		refreshData();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return CONFIG_NAME;
			}

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}

		});
	}

	public boolean insert(String xml) {
		try {
			config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse resource config xml for insert. xmlLength={}.",
					xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	private void refreshConfig() throws Exception {
		Config config = configRepository.findByName(CONFIG_NAME);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > this.modifyTime) {
				String content = config.getContent();
				ResourceConfig resourceConfig = DefaultSaxParser.parse(content);
				this.config = resourceConfig;
				this.modifyTime = modifyTime;

				refreshData();
				LOGGER.info("Refreshed resource config, configId={}, modifyTime={}, resourceCount={}.", configId,
						this.modifyTime, this.config.getResources().size());
			}
		}
	}

	private void ensureInitialized() {
		if (config == null) {
			synchronized (this) {
				if (config == null) {
					LOGGER.warn("Resource config is not initialized yet, loading it lazily.");
					initialize();
				}
			}
		}
	}

	private void refreshData() {
		Map<String, Map<String, Integer>> permissions = new ConcurrentHashMap<String, Map<String, Integer>>();

		for (Resource resource : config.getResources()) {
			String path = resource.getPath();
			Map<String, Integer> pathPermission = permissions.get(path);

			if (pathPermission == null) {
				pathPermission = new ConcurrentHashMap<String, Integer>();
				permissions.put(path, pathPermission);
			}

			pathPermission.put(resource.getOp(), resource.getRole());
		}

		this.permissions = permissions;
		LOGGER.info("Rebuilt resource permissions cache, pathCount={}.", permissions.size());
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = configRepository.createLocal();

				config.setId(configId);
				config.setKeyId(configId);
				config.setName(CONFIG_NAME);
				config.setContent(this.config.toString());
				configRepository.updateByPK(config);

				refreshData();
				LOGGER.info("Stored resource config, configId={}, resourceCount={}.", configId,
						this.config.getResources().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store resource config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
