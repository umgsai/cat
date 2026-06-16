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
package com.dianping.cat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.cat.core.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;
import com.dianping.cat.configuration.message.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;

public class AtomicMessageConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AtomicMessageConfigManager.class);

	private static final String CONFIG_NAME = "atomic-message-config";

	private static final String DEFAULT_DOMAIN = "default";

	protected ConfigRepository m_configDao;

	protected ContentFetcher m_fetcher;

	private int m_configId;

	private long m_modifyTime;

	private AtomicMessageConfig m_config;

	private volatile boolean m_initialized;

	public AtomicMessageConfig getConfig() {
		ensureInitialized();
		return m_config;
	}

	private void ensureInitialized() {
		if (!m_initialized) {
			initialize();
		}
	}

	public synchronized void initialize() {
		if (m_initialized) {
			return;
		}

		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
			m_config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded atomic message config from repository, configId={}, modifyTime={}.", m_configId,
					m_modifyTime);
		} catch (DalNotFoundException e) {
			LOGGER.warn("Atomic message config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized atomic message config from default content, configId={}.", m_configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize atomic message config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load atomic message config from repository.", e);
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new AtomicMessageConfig();
			LOGGER.warn("Atomic message config is empty after initialization, using a new empty config.");
		}

		TimerSyncTask.getInstance().register(new TimerSyncTask.SyncHandler() {

			@Override
			public String getName() {
				return CONFIG_NAME;
			}

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}
		});
		m_initialized = true;
	}

	public boolean insert(String xml) {
		ensureInitialized();

		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse atomic message config xml for insert. xmlLength={}.",
					xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public String queryAtomicMatchTypes(String domain) {
		ensureInitialized();

		Domain d = m_config.findDomain(domain);

		if (d == null) {
			d = m_config.findDomain(DEFAULT_DOMAIN);
		}

		if (d != null) {
			return d.getMatchTypes();
		} else {
			return "";
		}
	}

	public String queryAtomicStartTypes(String domain) {
		ensureInitialized();

		Domain d = m_config.findDomain(domain);

		if (d == null) {
			d = m_config.findDomain(DEFAULT_DOMAIN);
		}

		if (d != null) {
			return d.getStartTypes();
		} else {
			return "";
		}
	}

	public String queryMaxMetricTagValues(String domain) {
		ensureInitialized();

		Domain d = m_config.findDomain(domain);

		if (d == null) {
			d = m_config.findDomain(DEFAULT_DOMAIN);
		}

		if (d != null) {
			Property property = d.findProperty("max-metric-tagvalues");

			if (property != null) {
				return property.getValue();
			}
		}

		return "10000";
	}

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public int getPropertyValue(String domain, String propertyName, int defaultValue) {
		ensureInitialized();

		int result = defaultValue;
		Domain d = m_config.findDomain(domain);

		if (d == null) {
			d = m_config.findDomain(domain);
		}

		if (d != null) {
			Property property = d.findProperty(propertyName);

			if (property != null) {
				try {
					result = Integer.parseInt(property.getValue());
				} catch (Exception e) {
					LOGGER.warn("Unable to parse atomic message property, domain={}, propertyName={}, value={}; "
							+ "using defaultValue={}.", domain, propertyName, property.getValue(), defaultValue, e);
				}
			}
		}
		return result;
	}

	public int getMaxApiCountThreshold(String domain) {
		return getPropertyValue(domain, "max-api-count-threshold", 100);
	}

	public int getMaxNameThreshold(String domain) {
		return getPropertyValue(domain, "max-name-threshold", 200);
	}

	public int getMaxBusinessItemCount(String domain) {
		return getPropertyValue(domain, "max-business-item-count", 200);
	}

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				AtomicMessageConfig messageConfig = DefaultSaxParser.parse(content);

				m_config = messageConfig;
				m_modifyTime = modifyTime;
				LOGGER.info("Refreshed atomic message config, configId={}, modifyTime={}.", m_configId, m_modifyTime);
			}
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
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
				LOGGER.info("Stored atomic message config, configId={}.", m_configId);
			} catch (Exception e) {
				LOGGER.error("Unable to store atomic message config, configId={}.", m_configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
