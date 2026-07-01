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
package com.dianping.cat.report.page.cross.task;

import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossReportMerger;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultNativeBuilder;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.mybatis.data.WeeklyReportDO;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask.CurrentWeeklyMonthlyTask;

@Component(CrossAnalyzer.ID)
public class CrossReportBuilder implements TaskBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrossReportBuilder.class);

	public static final String ID = CrossAnalyzer.ID;

	@Resource
	protected CrossReportService reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		LOGGER.info("Building cross daily report, name={}, domain={}, period={}.", name, domain, period);
		CrossReport crossReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReportDO report = new DailyReportDO();

		report.setCreateTime(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(crossReport);
		return reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Cross report don't support HourlyReport!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		LOGGER.info("Building cross monthly report, name={}, domain={}, period={}.", name, domain, period);
		CrossReport crossReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(crossReport);
		return reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		LOGGER.info("Building cross weekly report, name={}, domain={}, period={}.", name, domain, period);
		CrossReport crossReport = queryDailyReportsByDuration(domain, period,
								new Date(period.getTime()	+ TimeHelper.ONE_WEEK));
		WeeklyReportDO report = new WeeklyReportDO();

		report.setCreateTime(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(crossReport);
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

	private CrossReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				CrossReport reportModel = reportService
										.queryReport(domain, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				LOGGER.error("Unable to merge cross daily report into duration report, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
		}
		CrossReport crossReport = merger.getCrossReport();
		crossReport.setStartTime(start);
		crossReport.setEndTime(end);
		return crossReport;
	}

	private CrossReport queryHourlyReportsByDuration(String name, String domain, Date period, Date endDate) {
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			CrossReport reportModel = reportService.queryReport(domain, date, new Date(date.getTime()	+ TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		CrossReport crossReport = merger.getCrossReport();
		crossReport.setStartTime(period);
		crossReport.setEndTime(endDate);

		return crossReport;
	}

	public void setReportService(CrossReportService reportService) {
		this.reportService = reportService;
	}
}
