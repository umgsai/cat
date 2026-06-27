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
package com.dianping.cat.config.transaction;

import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.tp.entity.Domain;
import com.dianping.cat.configuration.tp.entity.TpValueStatisticConfig;
import com.dianping.cat.configuration.tp.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.task.TimerSyncTask;

@Component
public class TpValueStatisticConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(TpValueStatisticConfigManager.class);

	public static final String DEFAULT = "default";

	private static final String CONFIG_NAME = "tp-value-statistic-config";

	@Resource
	protected ConfigRepository configRepository;

	@Resource
	protected ContentFetcher contentFetcher;

	@Resource
	private ServerConfigManager serverConfigManager;

	private long configId;

	private long modifyTime;

	private TpValueStatisticConfig config;

	private volatile boolean initialized;

	public TpValueStatisticConfig getConfig() {
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
			LOGGER.info("Loaded TP value statistic config from repository, configId={}, modifyTime={}.", configId,
					modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("TP value statistic config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				Config config = configRepository.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				configRepository.insert(config);
				configId = config.getId();
				this.config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized TP value statistic config from default content, configId={}.", configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize TP value statistic config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load TP value statistic config from repository.", e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new TpValueStatisticConfig();
			LOGGER.warn("TP value statistic config is empty after initialization, using a new empty config.");
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
			LOGGER.error("Unable to parse TP value statistic config xml for insert. xmlLength={}.",
					xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		this.serverConfigManager = serverConfigManager;
	}

	private void refreshConfig() throws Exception {
		Config config = configRepository.findByName(CONFIG_NAME);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > this.modifyTime) {
				String content = config.getContent();
				TpValueStatisticConfig tmp = DefaultSaxParser.parse(content);

				this.config = tmp;
				this.modifyTime = modifyTime;
				LOGGER.info("Refreshed TP value statistic config, configId={}, modifyTime={}.", configId,
						this.modifyTime);
			}
		}
	}

	private boolean defaultContainsType(String type) {
		ensureInitialized();

		Domain d = config.findDomain("default");
		return d.getTransactionTypes().contains(type);
	}

	private boolean domainContainsType(String type, String domain) {
		ensureInitialized();

		Domain d = config.findDomain(domain);
		return d != null && d.getTransactionTypes().contains(type);
	}

	private boolean matchesPrefix(String type) {
		for (String prefix : serverConfigManager.getForcedStatisticTypePrefixes()) {
			if (type.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	public boolean shouldStatistic(String type, String domain) {
		try {
			return defaultContainsType(type) || matchesPrefix(type) || domainContainsType(type, domain);
		} catch (Exception e) {
			LOGGER.error("Unable to evaluate TP value statistic config, type={}, domain={}, config={}.", type, domain,
					config, e);
			Cat.logError("no default config in tp9xx config: " + config.toString(), e);
			return false;
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
				LOGGER.info("Stored TP value statistic config, configId={}.", configId);
			} catch (Exception e) {
				LOGGER.error("Unable to store TP value statistic config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public Set<String> findTransactionTypesByDomain(String domain) {
		ensureInitialized();

		Domain d = config.findDomain(domain);

		if (d != null) {
			return d.getTransactionTypes();
		} else {
			return new HashSet<String>();
		}
	}

	public Set<String> deleteByDomainType(String domain, String type) {
		Set<String> types = findTransactionTypesByDomain(domain);

		if (types.contains(type)) {
			types.remove(type);
			storeConfig();
			return types;
		}
		return types;
	}

	public void insertOrUpdateByDomain(String domain, Set<String> params) {
		Domain dd = config.findDomain(domain);
		Set<String> st = null;

		if (dd == null) {
			dd = new Domain();
			dd.setId(domain);
		}
		st = dd.getTransactionTypes();
		st.clear();
		st.addAll(params);

		config.addDomain(dd);
		storeConfig();
	}
}
