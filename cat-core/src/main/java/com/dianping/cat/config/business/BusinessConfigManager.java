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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.transform.DefaultSaxParser;
import com.dianping.cat.mybatis.data.BusinessConfigDO;
import com.dianping.cat.mybatis.mapper.BusinessConfigRepository;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Component
public class BusinessConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessConfigManager.class);

	public final static String BASE_CONFIG = "base";

	@Resource
	private BusinessConfigRepository businessConfigRepository;

	@Resource
	private ServerConfigManager serverConfigManager;

	private Map<String, Set<String>> domains = new ConcurrentHashMap<String, Set<String>>();

	private Map<String, BusinessReportConfig> configs = new ConcurrentHashMap<String, BusinessReportConfig>();

	private boolean alertMachine;

	private volatile boolean initialized;

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
			BusinessConfigDO config = businessConfigRepository.findByNameDomain(BASE_CONFIG, domain);
			BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());

			businessReportConfig.removeBusinessItemConfig(key);
			config.setContent(businessReportConfig.toString());
			config.setUpdateTime(new Date());
			businessConfigRepository.updateByPK(config);

			Set<String> itemIds = domains.get(domain);

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
			BusinessConfigDO config = businessConfigRepository.findByNameDomain(BASE_CONFIG, domain);
			BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());

			businessReportConfig.removeCustomConfig(key);
			config.setContent(businessReportConfig.toString());
			config.setUpdateTime(new Date());

			businessConfigRepository.updateByPK(config);
			cacheConfigs(businessReportConfig, domain);
		} catch (Exception e) {
			LOGGER.error("Unable to delete business custom config, domain={}, key={}.", domain, key, e);
			Cat.logError(e);
			return false;
		}

		return true;
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

		if (serverConfigManager == null) {
			throw new IllegalStateException("ServerConfigManager is required for BusinessConfigManager.");
		}
		if (businessConfigRepository == null) {
			throw new IllegalStateException("BusinessConfigRepository is required for BusinessConfigManager.");
		}

		alertMachine = serverConfigManager.isAlertMachine();
		LOGGER.info("Initializing business config manager, alertMachine={}.", alertMachine);

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
		initialized = true;
	}

	public void setConfigDao(BusinessConfigRepository configDao) {
		businessConfigRepository = configDao;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		this.serverConfigManager = serverConfigManager;
	}

	private void loadData() {
		try {
			List<BusinessConfigDO> configs = businessConfigRepository.findByName(BASE_CONFIG);
			Map<String, Set<String>> domains = new ConcurrentHashMap<String, Set<String>>();

			for (BusinessConfigDO config : configs) {
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

			this.domains = domains;
			LOGGER.info("Loaded business configs, configCount={}, domainCount={}.", configs.size(),
					this.domains.size());
		} catch (Exception e) {
			LOGGER.error("Unable to load business configs.", e);
			Cat.logError(e);
		}
	}

	private void cacheConfigs(BusinessReportConfig businessReportConfig, String domain) {
		if (alertMachine) {
			configs.put(domain, businessReportConfig);
		}
	}

	public boolean insertBusinessConfigIfNotExist(String domain, String key, ConfigItem item) {
		ensureInitialized();

		try {
			if (!domains.containsKey(domain)) {
				BusinessReportConfig config = new BusinessReportConfig();
				config.setId(domain);

				BusinessItemConfig businessItemConfig = buildBusinessItemConfig(key, item);
				config.addBusinessItemConfig(businessItemConfig);

				BusinessConfigDO businessConfig = businessConfigRepository.createLocal();
				businessConfig.setName(BASE_CONFIG);
				businessConfig.setDomain(domain);
				businessConfig.setContent(config.toString());
				businessConfig.setUpdateTime(new Date());
				businessConfigRepository.insert(businessConfig);

				Set<String> itemIds = new HashSet<String>();
				itemIds.add(key);
				domains.put(domain, itemIds);
				cacheConfigs(config, domain);
				LOGGER.info("Inserted new business config, domain={}, key={}.", domain, key);
			} else {
				Set<String> itemIds = domains.get(domain);

				if (!itemIds.contains(key)) {
					BusinessConfigDO businessConfig = businessConfigRepository
											.findByNameDomain(BASE_CONFIG, domain);
					BusinessReportConfig config = DefaultSaxParser.parse(businessConfig.getContent());
					BusinessItemConfig businessItemConfig = buildBusinessItemConfig(key, item);

					config.addBusinessItemConfig(businessItemConfig);
					businessConfig.setContent(config.toString());
					businessConfigRepository.updateByPK(businessConfig);

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
			if (alertMachine) {
				businessReportConfig = configs.get(domain);
			} else {
				BusinessConfigDO config = businessConfigRepository.findByNameDomain(BASE_CONFIG, domain);

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

		BusinessConfigDO proto = businessConfigRepository.createLocal();
		String domain = config.getId();

		proto.setDomain(domain);
		proto.setName(BASE_CONFIG);
		proto.setContent(config.toString());

		try {
			businessConfigRepository.updateBaseConfigByDomain(proto);
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

		BusinessConfigDO proto = businessConfigRepository.createLocal();
		String domain = config.getId();

		proto.setDomain(domain);
		proto.setName(BASE_CONFIG);
		proto.setContent(config.toString());
		proto.setUpdateTime(new Date());

		try {
			businessConfigRepository.insert(proto);
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
