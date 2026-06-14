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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.helper.Threads.Task;
import org.apache.commons.lang3.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.spi.AlarmRule;
import com.dianping.cat.report.page.business.graph.CustomDataCalculator;
import com.dianping.cat.report.page.business.graph.CustomInfo;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.spring.CatSpringContext;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;

public class BusinessAlert implements Task {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessAlert.class);

	public static final String DEFAULT_TAG = "业务大盘";

	private static final long DURATION = TimeHelper.ONE_MINUTE;

	private static final int DATA_AREADY_MINUTE = 1;

	protected BaseRuleHelper m_baseRuleHelper;

	private BusinessRuleConfigManager m_alertConfigManager;

	private BusinessConfigManager m_configManager;

	private BusinessTagConfigManager m_tagConfigManager;

	private BusinessReportGroupService m_service;

	private ProjectService m_projectService;

	private AlertManager m_sendManager;

	private BusinessKeyHelper m_keyHelper;

	private BaselineService m_baselineService;

	private DataChecker m_dataChecker;

	private CustomDataCalculator m_customDataCalculator;

	private void buidMonitorConfigs(String domain, String key,	Map<String, Map<MetricType, List<Config>>> monitorConfigs,
							Map<MetricType, List<Config>> defaultRules) {
		Map<MetricType, List<Config>> monitorConfigsByItem = new HashMap<MetricType, List<Config>>();

		for (MetricType type : MetricType.values()) {
			List<Config> configs = m_alertConfigManager.queryConfigs(domain, key, type);

			if (configs != null && configs.size() > 0) {
				monitorConfigsByItem.put(type, configs);
			}
		}

		if (monitorConfigsByItem.isEmpty()) {
			monitorConfigs.put(key, defaultRules);
		} else {
			monitorConfigs.put(key, monitorConfigsByItem);
		}
	}

	private AlarmRule buildMonitorConfigs(String domain, BusinessReportConfig businessReportConfig) {
		Map<String, Map<MetricType, List<Config>>> monitorConfigs = new HashMap<String, Map<MetricType, List<Config>>>();
		Map<String, BusinessItemConfig> itemConfigs = businessReportConfig.getBusinessItemConfigs();
		Map<String, CustomConfig> customConfigs = businessReportConfig.getCustomConfigs();

		for (BusinessItemConfig config : itemConfigs.values()) {
			if (needAlert(config, domain)) {
				String key = config.getId();
				Map<MetricType, List<Config>> defaultRules = m_alertConfigManager.getDefaultRules(config);

				buidMonitorConfigs(domain, key, monitorConfigs, defaultRules);
			}
		}

		for (CustomConfig config : customConfigs.values()) {
			if (needAlert(config, domain)) {
				String key = config.getId();
				Map<MetricType, List<Config>> defaultRules = m_alertConfigManager.getDefaultRulesForCustomItem();

				buidMonitorConfigs(domain, key, monitorConfigs, defaultRules);
			}
		}

		return new AlarmRule(monitorConfigs);
	}

	private int calAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		return minute;
	}

	@Override
	public String getName() {
		return AlertType.Business.getName();
	}

	private boolean needAlert(BusinessItemConfig config, String domain) {
		if (config.isAlarm()) {
			return true;
		}
		Set<String> tags = m_tagConfigManager.findTagByDomain(domain).get(config.getId());

		if (tags != null && tags.contains(DEFAULT_TAG)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean needAlert(CustomConfig config, String domain) {
		if (config.isAlarm()) {
			return true;
		}
		Set<String> tags = m_tagConfigManager.findTagByDomain(domain).get(config.getId());

		if (tags != null && tags.contains(DEFAULT_TAG)) {
			return true;
		} else {
			return false;
		}
	}

	private List<DataCheckEntity> processBusinessItem(BusinessReportGroup reportGroup,
							Map<MetricType, List<Config>> alertConfig, String id, int minute, String domain) {
		List<DataCheckEntity> results = new ArrayList<DataCheckEntity>();

		for (Entry<MetricType, List<Config>> alertConfigEntry : alertConfig.entrySet()) {
			MetricType type = alertConfigEntry.getKey();
			String metricKey = m_keyHelper.generateKey(id, domain, type.getName());
			List<DataCheckEntity> tmpResults = processMetricType(minute, alertConfigEntry.getValue(), reportGroup,	metricKey,
									type);

			results.addAll(tmpResults);
		}
		return results;
	}

	private List<DataCheckEntity> processCustomItem(BusinessReportGroup currentReportGroup, List<Config> configs,
							int minute, String key, CustomConfig customConfig, int maxDuration) {
		try {
			Pair<Integer, List<Condition>> conditionPair = m_baseRuleHelper.convertConditions(configs);
			Map<String, double[]> businessItemDataCache = new HashMap<String, double[]>();
			Map<String, double[]> baseLineCache = new HashMap<String, double[]>();
			Map<String, BusinessReportGroup> reportGroupCache = new HashMap<String, BusinessReportGroup>();

			reportGroupCache.put(m_keyHelper.getDomain(key), currentReportGroup);

			if (conditionPair != null) {
				int ruleMinute = conditionPair.getKey();
				String pattern = customConfig.getPattern();
				List<CustomInfo> customInfos = m_customDataCalculator.translatePattern(pattern);

				for (CustomInfo customInfo : customInfos) {
					String domain = customInfo.getDomain();

					if (!reportGroupCache.containsKey(domain)) {
						BusinessReportGroup tmpReportGroup = m_service.prepareDatas(domain, minute, maxDuration);
						reportGroupCache.put(domain, tmpReportGroup);
					}
				}

				for (CustomInfo customInfo : customInfos) {
					String domain = customInfo.getDomain();
					String type = customInfo.getType();
					String id = customInfo.getKey();
					String metricKey = m_keyHelper.generateKey(id, domain, type);
					BusinessReportGroup reportGroup = reportGroupCache.get(domain);
					double[] value = reportGroup.extractData(minute, ruleMinute, id, MetricType.getTypeByName(type));
					double[] baseline = m_baselineService.queryBaseline(minute, ruleMinute, metricKey, BusinessAnalyzer.ID);
					businessItemDataCache.put(metricKey, value);
					baseLineCache.put(metricKey, baseline);
				}

				double[] currentData = m_customDataCalculator.calculate(pattern, customInfos, businessItemDataCache,	ruleMinute);
				double[] currentBaseLine = m_customDataCalculator.calculate(pattern, customInfos, baseLineCache, ruleMinute);
				List<Condition> conditions = conditionPair.getValue();

				return m_dataChecker.checkData(currentData, currentBaseLine, conditions);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to process custom business alert item, key={}, minute={}, maxDuration={}, pattern={}.",
					key, minute, maxDuration, customConfig == null ? null : customConfig.getPattern(), e);
			Cat.logError(e);
		}
		return new ArrayList<DataCheckEntity>();
	}

	private void processDomain(String domain) {
		refreshSpringBeans();

		BusinessReportConfig businessReportConfig = m_configManager.queryConfigByDomain(domain);
		AlarmRule monitorConfigs = buildMonitorConfigs(domain, businessReportConfig);
		int minute = calAlreadyMinute();
		int maxDuration = monitorConfigs.calMaxRuleMinute();

		if (maxDuration > 0) {
			LOGGER.info("Processing business alert domain, domain={}, minute={}, maxDuration={}.", domain, minute,
					maxDuration);
			BusinessReportGroup reportGroup = m_service.prepareDatas(domain, minute, maxDuration);

			if (reportGroup.isDataReady()) {
				Collection<BusinessItemConfig> configs = businessReportConfig.getBusinessItemConfigs().values();
				Collection<CustomConfig> customConfigs = businessReportConfig.getCustomConfigs().values();

				for (BusinessItemConfig itemConfig : configs) {
					String id = itemConfig.getId();
					Map<MetricType, List<Config>> alertConfig = monitorConfigs.getConfigs().get(id);

					if (alertConfig != null) {
						List<DataCheckEntity> results = processBusinessItem(reportGroup, alertConfig, id, minute, domain);
						sendBusinessAlerts(domain, itemConfig.getId(), results);
					}
				}

				for (CustomConfig customConfig : customConfigs) {
					String id = customConfig.getId();
					Map<MetricType, List<Config>> alertConfig = monitorConfigs.getConfigs().get(id);
					MetricType customType = MetricType.AVG;

					if (alertConfig != null) {
						String metricKey = m_keyHelper.generateKey(id, domain, customType.getName());
						List<DataCheckEntity> results = processCustomItem(reportGroup, alertConfig.get(customType), minute,	metricKey,
												customConfig, maxDuration);
						sendBusinessAlerts(domain, customConfig.getId(), results);
					}
				}
			}
		}
	}

	private List<DataCheckEntity> processMetricType(int minute, List<Config> configs, BusinessReportGroup reportGroup,
							String metricKey, MetricType type) {
		Pair<Integer, List<Condition>> conditionPair = m_baseRuleHelper.convertConditions(configs);

		if (conditionPair != null) {
			int ruleMinute = conditionPair.getKey();
			double[] value = reportGroup.extractData(minute, ruleMinute, m_keyHelper.getBusinessItemId(metricKey), type);
			double[] baseline = m_baselineService.queryBaseline(minute, ruleMinute, metricKey, BusinessAnalyzer.ID);
			List<Condition> conditions = conditionPair.getValue();

			return m_dataChecker.checkData(value, baseline, conditions);
		} else {
			return new ArrayList<DataCheckEntity>();
		}
	}

	@Override
	public void run() {
		refreshSpringBeans();

		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			refreshSpringBeans();

			Transaction t = Cat.newTransaction("AlertBusiness", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				Set<String> domains = m_projectService.findAllDomains();

				LOGGER.info("Business alert cycle started, domainCount={}.", domains.size());
				for (String domain : domains) {
					try {
						processDomain(domain);
					} catch (Exception e) {
						LOGGER.error("Unable to process business alert domain, domain={}.", domain, e);
						Cat.logError(e);
					}
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				LOGGER.error("Business alert cycle failed.", e);
				Cat.logError(e);
			} finally {
				t.complete();
			}

			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				LOGGER.warn("Business alert task interrupted.");
				active = false;
			}
		}
	}

	private void sendBusinessAlerts(String domain, String metricName, List<DataCheckEntity> alertResults) {
		for (DataCheckEntity alertResult : alertResults) {
			AlertEntity entity = new AlertEntity();

			entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
									.setLevel(alertResult.getAlertLevel());
			entity.setMetric(metricName).setType(getName()).setDomain(domain).setGroup(domain);
			entity.setContactGroup(domain);
			m_sendManager.addAlert(entity);
		}
		if (!alertResults.isEmpty()) {
			LOGGER.info("Business alerts queued, domain={}, metric={}, alertCount={}.", domain, metricName,
					alertResults.size());
		}
	}

	@Override
	public void shutdown() {
	}

	public void setAlertConfigManager(BusinessRuleConfigManager alertConfigManager) {
		m_alertConfigManager = alertConfigManager;
	}

	public void setBaseRuleHelper(BaseRuleHelper baseRuleHelper) {
		m_baseRuleHelper = baseRuleHelper;
	}

	public void setBaselineService(BaselineService baselineService) {
		m_baselineService = baselineService;
	}

	public void setConfigManager(BusinessConfigManager configManager) {
		m_configManager = configManager;
	}

	public void setCustomDataCalculator(CustomDataCalculator customDataCalculator) {
		m_customDataCalculator = customDataCalculator;
	}

	public void setDataChecker(DataChecker dataChecker) {
		m_dataChecker = dataChecker;
	}

	public void setKeyHelper(BusinessKeyHelper keyHelper) {
		m_keyHelper = keyHelper;
	}

	public void setProjectService(ProjectService projectService) {
		m_projectService = projectService;
	}

	public void setSendManager(AlertManager sendManager) {
		m_sendManager = sendManager;
	}

	public void setService(BusinessReportGroupService service) {
		m_service = service;
	}

	public void setTagConfigManager(BusinessTagConfigManager tagConfigManager) {
		m_tagConfigManager = tagConfigManager;
	}

	private void refreshSpringBeans() {
		BusinessConfigManager configManager = CatSpringContext.getBeanIfAvailable(BusinessConfigManager.class);
		ProjectService projectService = CatSpringContext.getBeanIfAvailable(ProjectService.class);
		BusinessRuleConfigManager alertConfigManager = CatSpringContext.getBeanIfAvailable(BusinessRuleConfigManager.class);
		BusinessTagConfigManager tagConfigManager = CatSpringContext.getBeanIfAvailable(BusinessTagConfigManager.class);
		BaseRuleHelper baseRuleHelper = CatSpringContext.getBeanIfAvailable(BaseRuleHelper.class);
		BusinessKeyHelper keyHelper = CatSpringContext.getBeanIfAvailable(BusinessKeyHelper.class);
		CustomDataCalculator customDataCalculator = CatSpringContext.getBeanIfAvailable(CustomDataCalculator.class);

		if (configManager != null) {
			m_configManager = configManager;
		}
		if (projectService != null) {
			m_projectService = projectService;
		}
		if (alertConfigManager != null) {
			m_alertConfigManager = alertConfigManager;
		}
		if (tagConfigManager != null) {
			m_tagConfigManager = tagConfigManager;
		}
		if (baseRuleHelper != null) {
			m_baseRuleHelper = baseRuleHelper;
		}
		if (keyHelper != null) {
			m_keyHelper = keyHelper;
		}
		if (customDataCalculator != null) {
			m_customDataCalculator = customDataCalculator;
		}
	}

}
