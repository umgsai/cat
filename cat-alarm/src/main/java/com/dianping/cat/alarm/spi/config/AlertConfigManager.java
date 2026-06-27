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
package com.dianping.cat.alarm.spi.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.receiver.entity.AlertConfig;
import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.receiver.transform.DefaultSaxParser;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class AlertConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertConfigManager.class);

	private static final String CONFIG_NAME = "alertConfig";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	private long configId;

	private AlertConfig alertConfig;

	private volatile boolean initialized;

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	public String buildReceiverContentByOnOff(String originXml, String allOnOrOff) {
		try {
			if (StringUtils.isEmpty(allOnOrOff)) {
				return originXml;
			}

			AlertConfig tmpConfig = DefaultSaxParser.parse(originXml);

			if (allOnOrOff.equals("on")) {
				turnOnOrOffConfig(tmpConfig, true);
			} else if (allOnOrOff.equals("off")) {
				turnOnOrOffConfig(tmpConfig, false);
			}

			return tmpConfig.toString();
		} catch (Exception e) {
			LOGGER.error("Unable to build alert receiver config by onOff={}.", allOnOrOff, e);
			Cat.logError(e);
			return null;
		}
	}

	public AlertConfig getAlertConfig() {
		ensureInitialized();
		return alertConfig;
	}

	@PostConstruct
	public void initialize() {
		if (initialized) {
			return;
		}
		synchronized (this) {
			if (initialized) {
				return;
			}
			try {
				Config config = configRepository.findByName(CONFIG_NAME);
				String content = config.getContent();

				configId = config.getId();
				alertConfig = DefaultSaxParser.parse(content);
				LOGGER.info("Loaded alert config from repository, configId={}.", configId);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Alert config is missing in repository, loading default content from fetcher.", e);

				try {
					String content = contentFetcher.getConfigContent(CONFIG_NAME);
					Config config = configRepository.createLocal();

					config.setName(CONFIG_NAME);
					config.setContent(content);
					configRepository.insert(config);

					configId = config.getId();
					alertConfig = DefaultSaxParser.parse(content);
					LOGGER.info("Initialized alert config from default content, configId={}.", configId);
				} catch (Exception ex) {
					LOGGER.error("Unable to initialize alert config from default content.", ex);
					Cat.logError(ex);
				}
			} catch (Exception e) {
				LOGGER.error("Unable to load alert config from repository.", e);
				Cat.logError(e);
			}
			if (alertConfig == null) {
				alertConfig = new AlertConfig();
				LOGGER.warn("Alert config is empty after initialization, using a new empty config.");
			}
			initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	public boolean insert(String xml) {
		ensureInitialized();
		try {
			alertConfig = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse alert config xml for insert. xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public Receiver queryReceiverById(String id) {
		ensureInitialized();
		return alertConfig.getReceivers().get(id);
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = configRepository.createLocal();

				config.setId(configId);
				config.setKeyId(configId);
				config.setName(CONFIG_NAME);
				config.setContent(alertConfig.toString());
				configRepository.updateByPK(config);
				LOGGER.info("Stored alert config, configId={}, receiverCount={}.", configId,
						alertConfig.getReceivers().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store alert config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	private void turnOnOrOffConfig(AlertConfig config, boolean isOn) {
		for (Receiver receiver : config.getReceivers().values()) {
			receiver.setEnable(isOn);
		}
	}

}
