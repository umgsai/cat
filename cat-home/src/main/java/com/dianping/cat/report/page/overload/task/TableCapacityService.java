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
package com.dianping.cat.report.page.overload.task;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.mybatis.HourlyReportRepository;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.mybatis.MonthlyReportRepository;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.mybatis.WeeklyReportRepository;
import com.dianping.cat.mybatis.DailyReportRepository;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.mybatis.OverloadRepository;

@Component
public class TableCapacityService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TableCapacityService.class);

	@Resource
	private OverloadRepository overloadRepository;

	@Resource
	private HourlyReportRepository hourlyReportRepository;

	@Resource
	private DailyReportRepository dailyReportRepository;

	@Resource
	private WeeklyReportRepository weeklyReportRepository;

	@Resource
	private MonthlyReportRepository monthlyReportRepository;

	private OverloadReport generateOverloadReport(Object object, double reportSize, int reportType) {
		OverloadReport overloadReport = new OverloadReport();

		switch (reportType) {
		case CapacityUpdater.HOURLY_TYPE:
			overloadReport.setDomain(((HourlyReport) object).getDomain());
			overloadReport.setIp(((HourlyReport) object).getIp());
			overloadReport.setName(((HourlyReport) object).getName());
			overloadReport.setPeriod(((HourlyReport) object).getPeriod());
			overloadReport.setType(((HourlyReport) object).getType());
			break;
		case CapacityUpdater.DAILY_TYPE:
			overloadReport.setDomain(((DailyReport) object).getDomain());
			overloadReport.setIp(((DailyReport) object).getIp());
			overloadReport.setName(((DailyReport) object).getName());
			overloadReport.setPeriod(((DailyReport) object).getPeriod());
			overloadReport.setType(((DailyReport) object).getType());
			break;
		case CapacityUpdater.WEEKLY_TYPE:
			overloadReport.setDomain(((WeeklyReport) object).getDomain());
			overloadReport.setIp(((WeeklyReport) object).getIp());
			overloadReport.setName(((WeeklyReport) object).getName());
			overloadReport.setPeriod(((WeeklyReport) object).getPeriod());
			overloadReport.setType(((WeeklyReport) object).getType());
			break;
		case CapacityUpdater.MONTHLY_TYPE:
			overloadReport.setDomain(((MonthlyReport) object).getDomain());
			overloadReport.setIp(((MonthlyReport) object).getIp());
			overloadReport.setName(((MonthlyReport) object).getName());
			overloadReport.setPeriod(((MonthlyReport) object).getPeriod());
			overloadReport.setType(((MonthlyReport) object).getType());
			break;
		}
		overloadReport.setReportType(reportType);
		overloadReport.setReportLength(reportSize);

		return overloadReport;
	}

	public List<OverloadReport> queryOverloadReports(Date startTime, Date endTime) {
		List<OverloadReport> reports = new ArrayList<OverloadReport>();

		try {
			List<Overload> overloads = overloadRepository
									.findIdAndSizeByDuration(startTime, endTime);

			for (Overload overload : overloads) {
				try {
					long reportId = overload.getReportId();
					int reportType = overload.getReportType();
					double reportSize = overload.getReportSize();
					Object report = null;

					switch (reportType) {
					case CapacityUpdater.HOURLY_TYPE:
						report = hourlyReportRepository.findByPK(reportId);
						break;
					case CapacityUpdater.DAILY_TYPE:
						report = dailyReportRepository.findByPK(reportId);
						break;
					case CapacityUpdater.WEEKLY_TYPE:
						report = weeklyReportRepository.findByPK(reportId);
						break;
					case CapacityUpdater.MONTHLY_TYPE:
						report = monthlyReportRepository.findByPK(reportId);
						break;
					}
					reports.add(generateOverloadReport(report, reportSize, reportType));
				} catch (EmptyResultDataAccessException e) {
					LOGGER.warn("Overload report target record not found, overloadId={}, reportId={}, reportType={}.",
					      overload.getId(), overload.getReportId(), overload.getReportType());
				} catch (Exception ex) {
					LOGGER.error("Unable to build overload report item, overloadId={}, reportId={}, reportType={}.",
					      overload.getId(), overload.getReportId(), overload.getReportType(), ex);
					Cat.logError(ex);
				}
			}
		} catch (RuntimeException e) {
			LOGGER.error("Unable to query overload reports, startTime={}, endTime={}.", startTime, endTime, e);
			Cat.logError(e);
		}

		return reports;
	}

	public void setDailyReportDao(DailyReportRepository dailyReportDao) {
		this.dailyReportRepository = dailyReportDao;
	}

	public void setHourlyReportDao(HourlyReportRepository hourlyReportDao) {
		this.hourlyReportRepository = hourlyReportDao;
	}

	public void setMonthlyReportDao(MonthlyReportRepository monthlyReportDao) {
		this.monthlyReportRepository = monthlyReportDao;
	}

	public void setOverloadDao(OverloadRepository overloadDao) {
		this.overloadRepository = overloadDao;
	}

	public void setWeeklyReportDao(WeeklyReportRepository weeklyReportDao) {
		this.weeklyReportRepository = weeklyReportDao;
	}

}
