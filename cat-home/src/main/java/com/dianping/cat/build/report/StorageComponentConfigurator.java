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
package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.mybatis.repository.daily.report.content.DailyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourly.report.content.HourlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourlyreport.HourlyReportRepository;
import com.dianping.cat.core.mybatis.repository.monthly.report.content.MonthlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.monthreport.MonthlyReportRepository;
import com.dianping.cat.core.mybatis.repository.weekly.report.content.WeeklyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.weeklyreport.WeeklyReportRepository;
import com.dianping.cat.core.report.daily.repository.DailyReportRepository;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder;
import com.dianping.cat.report.page.storage.service.CompositeStorageService;
import com.dianping.cat.report.page.storage.service.HistoricalStorageService;
import com.dianping.cat.report.page.storage.service.LocalStorageService;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;

public class StorageComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(StorageGroupConfigManager.class));

		all.add(C(LocalModelService.class, LocalStorageService.ID, LocalStorageService.class) //
								.req(ReportBucketManager.class, (String) null, "m_bucketManager") //
								.req(ServerConfigManager.class, (String) null, "m_configManager") //
								.req(MessageConsumer.class, (String) null, "m_consumer"));
		all.add(C(ModelService.class, "storage-historical", HistoricalStorageService.class) //
								.req(StorageReportService.class, (String) null, "m_reportService") //
								.req(ServerConfigManager.class, (String) null, "m_configManager"));
		all.add(C(ModelService.class, StorageAnalyzer.ID, CompositeStorageService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "storage-historical" }, "m_services"));

		all.add(reportService(StorageReportService.class));

		all.add(C(StorageAlertInfoBuilder.class) //
								.req(AlertService.class, (String) null, "m_alertService"));

		return all;
	}

	private Component reportService(Class<?> implementation) {
		return C(implementation) //
								.req(HourlyReportRepository.class, (String) null, "m_hourlyReportDao") //
								.req(HourlyReportContentRepository.class, (String) null, "m_hourlyReportContentDao") //
								.req(DailyReportRepository.class, (String) null, "m_dailyReportDao") //
								.req(DailyReportContentRepository.class, (String) null, "m_dailyReportContentDao") //
								.req(WeeklyReportRepository.class, (String) null, "m_weeklyReportDao") //
								.req(WeeklyReportContentRepository.class, (String) null, "m_weeklyReportContentDao") //
								.req(MonthlyReportRepository.class, (String) null, "m_monthlyReportDao") //
								.req(MonthlyReportContentRepository.class, (String) null, "m_monthlyReportContentDao");
	}
}
