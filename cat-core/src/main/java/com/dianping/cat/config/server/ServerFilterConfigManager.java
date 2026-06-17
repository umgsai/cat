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
package com.dianping.cat.config.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.springframework.dao.EmptyResultDataAccessException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.server.filter.entity.AtomicTreeConfig;
import com.dianping.cat.configuration.server.filter.entity.ServerFilterConfig;
import com.dianping.cat.configuration.server.filter.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

public class ServerFilterConfigManager {

	private static final String CONFIG_NAME = "serverFilter";

	protected ConfigRepository m_configDao;

	protected ContentFetcher m_fetcher;

	private volatile ServerFilterConfig m_config;

	private long m_configId;

	private long m_modifyTime;

	private volatile boolean m_initialized;

	public boolean discardTransaction(String type, String name) {
		ensureInitialized();

		if ("Cache.web".equals(type) || "ABTest".equals(type)) {
			return true;
		}
		if (m_config.getTransactionTypes().contains(type) && m_config.getTransactionNames().contains(name)) {
			return true;
		}
		return false;
	}

	public String getAtomicMatchTypes() {
		ensureInitialized();

		AtomicTreeConfig atomicTreeConfig = m_config.getAtomicTreeConfig();

		if (atomicTreeConfig != null) {
			return atomicTreeConfig.getMatchTypes();
		} else {
			return null;
		}
	}

	public String getAtomicStartTypes() {
		ensureInitialized();

		AtomicTreeConfig atomicTreeConfig = m_config.getAtomicTreeConfig();

		if (atomicTreeConfig != null) {
			return atomicTreeConfig.getStartTypes();
		} else {
			return null;
		}
	}

	public ServerFilterConfig getConfig() {
		ensureInitialized();
		return m_config;
	}

	public Set<String> getUnusedDomains() {
		ensureInitialized();

		Set<String> unusedDomains = new HashSet<String>();

		unusedDomains.addAll(m_config.getDomains());
		return unusedDomains;
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
			Config config = m_configDao.findByName(CONFIG_NAME);
			String content = config.getContent();

			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
			m_config = DefaultSaxParser.parse(content);
		} catch (EmptyResultDataAccessException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new ServerFilterConfig();
		}
		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}

			@Override
			public String getName() {
				return CONFIG_NAME;
			}
		});
		m_initialized = true;
	}

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public boolean insert(String xml) {
		ensureInitialized();

		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private void refreshConfig() throws SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				ServerFilterConfig serverConfig = DefaultSaxParser.parse(content);

				m_config = serverConfig;
				m_modifyTime = modifyTime;
			}
		}
	}

	public boolean storeConfig() {
		ensureInitialized();

		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_config.toString());
			m_configDao.updateByPK(config);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	public boolean validateDomain(String domain) {
		ensureInitialized();

		return !m_config.getDomains().contains(domain) && !m_config.getCrashLogDomains().containsKey(domain);
	}

}
