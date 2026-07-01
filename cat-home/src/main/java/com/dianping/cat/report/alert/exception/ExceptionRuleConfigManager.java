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
package com.dianping.cat.report.alert.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.mybatis.data.ConfigDO;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.exception.entity.ExceptionRuleConfig;
import com.dianping.cat.home.exception.transform.DefaultSaxParser;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class ExceptionRuleConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionRuleConfigManager.class);

	private static final String CONFIG_NAME = "exceptionRuleConfig";

	public static String DEFAULT_STRING = "Default";

	public static String TOTAL_STRING = "Total";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	private long configId;

	private ExceptionRuleConfig exceptionRuleConfig;

	private volatile boolean initialized;

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	public boolean deleteExceptionExclude(String domain, String exceptionName) {
		exceptionRuleConfig.removeExceptionExclude(domain + ":" + exceptionName);

		return storeConfig();
	}

	public boolean deleteExceptionLimit(String domain, String exceptionName) {
		exceptionRuleConfig.removeExceptionLimit(domain + ":" + exceptionName);

		return storeConfig();
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
				LOGGER.info("Initializing exception rule config manager, configName={}.", CONFIG_NAME);
				ConfigDO config = configRepository.findByName(CONFIG_NAME);
				String content = config.getContent();
				configId = config.getId();
				exceptionRuleConfig = DefaultSaxParser.parse(content);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Exception rule config not found in repository, loading default content, configName={}.",
				      CONFIG_NAME);
				try {
					String content = contentFetcher.getConfigContent(CONFIG_NAME);
					ConfigDO config = configRepository.createLocal();

					config.setName(CONFIG_NAME);
					config.setContent(content);
					configRepository.insert(config);

					configId = config.getId();
					exceptionRuleConfig = DefaultSaxParser.parse(content);
				} catch (Exception ex) {
					LOGGER.error("Unable to create default exception rule config, configName={}.", CONFIG_NAME, ex);
					Cat.logError(ex);
				}
			} catch (Exception e) {
				LOGGER.error("Unable to initialize exception rule config, configName={}.", CONFIG_NAME, e);
				Cat.logError(e);
			}
			if (exceptionRuleConfig == null) {
				LOGGER.warn("Exception rule config is empty after initialization, using an empty config.");
				exceptionRuleConfig = new ExceptionRuleConfig();
			}
			initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	public boolean isExcluded(String domain, String exceptionName) {
		ensureInitialized();
		boolean excluded = false;
		ExceptionExclude result = queryExceptionExclude(domain, exceptionName);

		if (result != null) {
			excluded = true;
		}
		return excluded;
	}

	public boolean insertExceptionExclude(ExceptionExclude exclude) {
		ensureInitialized();
		String id = exclude.getDomain() + ":" + exclude.getName();

		exceptionRuleConfig.getExceptionExcludes().put(id, exclude);
		return storeConfig();
	}

	public boolean insertExceptionLimit(ExceptionLimit limit) {
		ensureInitialized();
		String id = limit.getDomain() + ":" + limit.getName();

		exceptionRuleConfig.getExceptionLimits().put(id, limit);
		return storeConfig();
	}

	public List<ExceptionExclude> queryAllExceptionExcludes() {
		ensureInitialized();
		return new ArrayList<ExceptionExclude>(exceptionRuleConfig.getExceptionExcludes().values());
	}

	public List<ExceptionLimit> queryAllExceptionLimits() {
		ensureInitialized();
		return new ArrayList<ExceptionLimit>(exceptionRuleConfig.getExceptionLimits().values());
	}

	public ExceptionExclude queryExceptionExclude(String domain, String exceptionName) {
		ensureInitialized();
		ExceptionExclude exceptionExclude = exceptionRuleConfig.findExceptionExclude(domain + ":" + exceptionName);

		if (exceptionExclude == null) {
			exceptionExclude = exceptionRuleConfig.findExceptionExclude(DEFAULT_STRING + ":" + exceptionName);
		}
		return exceptionExclude;
	}

	public ExceptionLimit queryExceptionLimit(String domain, String exceptionName) {
		ensureInitialized();
		ExceptionLimit exceptionLimit = exceptionRuleConfig.findExceptionLimit(domain + ":" + exceptionName);

		if (exceptionLimit == null) {
			exceptionLimit = exceptionRuleConfig.findExceptionLimit(DEFAULT_STRING + ":" + exceptionName);
		}
		return exceptionLimit;
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				ConfigDO config = configRepository.createLocal();

				config.setId(configId);
				config.setName(CONFIG_NAME);
				config.setContent(exceptionRuleConfig.toString());
				configRepository.updateByPK(config);
			} catch (Exception e) {
				LOGGER.error("Unable to store exception rule config, configName={}, configId={}.", CONFIG_NAME,
				      configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public ExceptionLimit queryTotalLimitByDomain(String domain) {
		return queryExceptionLimit(domain, TOTAL_STRING);
	}

}
