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
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.rule.DataChecker;
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
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.report.alert.transaction.TransactionContactor;
import com.dianping.cat.report.alert.transaction.TransactionDecorator;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.page.transaction.service.CompositeTransactionService;
import com.dianping.cat.report.page.transaction.service.HistoricalTransactionService;
import com.dianping.cat.report.page.transaction.service.LocalTransactionService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class TransactionComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(TransactionMergeHelper.class));
		all.add(reportService(TransactionReportService.class));
		all.add(ruleConfigManager(TransactionRuleConfigManager.class));

		all.add(C(Contactor.class, TransactionContactor.ID, TransactionContactor.class)
								.req(ProjectService.class,	AlertConfigManager.class));
		all.add(C(Decorator.class, TransactionDecorator.ID, TransactionDecorator.class));
		all.add(C(TransactionAlert.class) //
								.req(TransactionRuleConfigManager.class, (String) null, "m_ruleConfigManager") //
								.req(DataChecker.class, (String) null, "m_dataChecker") //
								.req(AlertManager.class, (String) null, "m_sendManager") //
								.req(ModelService.class, TransactionAnalyzer.ID, "m_service") //
								.req(TransactionMergeHelper.class, (String) null, "m_mergeHelper"));

		all.add(C(LocalModelService.class, LocalTransactionService.ID, LocalTransactionService.class) //
								.req(ReportBucketManager.class, (String) null, "m_bucketManager") //
								.req(ServerConfigManager.class, (String) null, "m_configManager") //
								.req(MessageConsumer.class, (String) null, "m_consumer"));
		all.add(C(ModelService.class, "transaction-historical", HistoricalTransactionService.class) //
								.req(TransactionReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, TransactionAnalyzer.ID, CompositeTransactionService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "transaction-historical" }, "m_services"));

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
