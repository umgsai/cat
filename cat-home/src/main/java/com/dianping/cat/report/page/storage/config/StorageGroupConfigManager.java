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
package com.dianping.cat.report.page.storage.config;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.home.storage.entity.Link;
import com.dianping.cat.home.storage.entity.Storage;
import com.dianping.cat.home.storage.entity.StorageGroup;
import com.dianping.cat.home.storage.entity.StorageGroupConfig;
import com.dianping.cat.home.storage.transform.DefaultSaxParser;

@Component
public class StorageGroupConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageGroupConfigManager.class);

	public static final String IP_FORMAT = "${ip}";

	public static final String ID_FORMAT = "${id}";

	public static final String DEFAULT = "Default";

	private static final String CONFIG_NAME = "storageGroup";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	private long configId;

	private StorageGroupConfig config;

	public String buildUrl(String format, String id, String ip) {
		try {
			return format.replace(ID_FORMAT, URLEncoder.encode(id, "utf-8")).replace(IP_FORMAT,	URLEncoder.encode(ip, "utf-8"));
		} catch (Exception e) {
			LOGGER.error("Unable to build storage link url, id={}, ip={}.", id, ip, e);
			Cat.logError("can't encode [id: " + id + "] [ip: " + ip + "]", e);
			return null;
		}
	}

	public StorageGroupConfig getConfig() {
		ensureInitialized();

		return config;
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
			Config configDO = configRepository.findByName(CONFIG_NAME);
			String content = configDO.getContent();

			configId = configDO.getId();
			config = DefaultSaxParser.parse(content);
		} catch (EmptyResultDataAccessException e) {
			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				Config configDO = configRepository.createLocal();

				configDO.setName(CONFIG_NAME);
				configDO.setContent(content);
				configRepository.insert(configDO);

				configId = configDO.getId();
				config = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				LOGGER.error("Unable to create default storage group config, configName={}.", CONFIG_NAME, ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to initialize storage group config, configName={}.", CONFIG_NAME, e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new StorageGroupConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to insert storage group config, xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public String queryLinkFormat(String type) {
		StorageGroup group = queryStorageGroup(type);
		Link link = group.getLink();

		if (link != null) {
			String url = link.getUrl();
			List<String> pars = link.getPars();

			return url + "?" + StringUtils.join(pars, "&");
		} else {
			return null;
		}
	}

	public Map<String, Department> queryStorageDepartments(List<String> ids, String type) {
		ensureInitialized();

		Map<String, Department> departments = new LinkedHashMap<String, Department>();

		for (String id : ids) {
			Storage storage = queryStorageGroup(type).getStorages().get(id);
			String department;
			String product;

			if (storage != null) {
				department = storage.getDepartment();
				product = storage.getProductline();
			} else {
				department = DEFAULT;
				product = DEFAULT;
			}
			Department depart = departments.get(department);

			if (depart == null) {
				depart = new Department(department);

				departments.put(department, depart);
			}

			depart.findOrCreateProductline(product).addStorage(id);
		}
		return departments;
	}

	public StorageGroup queryStorageGroup(String type) {
		ensureInitialized();

		StorageGroup group = config.getStorageGroups().get(type);

		if (group != null) {
			return group;
		} else {
			return new StorageGroup();
		}
	}

	private void ensureInitialized() {
		if (config == null) {
			synchronized (this) {
				if (config == null) {
					initialize();
				}
			}
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			ensureInitialized();

			try {
				Config configDO = configRepository.createLocal();

				configDO.setId(configId);
				configDO.setKeyId(configId);
				configDO.setName(CONFIG_NAME);
				configDO.setContent(config.toString());
				configRepository.updateByPK(configDO);
				return true;
			} catch (Exception e) {
				LOGGER.error("Unable to store storage group config, configName={}, configId={}.", CONFIG_NAME, configId,
				      e);
				Cat.logError(e);
				return false;
			}
		}
	}

	public static class Department {

		private String id;

		private Map<String, Productline> productlines = new LinkedHashMap<String, Productline>();

		public Department(String id) {
			this.id = id;
		}

		public Productline findOrCreateProductline(String productline) {
			Productline product = productlines.get(productline);

			if (product == null) {
				product = new Productline(productline);

				productlines.put(productline, product);
			}
			return product;
		}

		public String getId() {
			return id;
		}

		public Map<String, Productline> getProductlines() {
			return productlines;
		}
	}

	public static class Productline {

		private String id;

		private List<String> storages = new LinkedList<String>();

		public Productline(String id) {
			this.id = id;
		}

		public List<String> addStorage(String storage) {
			storages.add(storage);
			return storages;
		}

		public String getId() {
			return id;
		}

		public List<String> getStorages() {
			return storages;
		}
	}
}
