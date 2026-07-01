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
package com.dianping.cat.report.alert.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.entity.SubCondition;
import com.dianping.cat.alarm.rule.transform.DefaultJsonParser;
import com.dianping.cat.alarm.rule.transform.DefaultSaxParser;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.mybatis.data.BusinessConfigDO;
import com.dianping.cat.mybatis.mapper.BusinessConfigRepository;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class BusinessRuleConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessRuleConfigManager.class);

	private static final String ALERT_CONFIG = "alert";

	private static final String TYPE = "type";

	private static final String SPLITTER = ":";

	Map<String, MonitorRules> rules = new ConcurrentHashMap<String, MonitorRules>();

	@Resource
	private BusinessConfigRepository businessConfigRepository;

	private volatile boolean initialized;

	public void setConfigDao(BusinessConfigRepository configDao) {
		businessConfigRepository = configDao;
	}

	private List<Config> buildDefaultConfigs() {
		List<Config> configs = new ArrayList<Config>();
		Config config = new Config();
		config.setStarttime("00:00");
		config.setEndtime("24:00");

		Condition condition = new Condition();
		SubCondition descPerSubcon = new SubCondition();
		SubCondition descValSubcon = new SubCondition();
		SubCondition flucPerSubcon = new SubCondition();

		descPerSubcon.setType("DescPer").setText("50");
		descValSubcon.setType("DescVal").setText("100");
		flucPerSubcon.setType("FluDescPer").setText("20");
		condition.addSubCondition(descPerSubcon).addSubCondition(descValSubcon).addSubCondition(flucPerSubcon);
		config.addCondition(condition);

		configs.add(config);

		return configs;
	}

	private String generateRuleId(String key, String type) {
		return new StringBuilder().append(key).append(SPLITTER).append(type).toString();
	}

	public Map<MetricType, List<Config>> getDefaultRules(BusinessItemConfig config) {
		Map<MetricType, List<Config>> configs = new HashMap<MetricType, List<Config>>();

		if (config.isShowAvg()) {
			configs.put(MetricType.AVG, buildDefaultConfigs());
		}

		if (config.isShowCount()) {
			configs.put(MetricType.COUNT, buildDefaultConfigs());
		}

		if (config.isShowSum()) {
			configs.put(MetricType.SUM, buildDefaultConfigs());
		}
		return configs;
	}

	public Map<MetricType, List<Config>> getDefaultRulesForCustomItem() {
		Map<MetricType, List<Config>> configs = new HashMap<MetricType, List<Config>>();

		configs.put(MetricType.AVG, buildDefaultConfigs());

		return configs;
	}

	@PostConstruct
	public void initialize() {
		if (initialized) {
			return;
		}
		synchronized (this) {
			if (initialized) {
				return;
			}

			LOGGER.info("Initializing business alert rule config manager.");
			loadData();

			TimerSyncTask.getInstance().register(new SyncHandler() {

				@Override
				public String getName() {
					return ALERT_CONFIG;
				}

				@Override
				public void handle() throws Exception {
					loadData();
				}
			});
			initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	private void loadData() {
		try {
			List<BusinessConfigDO> configs = businessConfigRepository.findByName(ALERT_CONFIG);
			Map<String, MonitorRules> rules = new ConcurrentHashMap<String, MonitorRules>();

			for (BusinessConfigDO config : configs) {
				try {
					String domainName = config.getDomain();
					MonitorRules rule = DefaultSaxParser.parse(config.getContent());
					rules.put(domainName, rule);
				} catch (Exception e) {
					LOGGER.error("Unable to parse business alert rule config, domain={}, id={}.", config.getDomain(),
					      config.getId(), e);
					Cat.logError(e);
				}
			}
			this.rules = rules;
			LOGGER.info("Loaded business alert rule configs, count={}.", this.rules.size());
		} catch (RuntimeException e) {
			LOGGER.error("Unable to load business alert rule configs from repository.", e);
			Cat.logError(e);
		}
	}

	public List<Config> queryConfigs(String domain, String key, MetricType type) {
		ensureInitialized();
		String typeName = type.getName();
		Rule rule = queryRule(domain, key, typeName);
		List<Config> configs = new ArrayList<Config>();

		if (rule != null && rule.getDynamicAttribute(TYPE).equals(typeName)) {
			configs.addAll(rule.getConfigs());
		}

		return configs;
	}

	public MonitorRules queryMonitorRules(String domain) {
		ensureInitialized();
		return rules.get(domain);
	}

	public Rule queryRule(String domain, String key, String type) {
		ensureInitialized();
		MonitorRules rule = rules.get(domain);

		if (rule != null) {
			return rule.findRule(generateRuleId(key, type));
		} else {
			return null;
		}
	}

	public void updateRule(String domain, String key, String configsStr, String type) {
		ensureInitialized();
		try {
			Rule rule = new Rule(generateRuleId(key, type));
			List<Config> configs = DefaultJsonParser.parseArray(Config.class, configsStr);

			for (Config config : configs) {
				rule.addConfig(config);
			}

			rule.setDynamicAttribute(TYPE, type);

			boolean isExist = true;
			MonitorRules domainRule = rules.get(domain);

			if (domainRule == null) {
				domainRule = new MonitorRules();
				rules.put(domain, domainRule);
				isExist = false;
			}

			domainRule.getRules().put(rule.getId(), rule);

			BusinessConfigDO proto = businessConfigRepository.createLocal();
			proto.setDomain(domain);
			proto.setContent(domainRule.toString());
			proto.setName(ALERT_CONFIG);
			proto.setUpdateTime(new Date());

			if (isExist) {
				businessConfigRepository.updateBaseConfigByDomain(proto);
			} else {
				businessConfigRepository.insert(proto);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to update business alert rule, domain={}, key={}, type={}.", domain, key, type, e);
			Cat.logError(e);
		}

	}
}
