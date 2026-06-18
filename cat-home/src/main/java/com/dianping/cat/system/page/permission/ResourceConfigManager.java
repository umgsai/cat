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

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;
import com.dianping.cat.home.resource.transform.DefaultSaxParser;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

public class ResourceConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceConfigManager.class);

	public static final int DEFAULT_RESOURCE_ROLE = 1;

	private static final String CONFIG_NAME = "resource-config";

	private static final String ALL = "*";

	protected ConfigRepository m_configDao;

	protected ContentFetcher m_fetcher;

	private long m_configId;

	private long m_modifyTime;

	private ResourceConfig m_config;

	private volatile Map<String, Map<String, Integer>> m_permissions = new ConcurrentHashMap<String, Map<String, Integer>>();

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public ResourceConfig getConfig() {
		ensureInitialized();

		return m_config;
	}

	public int getRole(String path, String op) {
		ensureInitialized();

		Map<String, Integer> pathPermission = m_permissions.get(path);

		if (pathPermission == null) {
			pathPermission = m_permissions.get(ALL);
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

	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME);
			String content = config.getContent();

			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
			m_config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded resource config from repository, configId={}, modifyTime={}.", m_configId,
					m_modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Resource config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized resource config from default content, configId={}.", m_configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize resource config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load resource config from repository.", e);
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new ResourceConfig();
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
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse resource config xml for insert. xmlLength={}.",
					xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				ResourceConfig resourceConfig = DefaultSaxParser.parse(content);
				m_config = resourceConfig;
				m_modifyTime = modifyTime;

				refreshData();
				LOGGER.info("Refreshed resource config, configId={}, modifyTime={}, resourceCount={}.", m_configId,
						m_modifyTime, m_config.getResources().size());
			}
		}
	}

	private void ensureInitialized() {
		if (m_config == null) {
			synchronized (this) {
				if (m_config == null) {
					LOGGER.warn("Resource config is not initialized yet, loading it lazily.");
					initialize();
				}
			}
		}
	}

	private void refreshData() {
		Map<String, Map<String, Integer>> permissions = new ConcurrentHashMap<String, Map<String, Integer>>();

		for (Resource resource : m_config.getResources()) {
			String path = resource.getPath();
			Map<String, Integer> pathPermission = permissions.get(path);

			if (pathPermission == null) {
				pathPermission = new ConcurrentHashMap<String, Integer>();
				permissions.put(path, pathPermission);
			}

			pathPermission.put(resource.getOp(), resource.getRole());
		}

		m_permissions = permissions;
		LOGGER.info("Rebuilt resource permissions cache, pathCount={}.", permissions.size());
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config);

				refreshData();
				LOGGER.info("Stored resource config, configId={}, resourceCount={}.", m_configId,
						m_config.getResources().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store resource config, configId={}.", m_configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
