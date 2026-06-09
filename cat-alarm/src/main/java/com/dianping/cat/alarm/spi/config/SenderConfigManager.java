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

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.sender.entity.Par;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.sender.entity.SenderConfig;
import com.dianping.cat.alarm.sender.transform.DefaultSaxParser;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.spring.CatSpringContext;

@Named
public class SenderConfigManager implements Initializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SenderConfigManager.class);

	private static final String CONFIG_NAME = "senderConfig";

	@Inject
	private ConfigRepository m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private SenderConfig m_senderConfig;

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public SenderConfig getConfig() {
		return m_senderConfig;
	}

	@Override
	public void initialize() throws InitializationException {
		refreshSpringBeans();

		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_senderConfig = DefaultSaxParser.parse(content);
			m_configId = config.getId();
			LOGGER.info("Loaded sender config from repository, configId={}.", m_configId);
		} catch (DalNotFoundException e) {
			LOGGER.warn("Sender config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_senderConfig = DefaultSaxParser.parse(content);
				m_configId = config.getId();
				LOGGER.info("Initialized sender config from default content, configId={}.", m_configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize sender config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load sender config from repository.", e);
			Cat.logError(e);
		}
		if (m_senderConfig == null) {
			m_senderConfig = new SenderConfig();
			LOGGER.warn("Sender config is empty after initialization, using a new empty config.");
		}
	}

	public boolean insert(Sender sender) {
		m_senderConfig.getSenders().put(sender.getId(), sender);

		return storeConfig();
	}

	public boolean insert(String xml) {
		try {
			m_senderConfig = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse sender config xml for insert. xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	private final String join(String[] array, String separator) {
		StringBuilder sb = new StringBuilder(1024);
		boolean first = true;

		for (String item : array) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}

			sb.append(item);
		}

		return sb.toString();
	}

	public String queryParString(Sender sender) {
		List<Par> pars = sender.getPars();
		String[] s = new String[pars.size()];
		int i = 0;

		for (Par par : pars) {
			s[i++] = par.getId();
		}
		return join(s, "&");
	}

	public Sender querySender(String id) {
		return m_senderConfig.getSenders().get(id);
	}

	public boolean remove(String id) {
		m_senderConfig.removeSender(id);

		return storeConfig();
	}

	private boolean storeConfig() {
		synchronized (this) {
			refreshSpringBeans();

			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_senderConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
				LOGGER.info("Stored sender config, configId={}, senderCount={}.", m_configId,
						m_senderConfig.getSenders().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store sender config, configId={}.", m_configId, e);
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
			LOGGER.info("SenderConfigManager refreshed Spring ConfigRepository dependency.");
		}
		if (fetcher != null) {
			m_fetcher = fetcher;
			LOGGER.info("SenderConfigManager refreshed Spring ContentFetcher dependency.");
		}
	}
}
