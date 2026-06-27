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
import org.springframework.stereotype.Component;

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
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.service.transform.DefaultNativeParser;
import com.dianping.cat.report.page.statistics.task.service.ServiceReportMerger;
import com.dianping.cat.report.service.AbstractReportService;

@Component
public class ServiceReportService extends AbstractReportService<ServiceReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceReportService.class);

	@Override
	public ServiceReport makeReport(String domain, Date start, Date end) {
		ServiceReport report = new ServiceReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public ServiceReport queryDailyReport(String domain, Date start, Date end) {
		ServiceReportMerger merger = new ServiceReportMerger(new ServiceReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_SERVICE;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = dailyReportRepository
										.findByDomainNamePeriod(domain, name, new Date(startTime));
				ServiceReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Service daily report is missing, domain={}, period={}.", domain, new Date(startTime), e);
			} catch (Exception e) {
				LOGGER.error("Unable to query service daily report, domain={}, period={}.", domain, new Date(startTime),
						e);
				Cat.logError(e);
			}
		}
		ServiceReport serviceReport = merger.getServiceReport();

		serviceReport.setStartTime(start);
		serviceReport.setEndTime(end);
		return serviceReport;
	}

	private ServiceReport queryFromDailyBinary(long id, String domain) {
		DailyReportContent content = dailyReportContentRepository.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new ServiceReport(domain);
		}
	}

	private ServiceReport queryFromHourlyBinary(long id, Date period, String domain) {
		HourlyReportContent content = hourlyReportContentRepository
								.findByPK(id, period);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new ServiceReport(domain);
		}
	}

	private ServiceReport queryFromMonthlyBinary(long id, String domain) {
		MonthlyReportContent content = monthlyReportContentRepository.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new ServiceReport(domain);
		}
	}

	private ServiceReport queryFromWeeklyBinary(long id, String domain) {
		WeeklyReportContent content = weeklyReportContentRepository.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new ServiceReport(domain);
		}
	}

	@Override
	public ServiceReport queryHourlyReport(String domain, Date start, Date end) {
		ServiceReportMerger merger = new ServiceReportMerger(new ServiceReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_SERVICE;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = hourlyReportRepository
										.findAllByDomainNamePeriod(new Date(startTime), domain, name);
			} catch (RuntimeException e) {
				LOGGER.error("Unable to query service hourly report list, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						ServiceReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);
						reportModel.accept(merger);
					} catch (EmptyResultDataAccessException e) {
						LOGGER.warn("Service hourly report content is missing, domain={}, reportId={}, period={}.", domain,
								report.getId(), report.getPeriod(), e);
					} catch (Exception e) {
						LOGGER.error("Unable to parse service hourly report, domain={}, reportId={}, period={}.", domain,
								report.getId(), report.getPeriod(), e);
						Cat.logError(e);
					}
				}
			}
		}
		ServiceReport serviceReport = merger.getServiceReport();

		serviceReport.setStartTime(start);
		serviceReport.setEndTime(new Date(end.getTime() - 1));

		return serviceReport;
	}

	@Override
	public ServiceReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = monthlyReportRepository
									.findReportByDomainNamePeriod(start, domain,	Constants.REPORT_SERVICE);
			return queryFromMonthlyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Service monthly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query service monthly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return new ServiceReport(domain);
	}

	@Override
	public ServiceReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = weeklyReportRepository
									.findReportByDomainNamePeriod(start, domain, Constants.REPORT_SERVICE);

			return queryFromWeeklyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Service weekly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query service weekly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return new ServiceReport(domain);
	}

}
