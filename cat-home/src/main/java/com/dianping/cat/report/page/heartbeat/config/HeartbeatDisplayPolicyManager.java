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
package com.dianping.cat.report.page.heartbeat.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;


import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.home.heartbeat.entity.Group;
import com.dianping.cat.home.heartbeat.entity.HeartbeatDisplayPolicy;
import com.dianping.cat.home.heartbeat.entity.Metric;
import com.dianping.cat.home.heartbeat.transform.DefaultSaxParser;

@Component
public class HeartbeatDisplayPolicyManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatDisplayPolicyManager.class);

	private static final int K = 1024;

	private static final String CONFIG_NAME = "heartbeat-display-policy";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	private long configId;

	private HeartbeatDisplayPolicy config;

	public HeartbeatDisplayPolicy getHeartbeatDisplayPolicy() {
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
				LOGGER.error("Unable to create default heartbeat display policy, configName={}.", CONFIG_NAME, ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to initialize heartbeat display policy, configName={}.", CONFIG_NAME, e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new HeartbeatDisplayPolicy();
		}
	}

	public boolean insert(String xml) {
		try {
			config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to insert heartbeat display policy, xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public boolean isDelta(String groupName, String metricName) {
		ensureInitialized();

		Group group = config.findGroup(groupName);

		if (group != null) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				return metric.isDelta();
			}
		}
		return false;
	}

	public Metric queryMetric(String groupName, String metricName) {
		ensureInitialized();

		Group group = config.findGroup(groupName);

		if (group != null) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				return metric;
			}
		}
		return null;
	}

	public List<String> queryAlertMetrics() {
		ensureInitialized();

		List<String> metrics = new ArrayList<String>();

		for (Group group : config.getGroups().values()) {
			String groupId = group.getId();

			for (Metric metric : group.getMetrics().values()) {
				if (metric.isAlert()) {
					metrics.add(groupId + ":" + metric.getId());
				}
			}
		}
		return metrics;
	}

	public int queryUnit(String groupName, String metricName) {
		ensureInitialized();

		Group group = config.findGroup(groupName);

		if (group != null) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				String metricUnit = metric.getUnit();

				if ("K".equals(metricUnit)) {
					return K;
				} else if ("M".equals(metricUnit)) {
					return K * K;
				} else if ("G".equals(metricUnit)) {
					return K * K * K;
				} else {
					return Integer.parseInt(metricUnit);
				}
			}
		}
		return 1;
	}

	public List<String> sortGroupNames(List<String> originGroupNames) {
		ensureInitialized();

		List<Group> groups = new ArrayList<Group>();

		for (Entry<String, Group> entry : config.getGroups().entrySet()) {
			if (originGroupNames.contains(entry.getKey())) {
				groups.add(entry.getValue());
			}
		}
		Collections.sort(groups, new Comparator<Group>() {
			@Override
			public int compare(Group g1, Group g2) {
				return g1.getOrder() - g2.getOrder();
			}
		});

		List<String> result = new ArrayList<String>();

		for (Group group : groups) {
			result.add(group.getId());
		}
		for (String originGroupName : originGroupNames) {
			if (!result.contains(originGroupName)) {
				result.add(originGroupName);
			}
		}
		return result;
	}

	public List<String> sortGroupNames(Set<String> originGroupNameSet) {
		return sortGroupNames(new ArrayList<String>(originGroupNameSet));
	}

	public List<String> sortMetricNames(String groupName, List<String> originMetricNames) {
		ensureInitialized();

		Group group = config.findGroup(groupName);
		List<String> result = new ArrayList<String>();

		if (group != null) {
			List<Metric> list = new ArrayList<Metric>();

			for (Entry<String, Metric> entry : group.getMetrics().entrySet()) {
				if (originMetricNames.contains(entry.getKey())) {
					list.add(entry.getValue());
				}
			}
			Collections.sort(list, new Comparator<Metric>() {
				@Override
				public int compare(Metric m1, Metric m2) {
					return m1.getOrder() - m2.getOrder();
				}
			});
			for (Metric metric : list) {
				result.add(metric.getId());
			}
		}

		for (String originMetricName : originMetricNames) {
			if (!result.contains(originMetricName)) {
				result.add(originMetricName);
			}
		}
		return result;
	}

	public List<String> sortMetricNames(String groupName, Set<String> originMetricNames) {
		return sortMetricNames(groupName, new ArrayList<String>(originMetricNames));
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
			} catch (Exception e) {
				LOGGER.error("Unable to store heartbeat display policy, configName={}, configId={}.", CONFIG_NAME,
				      configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
}
