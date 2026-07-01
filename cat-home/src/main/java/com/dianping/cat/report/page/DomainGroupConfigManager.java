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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.mybatis.data.ConfigDO;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;
import com.dianping.cat.home.group.transform.DefaultSaxParser;

@Component
public class DomainGroupConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainGroupConfigManager.class);

	private static final String CONFIG_NAME = "domainGroup";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	private long configId;

	private DomainGroup domainGroup;

	public DomainGroup getDomainGroup() {
		ensureInitialized();

		return domainGroup;
	}

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	@PostConstruct
	public void initialize() {
		try {
			ConfigDO configDO = configRepository.findByName(CONFIG_NAME);
			String content = configDO.getContent();

			configId = configDO.getId();
			domainGroup = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded domain group config from repository, configId={}.", configId);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Domain group config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				ConfigDO configDO = configRepository.createLocal();

				configDO.setName(CONFIG_NAME);
				configDO.setContent(content);
				configRepository.insert(configDO);

				configId = configDO.getId();
				domainGroup = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized domain group config from default content, configId={}.", configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize domain group config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load domain group config from repository.", e);
			Cat.logError(e);
		}
		if (domainGroup == null) {
			domainGroup = new DomainGroup();
			LOGGER.warn("Domain group config is empty after initialization, using a new empty config.");
		}
	}

	public boolean insert(String xml) {
		try {
			domainGroup = DefaultSaxParser.parse(xml);

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

			domainGroup.addDomain(domain);
			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse domain group json for insert. jsonLength={}.", json == null ? 0 : json.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public boolean deleteGroup(String domain) {
		domainGroup.removeDomain(domain);

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

		Domain domainGroup = this.domainGroup.findDomain(domain);

		return domainGroup;
	}

	public List<String> queryDomainGroup(String domain) {
		ensureInitialized();

		Domain domainGroup = this.domainGroup.findDomain(domain);

		if (domainGroup == null) {
			return new ArrayList<String>();
		} else {
			return new ArrayList<String>(domainGroup.getGroups().keySet());
		}
	}

	public List<String> queryIpByDomainAndGroup(String domain, String group) {
		ensureInitialized();

		Domain domainInfo = domainGroup.findDomain(domain);

		if (domainInfo != null) {
			Group groupInfo = domainInfo.findGroup(group);

			if (groupInfo != null) {
				return groupInfo.getIps();
			}
		}
		return new ArrayList<String>();
	}

	private void ensureInitialized() {
		if (domainGroup == null) {
			synchronized (this) {
				if (domainGroup == null) {
					LOGGER.warn("Domain group config is not initialized yet, loading it lazily.");
					initialize();
				}
			}
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				ConfigDO configDO = configRepository.createLocal();

				configDO.setId(configId);
				configDO.setName(CONFIG_NAME);
				configDO.setContent(domainGroup.toString());
				configRepository.updateByPK(configDO);
				LOGGER.info("Stored domain group config, configId={}, domainCount={}.", configId,
						domainGroup.getDomains().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store domain group config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
}
