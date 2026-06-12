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
package com.dianping.cat.report.page;

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
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;
import com.dianping.cat.home.group.transform.DefaultSaxParser;
import com.dianping.cat.spring.CatSpringContext;

public class DomainGroupConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainGroupConfigManager.class);

	private static final String CONFIG_NAME = "domainGroup";

	private ConfigRepository m_configDao;

	private ContentFetcher m_fetcher;

	private int m_configId;

	private DomainGroup m_domainGroup;

	public DomainGroup getDomainGroup() {
		ensureInitialized();

		return m_domainGroup;
	}

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public void initialize() {
		refreshSpringBeans();

		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_domainGroup = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded domain group config from repository, configId={}.", m_configId);
		} catch (DalNotFoundException e) {
			LOGGER.warn("Domain group config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_domainGroup = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized domain group config from default content, configId={}.", m_configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize domain group config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load domain group config from repository.", e);
			Cat.logError(e);
		}
		if (m_domainGroup == null) {
			m_domainGroup = new DomainGroup();
			LOGGER.warn("Domain group config is empty after initialization, using a new empty config.");
		}
	}

	public boolean insert(String xml) {
		try {
			m_domainGroup = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse domain group xml for insert. xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public boolean insertFromJson(String json) {
		try {
			Domain domain = (Domain) new JsonBuilder().parse(json, Domain.class);

			m_domainGroup.addDomain(domain);
			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse domain group json for insert. jsonLength={}.", json == null ? 0 : json.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public boolean deleteGroup(String domain) {
		m_domainGroup.removeDomain(domain);

		return storeConfig();
	}

	public String queryDefaultGroup(String domain) {
		List<String> groups = queryDomainGroup(domain);

		if (groups.size() >= 1) {
			return groups.get(0);
		} else {
			return "";
		}
	}

	public Domain queryGroupDomain(String domain) {
		ensureInitialized();

		Domain domainGroup = m_domainGroup.findDomain(domain);

		return domainGroup;
	}

	public List<String> queryDomainGroup(String domain) {
		ensureInitialized();

		Domain domainGroup = m_domainGroup.findDomain(domain);

		if (domainGroup == null) {
			return new ArrayList<String>();
		} else {
			return new ArrayList<String>(domainGroup.getGroups().keySet());
		}
	}

	public List<String> queryIpByDomainAndGroup(String domain, String group) {
		ensureInitialized();

		Domain domainInfo = m_domainGroup.findDomain(domain);

		if (domainInfo != null) {
			Group groupInfo = domainInfo.findGroup(group);

			if (groupInfo != null) {
				return groupInfo.getIps();
			}
		}
		return new ArrayList<String>();
	}

	private void ensureInitialized() {
		if (m_domainGroup == null) {
			synchronized (this) {
				if (m_domainGroup == null) {
					LOGGER.warn("Domain group config is not initialized yet, loading it lazily.");
					initialize();
				}
			}
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			refreshSpringBeans();

			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_domainGroup.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
				LOGGER.info("Stored domain group config, configId={}, domainCount={}.", m_configId,
						m_domainGroup.getDomains().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store domain group config, configId={}.", m_configId, e);
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
			LOGGER.info("DomainGroupConfigManager refreshed Spring ConfigRepository dependency.");
		}
		if (fetcher != null) {
			m_fetcher = fetcher;
			LOGGER.info("DomainGroupConfigManager refreshed Spring ContentFetcher dependency.");
		}
	}
}
