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
package com.dianping.cat.report.page.dependency.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;
import com.dianping.cat.home.dependency.format.transform.DefaultSaxParser;

@Component
public class TopoGraphFormatConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(TopoGraphFormatConfigManager.class);

	private static final String CONFIG_NAME = "topoGraphFormat";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	private long configId;

	private TopoGraphFormatConfig config;

	public String buildFormatJson() {
		ensureInitialized();

		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();

		for (ProductLine productline : config.getProductLines()) {
			Map<String, Integer> p = new HashMap<String, Integer>();

			map.put(productline.getId(), p);
			p.put("colInside", productline.getColInside());
		}
		return new JsonBuilder().toJson(map);
	}

	public TopoGraphFormatConfig getConfig() {
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
				LOGGER.error("Unable to create default topology graph format config, configName={}.", CONFIG_NAME, ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to initialize topology graph format config, configName={}.", CONFIG_NAME, e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new TopoGraphFormatConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			config = DefaultSaxParser.parse(xml);
			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to insert topology graph format config, xmlLength={}.", xml == null ? 0 : xml.length(),
			      e);
			Cat.logError(e);
			return false;
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

	public List<ProductLine> queryProduct() {
		ensureInitialized();

		return config.getProductLines();
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config configDO = configRepository.createLocal();

				configDO.setId(configId);
				configDO.setKeyId(configId);
				configDO.setName(CONFIG_NAME);
				configDO.setContent(config.toString());
				configRepository.updateByPK(configDO);
			} catch (Exception e) {
				LOGGER.error("Unable to store topology graph format config, configName={}, configId={}.", CONFIG_NAME,
				      configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
}
