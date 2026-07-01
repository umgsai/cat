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
package com.dianping.cat.consumer.config;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.consumer.all.config.entity.AllConfig;
import com.dianping.cat.consumer.all.config.entity.Name;
import com.dianping.cat.consumer.all.config.entity.Report;
import com.dianping.cat.consumer.all.config.entity.Type;
import com.dianping.cat.consumer.all.config.transform.DefaultSaxParser;
import com.dianping.cat.mybatis.data.ConfigDO;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Component
public class AllReportConfigManager {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AllReportConfigManager.class);

	private static final String CONFIG_NAME = "all-report-config";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	private long configId;

	private volatile AllConfig config;

	private long modifyTime;

	private volatile boolean initialized;

	public AllConfig getConfig() {
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
			ConfigDO config = configRepository.findByName(CONFIG_NAME);
			String content = config.getContent();

			configId = config.getId();
			this.config = DefaultSaxParser.parse(content);
			modifyTime = config.getUpdateTime().getTime();
			LOGGER.info("Loaded all report config from repository, configId={}, modifyTime={}.", configId, modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("All report config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				ConfigDO config = configRepository.createLocal();
				Date now = new Date();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				config.setUpdateTime(now);
				configRepository.insert(config);

				configId = config.getId();
				this.config = DefaultSaxParser.parse(content);
				modifyTime = now.getTime();
				LOGGER.info("Initialized all report config from default content, configId={}.", configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize all report config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load all report config from repository.", e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new AllConfig();
			LOGGER.warn("All report config is empty after initialization, using a new empty config.");
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

	public boolean insert(String xml) {
		ensureInitialized();

		try {
			config = DefaultSaxParser.parse(xml);
			boolean result = storeConfig();

			return result;
		} catch (Exception e) {
			Cat.logError(e);
			LOGGER.error("Unable to insert all report config.", e);
			return false;
		}
	}

	private void refreshConfig() throws SAXException, IOException {
		ConfigDO config = configRepository.findByName(CONFIG_NAME);
		long modifyTime = config.getUpdateTime().getTime();

		synchronized (this) {
			if (modifyTime > this.modifyTime) {
				String content = config.getContent();
				AllConfig allConfig = DefaultSaxParser.parse(content);

				this.config = allConfig;
				this.modifyTime = modifyTime;
				LOGGER.info("Refreshed all report config, configId={}, modifyTime={}.", configId, this.modifyTime);
			}
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
				LOGGER.info("Stored all report config, configId={}.", configId);
			} catch (Exception e) {
				LOGGER.error("Unable to store all report config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public boolean validate(String reportName, String type) {
		ensureInitialized();

		Report report = config.getReports().get(reportName);

		if (report != null) {
			Map<String, Type> types = report.getTypes();

			return types.containsKey(type) || types.containsKey("*");
		} else {
			return false;
		}
	}

	public boolean validate(String reportName, String type, String name) {
		ensureInitialized();

		Report report = config.getReports().get(reportName);

		if (report != null) {
			Map<String, Type> types = report.getTypes();
			Type typeConfig = types.get(type);

			if (typeConfig != null) {
				List<Name> list = typeConfig.getNameList();

				for (Name nameConfig : list) {
					String configId = nameConfig.getId();

					if (configId.equals(name) || "*".equals(configId)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

}
