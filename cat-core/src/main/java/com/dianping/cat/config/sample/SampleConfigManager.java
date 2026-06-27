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
package com.dianping.cat.config.sample;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.sample.entity.SampleConfig;
import com.dianping.cat.sample.transform.DefaultSaxParser;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Component
public class SampleConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleConfigManager.class);

	private static final String CONFIG_NAME = "sampleConfig";

	@Resource
	protected ConfigRepository configRepository;

	@Resource
	protected ContentFetcher contentFetcher;

	private long configId;

	private long modifyTime;

	private SampleConfig config;

	private volatile boolean initialized;

	public SampleConfig getConfig() {
		ensureInitialized();
		return config;
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

		try {
			Config config = configRepository.findByName(CONFIG_NAME);
			String content = config.getContent();

			configId = config.getId();
			modifyTime = config.getModifyDate().getTime();
			this.config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded sample config from repository, configId={}, modifyTime={}.", configId,
					modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Sample config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				Config config = configRepository.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				configRepository.insert(config);
				configId = config.getId();
				this.config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized sample config from default content, configId={}.", configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize sample config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load sample config from repository.", e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new SampleConfig();
			LOGGER.warn("Sample config is empty after initialization, using a new empty config.");
		}

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}

			@Override
			public String getName() {
				return CONFIG_NAME;
			}
		});
		initialized = true;
	}

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	public boolean insert(String xml) {
		ensureInitialized();

		try {
			config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse sample config xml for insert. xmlLength={}.", xml == null ? 0 : xml.length(),
					e);
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

				this.config = DefaultSaxParser.parse(content);
				this.modifyTime = modifyTime;
				LOGGER.info("Refreshed sample config, configId={}, modifyTime={}.", configId, this.modifyTime);
			}
		}
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
				LOGGER.info("Stored sample config, configId={}.", configId);
			} catch (Exception e) {
				LOGGER.error("Unable to store sample config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
