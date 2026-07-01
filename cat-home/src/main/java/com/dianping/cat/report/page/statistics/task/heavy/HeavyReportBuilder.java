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
package com.dianping.cat.report.page.statistics.task.heavy;

import jakarta.annotation.Resource;

import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.mybatis.data.HourlyReportDO;
import com.dianping.cat.mybatis.data.MonthReportDO;
import com.dianping.cat.mybatis.data.WeeklyReportDO;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

@Component(HeavyReportBuilder.ID)
public class HeavyReportBuilder implements TaskBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(HeavyReportBuilder.class);

	public static final String ID = Constants.REPORT_HEAVY;

	@Resource
	protected HeavyReportService reportService;

	@Resource
	protected MatrixReportService matrixReportService;

	@Resource
	private ServerFilterConfigManager serverFilterConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		LOGGER.info("Building heavy daily report, name={}, domain={}, period={}.", name, domain, period);

		HeavyReport heavyReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReportDO report = new DailyReportDO();

		report.setCreateTime(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(heavyReport);
		return reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		LOGGER.info("Building heavy hourly report, name={}, domain={}, period={}.", name, domain, start);

		HeavyReport heavyReport = new HeavyReport(Constants.CAT);
		MatrixReportVisitor visitor = new MatrixReportVisitor().setReport(heavyReport);
		Date end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
		Set<String> domains = reportService.queryAllDomainNames(start, end, MatrixAnalyzer.ID);

		heavyReport.setStartTime(start);
		heavyReport.setEndTime(end);
		for (String domainName : domains) {
			if (serverFilterConfigManager.validateDomain(domainName)) {
				MatrixReport matrixReport = matrixReportService.queryReport(domainName, start, end);

				visitor.visitMatrixReport(matrixReport);
			}
		}

		HourlyReportDO report = new HourlyReportDO();

		report.setCreateTime(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(heavyReport);
		return reportService.insertHourlyReport(report, binaryContent);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		LOGGER.info("Building heavy monthly report, name={}, domain={}, period={}.", name, domain, period);

		HeavyReport heavyReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthReportDO report = new MonthReportDO();

		report.setCreateTime(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(heavyReport);
		return reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		LOGGER.info("Building heavy weekly report, name={}, domain={}, period={}.", name, domain, period);

		HeavyReport heavyReport = queryDailyReportsByDuration(domain, period,
								new Date(period.getTime()	+ TimeHelper.ONE_WEEK));
		WeeklyReportDO report = new WeeklyReportDO();

		report.setCreateTime(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(heavyReport);
		return reportService.insertWeeklyReport(report, binaryContent);
	}

	private HeavyReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		HeavyReportMerger merger = new HeavyReportMerger(new HeavyReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				HeavyReport reportModel = reportService
										.queryReport(domain, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				LOGGER.error("Unable to merge heavy daily report into duration report, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
		}
		HeavyReport heavyReport = merger.getHeavyReport();
		heavyReport.setStartTime(start);
		heavyReport.setEndTime(end);
		return heavyReport;
	}

	private HeavyReport queryHourlyReportsByDuration(String name, String domain, Date period, Date endDate) {
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		HeavyReportMerger merger = new HeavyReportMerger(new HeavyReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			HeavyReport reportModel = reportService.queryReport(domain, date, new Date(date.getTime()	+ TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		com.dianping.cat.home.heavy.entity.HeavyReport heavyReport = merger.getHeavyReport();

		return heavyReport;
	}

	public void setConfigManager(ServerFilterConfigManager configManager) {
		this.serverFilterConfigManager = configManager;
	}

	public void setMatrixReportService(MatrixReportService matrixReportService) {
		this.matrixReportService = matrixReportService;
	}

	public void setReportService(HeavyReportService reportService) {
		this.reportService = reportService;
	}

}
