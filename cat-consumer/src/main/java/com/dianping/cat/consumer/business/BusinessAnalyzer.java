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
package com.dianping.cat.consumer.business;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.ContainerMessageAnalyzerFactory;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.business.ConfigItem;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Metric.Kind;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import jakarta.annotation.Resource;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(ContainerMessageAnalyzerFactory.ANALYZER_BEAN_PREFIX + BusinessAnalyzer.ID)
@Scope("prototype")
public class BusinessAnalyzer extends AbstractMessageAnalyzer<BusinessReport> {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BusinessAnalyzer.class);

	public static final String ID = "business";

	@Resource(name = BusinessAnalyzer.ID + "ReportManager")
	private ReportManager<BusinessReport> reportManager;

	@Resource(name = "businessConfigManager")
	private BusinessConfigManager businessConfigManager;

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public BusinessReport getReport(String domain) {
		long period = getStartTime();
		return reportManager.getHourlyReport(period, domain, false);
	}

	@Override
	public ReportManager<BusinessReport> getReportManager() {
		return reportManager;
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		return tree.getMetrics().size() > 0;
	}

	@Override
	protected void loadReports() {
		reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	ConfigItem parseValue(Metric metric) {
		ConfigItem config = new ConfigItem();
		Kind kind = metric.getKind();

		if (kind == Kind.COUNT) {
			config.setCount(metric.getCount());
			config.setValue(metric.getCount());
			config.setShowCount(true);
		} else if (kind == Kind.DURATION) {
			config.setCount(metric.getCount());
			config.setValue(metric.getDuration());
			config.setShowAvg(true);
		} else if (kind == Kind.SUM) {
			config.setCount(metric.getCount());
			config.setValue(metric.getSum());
			config.setShowSum(true);
		} else {
			return null;
		}

		return config;
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		BusinessReport report = reportManager.getHourlyReport(getStartTime(), domain, true);
		List<Metric> metrics = tree.getMetrics();

		for (Metric metric : metrics) {
			processMetric(report, metric, domain);
		}
	}

	private void processMetric(BusinessReport report, Metric metric, String domain) {
		ConfigItem config = parseValue(metric);

		if (config != null) {
			long current = metric.getTimestamp() / 1000 / 60;
			int min = (int) (current % 60);
			String name = metric.getName();
			BusinessItem businessItem = report.findOrCreateBusinessItem(name);
			Segment seg = businessItem.findOrCreateSegment(min);

			businessItem.setType(metric.getKind().name());

			seg.incCount(config.getCount());
			seg.incSum(config.getValue());
			seg.setAvg(seg.getSum() / seg.getCount());

			config.setTitle(name);

			boolean result = businessConfigManager.insertBusinessConfigIfNotExist(domain, name, config);

			if (!result) {
				LOGGER.error("error when insert business config info, domain {}, metricName {}", domain, name);
			}
		}
	}

	@Override
	@Resource(name = "serverConfigManager")
	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		super.setServerConfigManager(serverConfigManager);
	}
}
