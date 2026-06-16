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

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.home.user.entity.User;
import com.dianping.cat.home.user.entity.UserConfig;
import com.dianping.cat.home.user.transform.DefaultSaxParser;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

public class UserConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserConfigManager.class);

	public static final int DEFAULT_ROLE = 1;

	private static final String CONFIG_NAME = "user-config";

	protected ConfigRepository m_configDao;

	protected ContentFetcher m_fetcher;

	private int m_configId;

	private long m_modifyTime;

	private UserConfig m_config;

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public UserConfig getConfig() {
		ensureInitialized();

		return m_config;
	}

	public int getRole(String user) {
		ensureInitialized();

		User usr = m_config.findUser(user);

		if (usr != null) {
			return usr.getRole();
		}

		return DEFAULT_ROLE;
	}

	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME);
			String content = config.getContent();

			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
			m_config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded user config from repository, configId={}, modifyTime={}.", m_configId, m_modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("User config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized user config from default content, configId={}.", m_configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize user config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load user config from repository.", e);
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new UserConfig();
			LOGGER.warn("User config is empty after initialization, using a new empty config.");
		}

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

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				UserConfig userConfig = DefaultSaxParser.parse(content);
				m_config = userConfig;
				m_modifyTime = modifyTime;
				LOGGER.info("Refreshed user config, configId={}, modifyTime={}, userCount={}.", m_configId,
						m_modifyTime, m_config.getUsers().size());
			}
		}
	}

	private void ensureInitialized() {
		if (m_config == null) {
			synchronized (this) {
				if (m_config == null) {
					LOGGER.warn("User config is not initialized yet, loading it lazily.");
					initialize();
				}
			}
		}
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse user config xml for insert. xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
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
				LOGGER.info("Stored user config, configId={}, userCount={}.", m_configId, m_config.getUsers().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store user config, configId={}.", m_configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
