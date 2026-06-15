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
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.exception.entity.ExceptionRuleConfig;
import com.dianping.cat.home.exception.transform.DefaultSaxParser;

public class ExceptionRuleConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionRuleConfigManager.class);

	private static final String CONFIG_NAME = "exceptionRuleConfig";

	public static String DEFAULT_STRING = "Default";

	public static String TOTAL_STRING = "Total";

	private ConfigRepository m_configDao;

	private ContentFetcher m_fetcher;

	private int m_configId;

	private ExceptionRuleConfig m_exceptionRuleConfig;

	private volatile boolean m_initialized;

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public boolean deleteExceptionExclude(String domain, String exceptionName) {
		m_exceptionRuleConfig.removeExceptionExclude(domain + ":" + exceptionName);

		return storeConfig();
	}

	public boolean deleteExceptionLimit(String domain, String exceptionName) {
		m_exceptionRuleConfig.removeExceptionLimit(domain + ":" + exceptionName);

		return storeConfig();
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
				LOGGER.info("Initializing exception rule config manager, configName={}.", CONFIG_NAME);
				Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
				String content = config.getContent();
				m_configId = config.getId();
				m_exceptionRuleConfig = DefaultSaxParser.parse(content);
			} catch (DalNotFoundException e) {
				LOGGER.warn("Exception rule config not found in repository, loading default content, configName={}.",
				      CONFIG_NAME);
				try {
					String content = m_fetcher.getConfigContent(CONFIG_NAME);
					Config config = m_configDao.createLocal();

					config.setName(CONFIG_NAME);
					config.setContent(content);
					m_configDao.insert(config);

					m_configId = config.getId();
					m_exceptionRuleConfig = DefaultSaxParser.parse(content);
				} catch (Exception ex) {
					LOGGER.error("Unable to create default exception rule config, configName={}.", CONFIG_NAME, ex);
					Cat.logError(ex);
				}
			} catch (Exception e) {
				LOGGER.error("Unable to initialize exception rule config, configName={}.", CONFIG_NAME, e);
				Cat.logError(e);
			}
			if (m_exceptionRuleConfig == null) {
				LOGGER.warn("Exception rule config is empty after initialization, using an empty config.");
				m_exceptionRuleConfig = new ExceptionRuleConfig();
			}
			m_initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!m_initialized) {
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

		m_exceptionRuleConfig.getExceptionExcludes().put(id, exclude);
		return storeConfig();
	}

	public boolean insertExceptionLimit(ExceptionLimit limit) {
		ensureInitialized();
		String id = limit.getDomain() + ":" + limit.getName();

		m_exceptionRuleConfig.getExceptionLimits().put(id, limit);
		return storeConfig();
	}

	public List<ExceptionExclude> queryAllExceptionExcludes() {
		ensureInitialized();
		return new ArrayList<ExceptionExclude>(m_exceptionRuleConfig.getExceptionExcludes().values());
	}

	public List<ExceptionLimit> queryAllExceptionLimits() {
		ensureInitialized();
		return new ArrayList<ExceptionLimit>(m_exceptionRuleConfig.getExceptionLimits().values());
	}

	public ExceptionExclude queryExceptionExclude(String domain, String exceptionName) {
		ensureInitialized();
		ExceptionExclude exceptionExclude = m_exceptionRuleConfig.findExceptionExclude(domain + ":" + exceptionName);

		if (exceptionExclude == null) {
			exceptionExclude = m_exceptionRuleConfig.findExceptionExclude(DEFAULT_STRING + ":" + exceptionName);
		}
		return exceptionExclude;
	}

	public ExceptionLimit queryExceptionLimit(String domain, String exceptionName) {
		ensureInitialized();
		ExceptionLimit exceptionLimit = m_exceptionRuleConfig.findExceptionLimit(domain + ":" + exceptionName);

		if (exceptionLimit == null) {
			exceptionLimit = m_exceptionRuleConfig.findExceptionLimit(DEFAULT_STRING + ":" + exceptionName);
		}
		return exceptionLimit;
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_exceptionRuleConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				LOGGER.error("Unable to store exception rule config, configName={}, configId={}.", CONFIG_NAME,
				      m_configId, e);
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
