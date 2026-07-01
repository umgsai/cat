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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;
import com.dianping.cat.configuration.message.transform.DefaultSaxParser;
import com.dianping.cat.mybatis.data.ConfigDO;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.task.TimerSyncTask;

@Component
public class AtomicMessageConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AtomicMessageConfigManager.class);

	private static final String CONFIG_NAME = "atomic-message-config";

	private static final String DEFAULT_DOMAIN = "default";

	@Resource
	protected ConfigRepository configRepository;

	@Resource
	protected ContentFetcher contentFetcher;

	private long configId;

	private long modifyTime;

	private AtomicMessageConfig config;

	private volatile boolean initialized;

	public AtomicMessageConfig getConfig() {
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
			modifyTime = config.getUpdateTime().getTime();
			this.config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded atomic message config from repository, configId={}, modifyTime={}.", configId,
					modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Atomic message config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				ConfigDO config = configRepository.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				configRepository.insert(config);
				configId = config.getId();
				this.config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized atomic message config from default content, configId={}.", configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize atomic message config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load atomic message config from repository.", e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new AtomicMessageConfig();
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
		initialized = true;
	}

	public boolean insert(String xml) {
		ensureInitialized();

		try {
			config = DefaultSaxParser.parse(xml);

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

		Domain d = config.findDomain(domain);

		if (d == null) {
			d = config.findDomain(DEFAULT_DOMAIN);
		}

		if (d != null) {
			return d.getMatchTypes();
		} else {
			return "";
		}
	}

	public String queryAtomicStartTypes(String domain) {
		ensureInitialized();

		Domain d = config.findDomain(domain);

		if (d == null) {
			d = config.findDomain(DEFAULT_DOMAIN);
		}

		if (d != null) {
			return d.getStartTypes();
		} else {
			return "";
		}
	}

	public String queryMaxMetricTagValues(String domain) {
		ensureInitialized();

		Domain d = config.findDomain(domain);

		if (d == null) {
			d = config.findDomain(DEFAULT_DOMAIN);
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
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	public int getPropertyValue(String domain, String propertyName, int defaultValue) {
		ensureInitialized();

		int result = defaultValue;
		Domain d = config.findDomain(domain);

		if (d == null) {
			d = config.findDomain(domain);
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
		ConfigDO config = configRepository.findByName(CONFIG_NAME);
		long modifyTime = config.getUpdateTime().getTime();

		synchronized (this) {
			if (modifyTime > this.modifyTime) {
				String content = config.getContent();
				AtomicMessageConfig messageConfig = DefaultSaxParser.parse(content);

				this.config = messageConfig;
				this.modifyTime = modifyTime;
				LOGGER.info("Refreshed atomic message config, configId={}, modifyTime={}.", configId,
						this.modifyTime);
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
				LOGGER.info("Stored atomic message config, configId={}.", configId);
			} catch (Exception e) {
				LOGGER.error("Unable to store atomic message config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
