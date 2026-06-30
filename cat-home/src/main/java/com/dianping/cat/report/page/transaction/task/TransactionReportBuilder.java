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
package com.dianping.cat.report.page.transaction.task;

import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import java.util.Date;

import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportCountFilter;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask.CurrentWeeklyMonthlyTask;

@Component(TransactionAnalyzer.ID)
public class TransactionReportBuilder implements TaskBuilder {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(TransactionReportBuilder.class);

	public static final String ID = TransactionAnalyzer.ID;

	@Resource
	protected TransactionReportService reportService;

	@Resource
	protected ServerConfigManager serverConfigManager;

	@Resource
	private AtomicMessageConfigManager atomicMessageConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			Date end = TaskHelper.tomorrowZero(period);
			TransactionReport transactionReport = queryHourlyReportsByDuration(name, domain, period, end);

			DailyReportDO report = new DailyReportDO();

			report.setCreateTime(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(transactionReport);
			return reportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			SLF4J_LOGGER.error("Unable to build transaction daily report, name={}, domain={}, period={}.", name, domain,
					period, e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("transaction report don't support HourlyReport!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		SLF4J_LOGGER.info("Building transaction monthly report, name={}, domain={}, period={}.", name, domain, period);

		Date end = null;

		if (period.equals(TimeHelper.getCurrentMonth())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = TaskHelper.nextMonthStart(period);
		}
		TransactionReport transactionReport = queryDailyReportsByDuration(domain, period, end);
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(transactionReport);
		return reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		SLF4J_LOGGER.info("Building transaction weekly report, name={}, domain={}, period={}.", name, domain, period);

		Date end = null;

		if (period.equals(TimeHelper.getCurrentWeek())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = new Date(period.getTime() + TimeHelper.ONE_WEEK);
		}

		TransactionReport transactionReport = queryDailyReportsByDuration(domain, period, end);
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(transactionReport);
		return reportService.insertWeeklyReport(report, binaryContent);
	}

	@PostConstruct
	public void initialize() {
		CurrentWeeklyMonthlyReportTask.getInstance().register(new CurrentWeeklyMonthlyTask() {

			@Override
			public void buildCurrentMonthlyTask(String name, String domain, Date start) {
				buildMonthlyTask(name, domain, start);
			}

			@Override
			public void buildCurrentWeeklyTask(String name, String domain, Date start) {
				buildWeeklyTask(name, domain, start);
			}

			@Override
			public String getReportName() {
				return ID;
			}
		});
	}

	private TransactionReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		double duration = (end.getTime() - start.getTime()) * 1.0 / TimeHelper.ONE_DAY;

		HistoryTransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(domain))
								.setDuration(duration);
		TransactionReport transactionReport = merger.getTransactionReport();

		TransactionReportDailyGraphCreator creator = new TransactionReportDailyGraphCreator(transactionReport, (int) duration,
								start);

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				TransactionReport reportModel = reportService
										.queryReport(domain, new Date(startTime), new Date(startTime + TimeHelper.ONE_DAY));

				creator.createGraph(reportModel);
				reportModel.accept(merger);
			} catch (Exception e) {
				SLF4J_LOGGER.error("Unable to merge transaction daily report into duration report, domain={}, period={}.",
						domain, new Date(startTime), e);
				Cat.logError(e);
			}
		}

		transactionReport.setStartTime(start);
		transactionReport.setEndTime(end);

		new TransactionReportCountFilter(serverConfigManager.getMaxTypeThreshold(),
								atomicMessageConfigManager.getMaxNameThreshold(domain), serverConfigManager.getTypeNameLengthLimit())
								.visitTransactionReport(transactionReport);
		return transactionReport;
	}

	private TransactionReport queryHourlyReportsByDuration(String name, String domain, Date start, Date endDate) {
		long startTime = start.getTime();
		long endTime = endDate.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;

		HistoryTransactionReportMerger dailyMerger = new HistoryTransactionReportMerger(new TransactionReport(domain))
								.setDuration(duration);
		TransactionReportHourlyGraphCreator graphCreator = new TransactionReportHourlyGraphCreator(
								dailyMerger.getTransactionReport(), 10);

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			TransactionReport report = reportService
									.queryReport(domain, new Date(startTime), new Date(startTime + TimeHelper.ONE_HOUR));

			graphCreator.createGraph(report);
			report.accept(dailyMerger);
		}

		TransactionReport dailyreport = dailyMerger.getTransactionReport();
		Date date = dailyreport.getStartTime();
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);

		dailyreport.setStartTime(TaskHelper.todayZero(date));
		dailyreport.setEndTime(end);

		new TransactionReportCountFilter(serverConfigManager.getMaxTypeThreshold(),
								atomicMessageConfigManager.getMaxNameThreshold(domain), serverConfigManager.getTypeNameLengthLimit())
								.visitTransactionReport(dailyreport);

		return dailyreport;
	}

	public void setAtomicMessageConfigManager(AtomicMessageConfigManager atomicMessageConfigManager) {
		this.atomicMessageConfigManager = atomicMessageConfigManager;
	}

	public void setReportService(TransactionReportService reportService) {
		this.reportService = reportService;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		this.serverConfigManager = serverConfigManager;
	}

}
