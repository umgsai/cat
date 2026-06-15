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
package com.dianping.cat.report.page.heartbeat.task;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

public class HeartbeatReportBuilder implements TaskBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatReportBuilder.class);

	public static final String ID = HeartbeatAnalyzer.ID;

	protected HeartbeatReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			Date end = TaskHelper.tomorrowZero(period);
			HeartbeatReport heartbeatReport = queryDailyHeartbeatReport(name, domain, period, end);
			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(heartbeatReport);

			return m_reportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			LOGGER.error("Unable to build heartbeat daily report, name={}, domain={}, period={}.", name, domain, period,
					e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no hourly report builder for heartbeat!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no month report builder for heartbeat!");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no weekly report builder for heartbeat!");
	}

	private HeartbeatReport queryDailyHeartbeatReport(String name, String domain, Date start, Date end) {
		HeartbeatDailyMerger merger = new HeartbeatDailyMerger(new HeartbeatReport(domain), start.getTime());
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime += TimeHelper.ONE_HOUR) {
			LOGGER.info("Merging heartbeat hourly report into daily report, name={}, domain={}, period={}.", name,
					domain, new Date(startTime));
			HeartbeatReport report = m_reportService
									.queryReport(domain, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_HOUR));

			report.accept(merger);
		}

		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();

		heartbeatReport.setStartTime(start);
		heartbeatReport.setEndTime(new Date(end.getTime() - 1));

		return heartbeatReport;
	}

	public void setReportService(HeartbeatReportService reportService) {
		m_reportService = reportService;
	}
}
