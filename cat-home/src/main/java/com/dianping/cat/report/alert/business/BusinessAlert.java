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
import org.springframework.stereotype.Component;
import com.dianping.cat.support.Threads.Task;
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
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;

import jakarta.annotation.Resource;

@Component
public class BusinessAlert implements Task {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessAlert.class);

	public static final String DEFAULT_TAG = "业务大盘";

	private static final long DURATION = TimeHelper.ONE_MINUTE;

	private static final int DATA_AREADY_MINUTE = 1;

	@Resource
	protected BaseRuleHelper baseRuleHelper;

	@Resource
	private BusinessRuleConfigManager businessRuleConfigManager;

	@Resource
	private BusinessConfigManager businessConfigManager;

	@Resource
	private BusinessTagConfigManager businessTagConfigManager;

	@Resource
	private BusinessReportGroupService businessReportGroupService;

	@Resource
	private ProjectService projectService;

	@Resource(name = "spiAlertManager")
	private AlertManager alertManager;

	@Resource
	private BusinessKeyHelper businessKeyHelper;

	@Resource
	private BaselineService baselineService;

	@Resource
	private DataChecker dataChecker;

	@Resource
	private CustomDataCalculator customDataCalculator;

	private void buidMonitorConfigs(String domain, String key,	Map<String, Map<MetricType, List<Config>>> monitorConfigs,
							Map<MetricType, List<Config>> defaultRules) {
		Map<MetricType, List<Config>> monitorConfigsByItem = new HashMap<MetricType, List<Config>>();

		for (MetricType type : MetricType.values()) {
			List<Config> configs = businessRuleConfigManager.queryConfigs(domain, key, type);

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
				Map<MetricType, List<Config>> defaultRules = businessRuleConfigManager.getDefaultRules(config);

				buidMonitorConfigs(domain, key, monitorConfigs, defaultRules);
			}
		}

		for (CustomConfig config : customConfigs.values()) {
			if (needAlert(config, domain)) {
				String key = config.getId();
				Map<MetricType, List<Config>> defaultRules = businessRuleConfigManager.getDefaultRulesForCustomItem();

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
		Set<String> tags = businessTagConfigManager.findTagByDomain(domain).get(config.getId());

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
		Set<String> tags = businessTagConfigManager.findTagByDomain(domain).get(config.getId());

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
			String metricKey = businessKeyHelper.generateKey(id, domain, type.getName());
			List<DataCheckEntity> tmpResults = processMetricType(minute, alertConfigEntry.getValue(), reportGroup,	metricKey,
									type);

			results.addAll(tmpResults);
		}
		return results;
	}

	private List<DataCheckEntity> processCustomItem(BusinessReportGroup currentReportGroup, List<Config> configs,
							int minute, String key, CustomConfig customConfig, int maxDuration) {
		try {
			Pair<Integer, List<Condition>> conditionPair = baseRuleHelper.convertConditions(configs);
			Map<String, double[]> businessItemDataCache = new HashMap<String, double[]>();
			Map<String, double[]> baseLineCache = new HashMap<String, double[]>();
			Map<String, BusinessReportGroup> reportGroupCache = new HashMap<String, BusinessReportGroup>();

			reportGroupCache.put(businessKeyHelper.getDomain(key), currentReportGroup);

			if (conditionPair != null) {
				int ruleMinute = conditionPair.getKey();
				String pattern = customConfig.getPattern();
				List<CustomInfo> customInfos = customDataCalculator.translatePattern(pattern);

				for (CustomInfo customInfo : customInfos) {
					String domain = customInfo.getDomain();

					if (!reportGroupCache.containsKey(domain)) {
						BusinessReportGroup tmpReportGroup = businessReportGroupService.prepareDatas(domain, minute, maxDuration);
						reportGroupCache.put(domain, tmpReportGroup);
					}
				}

				for (CustomInfo customInfo : customInfos) {
					String domain = customInfo.getDomain();
					String type = customInfo.getType();
					String id = customInfo.getKey();
					String metricKey = businessKeyHelper.generateKey(id, domain, type);
					BusinessReportGroup reportGroup = reportGroupCache.get(domain);
					double[] value = reportGroup.extractData(minute, ruleMinute, id, MetricType.getTypeByName(type));
					double[] baseline = baselineService.queryBaseline(minute, ruleMinute, metricKey, BusinessAnalyzer.ID);
					businessItemDataCache.put(metricKey, value);
					baseLineCache.put(metricKey, baseline);
				}

				double[] currentData = customDataCalculator.calculate(pattern, customInfos, businessItemDataCache,	ruleMinute);
				double[] currentBaseLine = customDataCalculator.calculate(pattern, customInfos, baseLineCache, ruleMinute);
				List<Condition> conditions = conditionPair.getValue();

				return dataChecker.checkData(currentData, currentBaseLine, conditions);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to process custom business alert item, key={}, minute={}, maxDuration={}, pattern={}.",
					key, minute, maxDuration, customConfig == null ? null : customConfig.getPattern(), e);
			Cat.logError(e);
		}
		return new ArrayList<DataCheckEntity>();
	}

	private void processDomain(String domain) {
		BusinessReportConfig businessReportConfig = businessConfigManager.queryConfigByDomain(domain);
		AlarmRule monitorConfigs = buildMonitorConfigs(domain, businessReportConfig);
		int minute = calAlreadyMinute();
		int maxDuration = monitorConfigs.calMaxRuleMinute();

		if (maxDuration > 0) {
			LOGGER.info("Processing business alert domain, domain={}, minute={}, maxDuration={}.", domain, minute,
					maxDuration);
			BusinessReportGroup reportGroup = businessReportGroupService.prepareDatas(domain, minute, maxDuration);

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
						String metricKey = businessKeyHelper.generateKey(id, domain, customType.getName());
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
		Pair<Integer, List<Condition>> conditionPair = baseRuleHelper.convertConditions(configs);

		if (conditionPair != null) {
			int ruleMinute = conditionPair.getKey();
			double[] value = reportGroup.extractData(minute, ruleMinute, businessKeyHelper.getBusinessItemId(metricKey), type);
			double[] baseline = baselineService.queryBaseline(minute, ruleMinute, metricKey, BusinessAnalyzer.ID);
			List<Condition> conditions = conditionPair.getValue();

			return dataChecker.checkData(value, baseline, conditions);
		} else {
			return new ArrayList<DataCheckEntity>();
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("AlertBusiness", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				Set<String> domains = projectService.findAllDomains();

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
			alertManager.addAlert(entity);
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
		businessRuleConfigManager = alertConfigManager;
	}

	public void setBaseRuleHelper(BaseRuleHelper baseRuleHelper) {
		this.baseRuleHelper = baseRuleHelper;
	}

	public void setBaselineService(BaselineService baselineService) {
		this.baselineService = baselineService;
	}

	public void setConfigManager(BusinessConfigManager configManager) {
		businessConfigManager = configManager;
	}

	public void setCustomDataCalculator(CustomDataCalculator customDataCalculator) {
		this.customDataCalculator = customDataCalculator;
	}

	public void setDataChecker(DataChecker dataChecker) {
		this.dataChecker = dataChecker;
	}

	public void setKeyHelper(BusinessKeyHelper keyHelper) {
		businessKeyHelper = keyHelper;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setSendManager(AlertManager sendManager) {
		alertManager = sendManager;
	}

	public void setService(BusinessReportGroupService service) {
		businessReportGroupService = service;
	}

	public void setTagConfigManager(BusinessTagConfigManager tagConfigManager) {
		businessTagConfigManager = tagConfigManager;
	}

}
