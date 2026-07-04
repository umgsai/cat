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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.mybatis.data.ConfigDO;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.home.user.entity.User;
import com.dianping.cat.home.user.entity.UserConfig;
import com.dianping.cat.home.user.transform.DefaultSaxParser;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Component
public class UserConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserConfigManager.class);

	private static final String ADMIN = "admin";

	public static final int DEFAULT_ROLE = 1;

	private static final String CONFIG_NAME = "user-config";

	@Resource
	protected ConfigRepository configRepository;

	@Resource
	protected ContentFetcher contentFetcher;

	private long configId;

	private long modifyTime;

	private UserConfig config;

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	public UserConfig getConfig() {
		ensureInitialized();

		return config;
	}

	public int getRole(String user) {
		ensureInitialized();

		if (ADMIN.equals(user)) {
			return Integer.MAX_VALUE;
		}

		User usr = config.findUser(user);

		if (usr != null) {
			return usr.getRole();
		}

		return DEFAULT_ROLE;
	}

	@PostConstruct
	public void initialize() {
		try {
			ConfigDO config = configRepository.findByName(CONFIG_NAME);
			String content = config.getContent();

			configId = config.getId();
			modifyTime = config.getUpdateTime().getTime();
			this.config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded user config from repository, configId={}, modifyTime={}.", configId, modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("User config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				ConfigDO config = configRepository.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				configRepository.insert(config);
				configId = config.getId();
				this.config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized user config from default content, configId={}.", configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize user config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load user config from repository.", e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new UserConfig();
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
		ConfigDO config = configRepository.findByName(CONFIG_NAME);
		long modifyTime = config.getUpdateTime().getTime();

		synchronized (this) {
			if (modifyTime > this.modifyTime) {
				String content = config.getContent();
				UserConfig userConfig = DefaultSaxParser.parse(content);
				this.config = userConfig;
				this.modifyTime = modifyTime;
				LOGGER.info("Refreshed user config, configId={}, modifyTime={}, userCount={}.", configId,
						this.modifyTime, this.config.getUsers().size());
			}
		}
	}

	private void ensureInitialized() {
		if (config == null) {
			synchronized (this) {
				if (config == null) {
					LOGGER.warn("User config is not initialized yet, loading it lazily.");
					initialize();
				}
			}
		}
	}

	public boolean insert(String xml) {
		try {
			config = DefaultSaxParser.parse(xml);

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
				ConfigDO config = configRepository.createLocal();

				config.setId(configId);
				config.setName(CONFIG_NAME);
				config.setContent(this.config.toString());
				configRepository.updateByPK(config);
				LOGGER.info("Stored user config, configId={}, userCount={}.", configId, this.config.getUsers().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store user config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
