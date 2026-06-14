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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.policy.entity.AlertPolicy;
import com.dianping.cat.alarm.policy.entity.Group;
import com.dianping.cat.alarm.policy.entity.Level;
import com.dianping.cat.alarm.policy.entity.Type;
import com.dianping.cat.alarm.policy.transform.DefaultSaxParser;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.spring.CatSpringContext;

public class AlertPolicyManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertPolicyManager.class);

	private static final String CONFIG_NAME = "alertPolicy";

	private static final String DEFAULT_TYPE = "default";

	private static final String DEFAULT_GROUP = "default";

	private ConfigRepository m_configDao;

	private ContentFetcher m_fetcher;

	private int m_configId;

	private AlertPolicy m_config;

	private volatile boolean m_initialized;

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public AlertPolicy getAlertPolicy() {
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
			refreshSpringBeans();

			try {
				Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
				String content = config.getContent();

				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
				LOGGER.info("Loaded alert policy from repository, configId={}.", m_configId);
			} catch (DalNotFoundException e) {
				LOGGER.warn("Alert policy is missing in repository, loading default content from fetcher.", e);

				try {
					String content = m_fetcher.getConfigContent(CONFIG_NAME);
					Config config = m_configDao.createLocal();

					config.setName(CONFIG_NAME);
					config.setContent(content);
					m_configDao.insert(config);

					m_configId = config.getId();
					m_config = DefaultSaxParser.parse(content);
					LOGGER.info("Initialized alert policy from default content, configId={}.", m_configId);
				} catch (Exception ex) {
					LOGGER.error("Unable to initialize alert policy from default content.", ex);
					Cat.logError(ex);
				}
			} catch (Exception e) {
				LOGGER.error("Unable to load alert policy from repository.", e);
				Cat.logError(e);
			}
			if (m_config == null) {
				m_config = new AlertPolicy();
				LOGGER.warn("Alert policy is empty after initialization, using a new empty policy.");
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
			LOGGER.error("Unable to parse alert policy xml for insert. xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public List<AlertChannel> queryChannels(String typeName, String groupName, String levelName) {
		ensureInitialized();
		try {
			Level level = queryLevel(typeName, groupName, levelName);
			if (level == null) {
				return new ArrayList<AlertChannel>();
			} else {
				String send = level.getSend();
				String[] sends = send.split(",");
				List<AlertChannel> channels = new ArrayList<AlertChannel>();

				for (String str : sends) {
					AlertChannel channel = AlertChannel.findByName(str);

					if (channel != null) {
						channels.add(channel);
					}
				}

			return channels;
		}
	} catch (Exception ex) {
		LOGGER.warn("Unable to query alert channels, type={}, group={}, level={}; returning empty channels.", typeName,
				groupName, levelName, ex);
		return new ArrayList<AlertChannel>();
	}
}

	private Level queryLevel(String typeName, String groupName, String levelName) {
		ensureInitialized();
		Type type = m_config.findType(typeName);

		if (type == null) {
			type = m_config.findType(DEFAULT_TYPE);
		}

		Group group = type.findGroup(groupName);

		if (group == null) {
			group = type.findGroup(DEFAULT_GROUP);
		}

		return group.findLevel(levelName);
	}

	public int queryRecoverMinute(String typeName, String groupName, String levelName) {
		ensureInitialized();
		try {
			Level level = queryLevel(typeName, groupName, levelName);

			if (level == null) {
				return 1;
			} else {
			return level.getRecoverMinute();
		}
	} catch (Exception ex) {
		LOGGER.warn("Unable to query alert recover minute, type={}, group={}, level={}; returning default value 1.",
				typeName, groupName, levelName, ex);
		return 1;
	}
}

	public int querySuspendMinute(String typeName, String groupName, String levelName) {
		ensureInitialized();
		try {
			Level level = queryLevel(typeName, groupName, levelName);

			if (level == null) {
				return 0;
			} else {
			return level.getSuspendMinute();
		}
	} catch (Exception ex) {
		LOGGER.warn("Unable to query alert suspend minute, type={}, group={}, level={}; returning default value 0.",
				typeName, groupName, levelName, ex);
		return 0;
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
				LOGGER.info("Stored alert policy, configId={}, typeCount={}.", m_configId, m_config.getTypes().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store alert policy, configId={}.", m_configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	private void refreshSpringBeans() {
		ConfigRepository configDao = CatSpringContext.getBeanIfAvailable(ConfigRepository.class);
		ContentFetcher fetcher = CatSpringContext.getBeanIfAvailable(ContentFetcher.class);

		if (configDao != null) {
			m_configDao = configDao;
		}
		if (fetcher != null) {
			m_fetcher = fetcher;
		}
	}

}
