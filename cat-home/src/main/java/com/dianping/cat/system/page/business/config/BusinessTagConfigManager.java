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
package com.dianping.cat.system.page.business.config;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.mybatis.data.BusinessConfigDO;
import com.dianping.cat.mybatis.mapper.BusinessConfigRepository;
import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;
import com.dianping.cat.home.business.transform.DefaultSaxParser;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class BusinessTagConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessTagConfigManager.class);

	public final static String TAG_CONFIG = "tag";

	@Resource
	private BusinessConfigRepository businessConfigRepository;

	private long configId;

	private BusinessTagConfig tagConfig = new BusinessTagConfig();

	private volatile boolean initialized;

	public void setConfigDao(BusinessConfigRepository configDao) {
		businessConfigRepository = configDao;
	}

	public Set<String> findAllTags() {
		ensureInitialized();

		return tagConfig.getTags().keySet();
	}

	public Tag findTag(String id) {
		ensureInitialized();

		return tagConfig.findTag(id);
	}

	public Map<String, Set<String>> findTagByDomain(String domain) {
		ensureInitialized();

		Map<String, Set<String>> domainTags = new HashMap<String, Set<String>>();
		Map<String, Tag> tags = tagConfig.getTags();

		for (Tag tag : tags.values()) {
			List<BusinessItem> items = tag.getBusinessItems();

			for (BusinessItem item : items) {
				if (item.getDomain().equals(domain)) {
					String id = item.getItemId();
					Set<String> itemTags = domainTags.get(id);

					if (itemTags == null) {
						itemTags = new HashSet<String>();
						domainTags.put(id, itemTags);
					}

					itemTags.add(tag.getId());
				}
			}
		}
		return domainTags;
	}

	public BusinessTagConfig getConfig() {
		ensureInitialized();
		return tagConfig;
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
			List<BusinessConfigDO> result = businessConfigRepository.findByName(TAG_CONFIG);

			if (result.size() > 0) {
				BusinessConfigDO config = result.get(0);
				configId = config.getId();
				tagConfig = DefaultSaxParser.parse(config.getContent());
				LOGGER.info("Loaded business tag config from repository, configId={}, tagCount={}.", configId,
						tagConfig.getTags().size());
			} else {
				tagConfig = new BusinessTagConfig();

				BusinessConfigDO config = businessConfigRepository.createLocal();

				config.setName(TAG_CONFIG);
				config.setDomain(Constants.CAT);
				config.setContent(tagConfig.toString());
				config.setUpdateTime(new Date());

				businessConfigRepository.insert(config);
				configId = config.getId();
				LOGGER.info("Initialized empty business tag config, configId={}.", configId);
			}

		} catch (Exception e) {
			LOGGER.error("Unable to initialize business tag config.", e);
			Cat.logError(e);
		}
		initialized = true;
	}

	public boolean store(String xml) {
		ensureInitialized();

		try {
			tagConfig = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse business tag config xml for store. xmlLength={}.",
					xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				BusinessConfigDO config = businessConfigRepository.createLocal();

				config.setId(configId);
				config.setName(TAG_CONFIG);
				config.setDomain(Constants.CAT);
				config.setContent(tagConfig.toString());
				config.setUpdateTime(new Date());
				businessConfigRepository.updateByPK(config);
				LOGGER.info("Stored business tag config, configId={}, tagCount={}.", configId,
						tagConfig.getTags().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store business tag config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
