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
package com.dianping.cat.system.page.config.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.MetricItem;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.report.alert.spi.config.BaseRuleConfigManager;
import com.dianping.cat.system.page.config.Model;

public class BaseProcesser {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseProcesser.class);

	@Resource(name = "ruleFTLDecorator")
	protected RuleFTLDecorator ruleDecorator;

	public boolean addSubmitRule(BaseRuleConfigManager manager, String id, String metrics, String configs) {
		try {
			String xmlContent = manager.updateRule(id, metrics, configs);

			return manager.insert(xmlContent);
		} catch (Exception ex) {
			LOGGER.error("Unable to add or update rule, id={}, metrics={}.", id, metrics, ex);
			Cat.logError(ex);
			return false;
		}
	}

	public boolean addSubmitRule(BaseRuleConfigManager manager, String id, String metrics,
								 String configs, Boolean available) {
		try {
			String xmlContent = manager.updateRule(id, metrics, configs, available);

			return manager.insert(xmlContent);
		} catch (Exception ex) {
			LOGGER.error("Unable to add or update rule with available flag, id={}, metrics={}, available={}.", id,
			      metrics, available, ex);
			Cat.logError(ex);
			return false;
		}
	}

	public boolean deleteRule(BaseRuleConfigManager manager, String key) {
		try {
			String xmlContent = manager.deleteRule(key);
			return manager.insert(xmlContent);
		} catch (Exception ex) {
			LOGGER.error("Unable to delete rule, key={}.", key, ex);
			Cat.logError(ex);
			return false;
		}
	}

	public void generateRuleConfigContent(String key, BaseRuleConfigManager manager, Model model) {
		String configsStr = "";
		String ruleId = "";

		if (StringUtils.isNotEmpty(key)) {
			Rule rule = manager.queryRule(key);

			if (rule != null) {
				ruleId = rule.getId();
				configsStr = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
				String configHeader = new DefaultJsonBuilder(true).buildArray(rule.getMetricItems());

				if (null != rule.getAvailable()) {
					model.setAvailable(rule.getAvailable());
				}

				model.setConfigHeader(configHeader);
			}
		}
		String content = ruleDecorator.generateConfigsHtml(configsStr);

		model.setContent(content);
		model.setId(ruleId);
	}

	public void generateRuleItemList(BaseRuleConfigManager manager, Model model) {
		Collection<Rule> rules = manager.getMonitorRules().getRules().values();
		List<RuleItem> ruleItems = new ArrayList<RuleItem>();

		for (Rule rule : rules) {
			String id = rule.getId();
			List<MetricItem> items = rule.getMetricItems();

			if (items.size() > 0) {
				MetricItem item = items.get(0);
				String productText = item.getProductText();
				String metricText = item.getMetricItemText();
				RuleItem ruleItem = new RuleItem(id, productText, metricText);

				if (null == rule.getAvailable()) {
					ruleItem.setAvailable(true);
				} else {
					ruleItem.setAvailable(rule.getAvailable());
				}

				ruleItem.setMonitorCount(item.isMonitorCount());
				ruleItem.setMonitorAvg(item.isMonitorAvg());
				ruleItem.setMonitorSum(item.isMonitorSum());

				ruleItems.add(ruleItem);
			}
		}
		model.setRuleItems(ruleItems);
	}

	public class RuleItem {
		private String id;

		private boolean available;

		private String productlineText;

		private String metricText;

		private boolean monitorCount;

		private boolean monitorSum;

		private boolean monitorAvg;

		public RuleItem(String id, String productlineText, String metricText) {
			this.id = id;
			this.productlineText = productlineText;
			this.metricText = metricText;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public boolean isAvailable() {
			return available;
		}

		public void setAvailable(boolean available) {
			this.available = available;
		}

		public String getMetricText() {
			return metricText;
		}

		public void setMetricText(String metricText) {
			this.metricText = metricText;
		}

		public String getProductlineText() {
			return productlineText;
		}

		public void setProductlineText(String productlineText) {
			this.productlineText = productlineText;
		}

		public boolean isMonitorAvg() {
			return monitorAvg;
		}

		public void setMonitorAvg(boolean monitorAvg) {
			this.monitorAvg = monitorAvg;
		}

		public boolean isMonitorCount() {
			return monitorCount;
		}

		public void setMonitorCount(boolean monitorCount) {
			this.monitorCount = monitorCount;
		}

		public boolean isMonitorSum() {
			return monitorSum;
		}

		public void setMonitorSum(boolean monitorSum) {
			this.monitorSum = monitorSum;
		}
	}

}
