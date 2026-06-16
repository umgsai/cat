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
package com.dianping.cat.report.page.statistics.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultNativeParser;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportMerger;
import com.dianping.cat.report.service.AbstractReportService;

public class UtilizationReportService extends AbstractReportService<UtilizationReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UtilizationReportService.class);

	@Override
	public UtilizationReport makeReport(String domain, Date start, Date end) {
		UtilizationReport report = new UtilizationReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public UtilizationReport queryDailyReport(String domain, Date start, Date end) {
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_UTILIZATION;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao
										.findByDomainNamePeriod(domain, name, new Date(startTime));
				UtilizationReport reportModel = queryFromDailyBinary(report.getId(), domain);
				reportModel.accept(merger);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Utilization daily report is missing, domain={}, period={}.", domain, new Date(startTime), e);
			} catch (Exception e) {
				LOGGER.error("Unable to query utilization daily report, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);
		return utilizationReport;
	}

	private UtilizationReport queryFromDailyBinary(long id, String domain) {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new UtilizationReport(domain);
		}
	}

	private UtilizationReport queryFromHourlyBinary(int id, Date period, String domain) {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new UtilizationReport(domain);
		}
	}

	private UtilizationReport queryFromMonthlyBinary(int id, String domain) {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new UtilizationReport(domain);
		}
	}

	private UtilizationReport queryFromWeeklyBinary(long id, String domain) {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new UtilizationReport(domain);
		}
	}

	@Override
	public UtilizationReport queryHourlyReport(String domain, Date start, Date end) {
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_UTILIZATION;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao
										.findAllByDomainNamePeriod(new Date(startTime), domain, name);
			} catch (RuntimeException e) {
				LOGGER.error("Unable to query utilization hourly report list, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						UtilizationReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);
						reportModel.accept(merger);
					} catch (EmptyResultDataAccessException e) {
						LOGGER.warn("Utilization hourly report content is missing, domain={}, reportId={}, period={}.",
								domain, report.getId(), report.getPeriod(), e);
					} catch (Exception e) {
						LOGGER.error("Unable to parse utilization hourly report, domain={}, reportId={}, period={}.",
								domain, report.getId(), report.getPeriod(), e);
						Cat.logError(e);
					}
				}
			}
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(new Date(end.getTime() - 1));

		return utilizationReport;
	}

	@Override
	public UtilizationReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao
									.findReportByDomainNamePeriod(start, domain,	Constants.REPORT_UTILIZATION);
			return queryFromMonthlyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Utilization monthly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query utilization monthly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return new UtilizationReport(domain);
	}

	@Override
	public UtilizationReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao
									.findReportByDomainNamePeriod(start, domain,	Constants.REPORT_UTILIZATION);
			return queryFromWeeklyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Utilization weekly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query utilization weekly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return new UtilizationReport(domain);
	}

}
