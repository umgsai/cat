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

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.receiver.entity.AlertConfig;
import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.receiver.transform.DefaultSaxParser;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;

public class AlertConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertConfigManager.class);

	private static final String CONFIG_NAME = "alertConfig";

	private ConfigRepository m_configDao;

	private ContentFetcher m_fetcher;

	private long m_configId;

	private AlertConfig m_config;

	private volatile boolean m_initialized;

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
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
		return m_config;
	}

	public void initialize() {
		if (m_initialized) {
			return;
		}
		synchronized (this) {
			if (m_initialized) {
				return;
			}
			try {
				Config config = m_configDao.findByName(CONFIG_NAME);
				String content = config.getContent();

				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
				LOGGER.info("Loaded alert config from repository, configId={}.", m_configId);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Alert config is missing in repository, loading default content from fetcher.", e);

				try {
					String content = m_fetcher.getConfigContent(CONFIG_NAME);
					Config config = m_configDao.createLocal();

					config.setName(CONFIG_NAME);
					config.setContent(content);
					m_configDao.insert(config);

					m_configId = config.getId();
					m_config = DefaultSaxParser.parse(content);
					LOGGER.info("Initialized alert config from default content, configId={}.", m_configId);
				} catch (Exception ex) {
					LOGGER.error("Unable to initialize alert config from default content.", ex);
					Cat.logError(ex);
				}
			} catch (Exception e) {
				LOGGER.error("Unable to load alert config from repository.", e);
				Cat.logError(e);
			}
			if (m_config == null) {
				m_config = new AlertConfig();
				LOGGER.warn("Alert config is empty after initialization, using a new empty config.");
			}
			m_initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!m_initialized) {
			initialize();
		}
	}

	public boolean insert(String xml) {
		ensureInitialized();
		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse alert config xml for insert. xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public Receiver queryReceiverById(String id) {
		ensureInitialized();
		return m_config.getReceivers().get(id);
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
				LOGGER.info("Stored alert config, configId={}, receiverCount={}.", m_configId,
						m_config.getReceivers().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store alert config, configId={}.", m_configId, e);
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
