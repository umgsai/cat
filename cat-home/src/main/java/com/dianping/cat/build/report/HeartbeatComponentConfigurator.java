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
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.mybatis.repository.daily.report.content.DailyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourly.report.content.HourlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourlyreport.HourlyReportRepository;
import com.dianping.cat.core.mybatis.repository.monthly.report.content.MonthlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.monthreport.MonthlyReportRepository;
import com.dianping.cat.core.mybatis.repository.weekly.report.content.WeeklyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.weeklyreport.WeeklyReportRepository;
import com.dianping.cat.core.report.daily.repository.DailyReportRepository;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatContactor;
import com.dianping.cat.report.alert.heartbeat.HeartbeatDecorator;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager;
import com.dianping.cat.report.page.heartbeat.service.CompositeHeartbeatService;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.heartbeat.service.HistoricalHeartbeatService;
import com.dianping.cat.report.page.heartbeat.service.LocalHeartbeatService;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class HeartbeatComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(ruleConfigManager(HeartbeatRuleConfigManager.class));

		all.add(reportService(HeartbeatReportService.class));

		all.add(C(LocalModelService.class, LocalHeartbeatService.ID, LocalHeartbeatService.class) //
								.req(ReportBucketManager.class, (String) null, "m_bucketManager") //
								.req(ServerConfigManager.class, (String) null, "m_configManager") //
								.req(MessageConsumer.class, (String) null, "m_consumer"));
		all.add(C(ModelService.class, "heartbeat-historical", HistoricalHeartbeatService.class) //
								.req(HeartbeatReportService.class, (String) null, "m_reportService") //
								.req(ServerConfigManager.class, (String) null, "m_configManager"));
		all.add(C(ModelService.class, HeartbeatAnalyzer.ID, CompositeHeartbeatService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "heartbeat-historical" }, "m_services"));

		all.add(C(Contactor.class, HeartbeatContactor.ID, HeartbeatContactor.class)
								.req(ProjectService.class,	AlertConfigManager.class));
		all.add(C(Decorator.class, HeartbeatDecorator.ID, HeartbeatDecorator.class));

		all.add(A(HeartbeatAlert.class));

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

	private Component ruleConfigManager(Class<?> implementation) {
		return C(implementation) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(ContentFetcher.class, (String) null, "m_fetcher") //
								.req(UserDefinedRuleManager.class, (String) null, "m_manager") //
								.req(BaseRuleHelper.class, (String) null, "m_helper");
	}
}
