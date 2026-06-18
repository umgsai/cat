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
package com.dianping.cat.config.business;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.transform.DefaultSaxParser;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.mybatis.mapper.BusinessConfigRepository;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

public class BusinessConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessConfigManager.class);

	public final static String BASE_CONFIG = "base";

	private BusinessConfigRepository m_configDao;

	private ServerConfigManager m_serverConfigManager;

	private Map<String, Set<String>> m_domains = new ConcurrentHashMap<String, Set<String>>();

	private Map<String, BusinessReportConfig> m_configs = new ConcurrentHashMap<String, BusinessReportConfig>();

	private boolean m_alertMachine;

	private volatile boolean m_initialized;

	private BusinessItemConfig buildBusinessItemConfig(String key, ConfigItem item) {
		BusinessItemConfig config = new BusinessItemConfig();

		config.setId(key);
		config.setTitle(item.getTitle());
		config.setShowAvg(item.isShowAvg());
		config.setShowCount(item.isShowCount());
		config.setShowSum(item.isShowSum());
		config.setViewOrder(item.getViewOrder());
		return config;
	}

	public boolean deleteBusinessItem(String domain, String key) {
		ensureInitialized();

		try {
			BusinessConfig config = m_configDao.findByNameDomain(BASE_CONFIG, domain);
			BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());

			businessReportConfig.removeBusinessItemConfig(key);
			config.setContent(businessReportConfig.toString());
			config.setUpdatetime(new Date());
			m_configDao.updateByPK(config);

			Set<String> itemIds = m_domains.get(domain);

			itemIds.remove(key);
			cacheConfigs(businessReportConfig, domain);
		} catch (Exception e) {
			LOGGER.error("Unable to delete business item config, domain={}, key={}.", domain, key, e);
			Cat.logError(e);
			return false;
		}
		return true;
	}

	public boolean deleteCustomItem(String domain, String key) {
		ensureInitialized();

		try {
			BusinessConfig config = m_configDao.findByNameDomain(BASE_CONFIG, domain);
			BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());

			businessReportConfig.removeCustomConfig(key);
			config.setContent(businessReportConfig.toString());
			config.setUpdatetime(new Date());

			m_configDao.updateByPK(config);
			cacheConfigs(businessReportConfig, domain);
		} catch (Exception e) {
			LOGGER.error("Unable to delete business custom config, domain={}, key={}.", domain, key, e);
			Cat.logError(e);
			return false;
		}

		return true;
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

		if (m_serverConfigManager == null) {
			throw new IllegalStateException("ServerConfigManager is required for BusinessConfigManager.");
		}
		if (m_configDao == null) {
			throw new IllegalStateException("BusinessConfigRepository is required for BusinessConfigManager.");
		}

		m_alertMachine = m_serverConfigManager.isAlertMachine();
		LOGGER.info("Initializing business config manager, alertMachine={}.", m_alertMachine);

		loadData();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public void handle() throws Exception {
				loadData();
			}

			@Override
			public String getName() {
				return BASE_CONFIG;
			}
		});
		m_initialized = true;
	}

	public void setConfigDao(BusinessConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		m_serverConfigManager = serverConfigManager;
	}

	private void loadData() {
		try {
			List<BusinessConfig> configs = m_configDao.findByName(BASE_CONFIG);
			Map<String, Set<String>> domains = new ConcurrentHashMap<String, Set<String>>();

			for (BusinessConfig config : configs) {
				try {
					BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());
					String domain = businessReportConfig.getId();
					Set<String> itemIds = new HashSet<String>(businessReportConfig.getBusinessItemConfigs().keySet());

					domains.put(domain, itemIds);
					cacheConfigs(businessReportConfig, domain);
				} catch (Exception e) {
					LOGGER.error("Unable to parse business config, configId={}, domain={}.", config.getId(),
							config.getDomain(), e);
					Cat.logError(e);
				}
			}

			m_domains = domains;
			LOGGER.info("Loaded business configs, configCount={}, domainCount={}.", configs.size(), m_domains.size());
		} catch (Exception e) {
			LOGGER.error("Unable to load business configs.", e);
			Cat.logError(e);
		}
	}

	private void cacheConfigs(BusinessReportConfig businessReportConfig, String domain) {
		if (m_alertMachine) {
			m_configs.put(domain, businessReportConfig);
		}
	}

	public boolean insertBusinessConfigIfNotExist(String domain, String key, ConfigItem item) {
		ensureInitialized();

		try {
			if (!m_domains.containsKey(domain)) {
				BusinessReportConfig config = new BusinessReportConfig();
				config.setId(domain);

				BusinessItemConfig businessItemConfig = buildBusinessItemConfig(key, item);
				config.addBusinessItemConfig(businessItemConfig);

				BusinessConfig businessConfig = m_configDao.createLocal();
				businessConfig.setName(BASE_CONFIG);
				businessConfig.setDomain(domain);
				businessConfig.setContent(config.toString());
				businessConfig.setUpdatetime(new Date());
				m_configDao.insert(businessConfig);

				Set<String> itemIds = new HashSet<String>();
				itemIds.add(key);
				m_domains.put(domain, itemIds);
				cacheConfigs(config, domain);
				LOGGER.info("Inserted new business config, domain={}, key={}.", domain, key);
			} else {
				Set<String> itemIds = m_domains.get(domain);

				if (!itemIds.contains(key)) {
					BusinessConfig businessConfig = m_configDao
											.findByNameDomain(BASE_CONFIG, domain);
					BusinessReportConfig config = DefaultSaxParser.parse(businessConfig.getContent());
					BusinessItemConfig businessItemConfig = buildBusinessItemConfig(key, item);

					config.addBusinessItemConfig(businessItemConfig);
					businessConfig.setContent(config.toString());
					m_configDao.updateByPK(businessConfig);

					itemIds.add(key);
					cacheConfigs(config, domain);
					LOGGER.info("Inserted business item config, domain={}, key={}.", domain, key);
				}
			}

			return true;
		} catch (Exception e) {
			LOGGER.error("Unable to insert business config if not exists, domain={}, key={}.", domain, key, e);
			Cat.logError(e);
		}
		return false;
	}

	public BusinessReportConfig queryConfigByDomain(String domain) {
		ensureInitialized();

		BusinessReportConfig businessReportConfig = null;

		try {
			if (m_alertMachine) {
				businessReportConfig = m_configs.get(domain);
			} else {
				BusinessConfig config = m_configDao.findByNameDomain(BASE_CONFIG, domain);

				businessReportConfig = DefaultSaxParser.parse(config.getContent());
			}
		} catch (EmptyResultDataAccessException notFound) {
			LOGGER.warn("Business config is missing, domain={}; returning empty config.", domain, notFound);
		} catch (Exception e) {
			LOGGER.error("Unable to query business config by domain={}.", domain, e);
			Cat.logError(e);
		}

		if (businessReportConfig == null) {
			businessReportConfig = new BusinessReportConfig();
		}
		return businessReportConfig;
	}

	public boolean updateConfigByDomain(BusinessReportConfig config) {
		ensureInitialized();

		BusinessConfig proto = m_configDao.createLocal();
		String domain = config.getId();

		proto.setDomain(domain);
		proto.setName(BASE_CONFIG);
		proto.setContent(config.toString());

		try {
			m_configDao.updateBaseConfigByDomain(proto);
			cacheConfigs(config, domain);
			LOGGER.info("Updated business config, domain={}.", domain);
			return true;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to update business config, domain={}.", domain, e);
			Cat.logError(e);
		}

		return false;
	}

	public boolean insertConfigByDomain(BusinessReportConfig config) {
		ensureInitialized();

		BusinessConfig proto = m_configDao.createLocal();
		String domain = config.getId();

		proto.setDomain(domain);
		proto.setName(BASE_CONFIG);
		proto.setContent(config.toString());
		proto.setUpdatetime(new Date());

		try {
			m_configDao.insert(proto);
			cacheConfigs(config, domain);
			LOGGER.info("Inserted business config, domain={}.", domain);
			return true;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to insert business config, domain={}.", domain, e);
			Cat.logError(e);
		}

		return false;
	}
}
