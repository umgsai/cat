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

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.mybatis.repository.business.config.BusinessConfigRepository;
import com.dianping.cat.core.config.BusinessConfigEntity;
import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;
import com.dianping.cat.home.business.transform.DefaultSaxParser;
import com.dianping.cat.spring.CatSpringContext;

public class BusinessTagConfigManager implements Initializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessTagConfigManager.class);

	public final static String TAG_CONFIG = "tag";

	@Inject
	private BusinessConfigRepository m_configDao;

	private int m_configId;

	private BusinessTagConfig m_tagConfig = new BusinessTagConfig();

	public void setConfigDao(BusinessConfigRepository configDao) {
		m_configDao = configDao;
	}

	public Set<String> findAllTags() {
		return m_tagConfig.getTags().keySet();
	}

	public Tag findTag(String id) {
		return m_tagConfig.findTag(id);
	}

	public Map<String, Set<String>> findTagByDomain(String domain) {
		Map<String, Set<String>> domainTags = new HashMap<String, Set<String>>();
		Map<String, Tag> tags = m_tagConfig.getTags();

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
		return m_tagConfig;
	}

	@Override
	public void initialize() throws InitializationException {
		refreshSpringBeans();

		try {
			List<BusinessConfig> result = m_configDao.findByName(TAG_CONFIG, BusinessConfigEntity.READSET_FULL);

			if (result.size() > 0) {
				BusinessConfig config = result.get(0);
				m_configId = config.getId();
				m_tagConfig = DefaultSaxParser.parse(config.getContent());
				LOGGER.info("Loaded business tag config from repository, configId={}, tagCount={}.", m_configId,
						m_tagConfig.getTags().size());
			} else {
				m_tagConfig = new BusinessTagConfig();

				BusinessConfig config = m_configDao.createLocal();

				config.setName(TAG_CONFIG);
				config.setDomain(Constants.CAT);
				config.setContent(m_tagConfig.toString());
				config.setUpdatetime(new Date());

				m_configDao.insert(config);
				m_configId = config.getId();
				LOGGER.info("Initialized empty business tag config, configId={}.", m_configId);
			}

		} catch (Exception e) {
			LOGGER.error("Unable to initialize business tag config.", e);
			Cat.logError(e);
		}
	}

	public boolean store(String xml) {
		try {
			m_tagConfig = DefaultSaxParser.parse(xml);

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
			refreshSpringBeans();

			try {
				BusinessConfig config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(TAG_CONFIG);
				config.setContent(m_tagConfig.toString());
				config.setUpdatetime(new Date());
				m_configDao.updateByPK(config, BusinessConfigEntity.UPDATESET_FULL);
				LOGGER.info("Stored business tag config, configId={}, tagCount={}.", m_configId,
						m_tagConfig.getTags().size());
			} catch (Exception e) {
				LOGGER.error("Unable to store business tag config, configId={}.", m_configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	private void refreshSpringBeans() {
		BusinessConfigRepository configDao = CatSpringContext.getBeanIfAvailable(BusinessConfigRepository.class);

		if (configDao != null) {
			m_configDao = configDao;
			LOGGER.info("BusinessTagConfigManager refreshed Spring BusinessConfigRepository dependency.");
		}
	}

}
