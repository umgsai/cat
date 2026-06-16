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

import org.springframework.dao.EmptyResultDataAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;
import com.dianping.cat.home.dependency.format.transform.DefaultSaxParser;

public class TopoGraphFormatConfigManager {

	private static final String CONFIG_NAME = "topoGraphFormat";

	private ConfigRepository m_configDao;

	private ContentFetcher m_fetcher;

	private int m_configId;

	private TopoGraphFormatConfig m_config;

	public String buildFormatJson() {
		ensureInitialized();

		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();

		for (ProductLine productline : m_config.getProductLines()) {
			Map<String, Integer> p = new HashMap<String, Integer>();

			map.put(productline.getId(), p);
			p.put("colInside", productline.getColInside());
		}
		return new JsonBuilder().toJson(map);
	}

	public TopoGraphFormatConfig getConfig() {
		ensureInitialized();

		return m_config;
	}

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME);
			String content = config.getContent();

			m_configId = config.getId();
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
			m_config = new TopoGraphFormatConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);
			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private void ensureInitialized() {
		if (m_config == null) {
			synchronized (this) {
				if (m_config == null) {
					initialize();
				}
			}
		}
	}

	public List<ProductLine> queryProduct() {
		ensureInitialized();

		return m_config.getProductLines();
	}

	private boolean storeConfig() {
		synchronized (this) {
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
		}
		return true;
	}
}
