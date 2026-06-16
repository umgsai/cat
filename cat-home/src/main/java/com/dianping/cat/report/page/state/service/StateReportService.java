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
package com.dianping.cat.report.page.state.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateReportMerger;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;

public class StateReportService extends AbstractReportService<StateReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateReportService.class);

	@Override
	public StateReport makeReport(String domain, Date start, Date end) {
		StateReport report = new StateReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public StateReport queryDailyReport(String domain, Date start, Date end) {
		StateReportMerger merger = new StateReportMerger(new StateReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = StateAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao
										.findByDomainNamePeriod(domain, name, new Date(startTime));
				StateReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("State daily report is missing, domain={}, period={}.", domain, new Date(startTime), e);
			} catch (Exception e) {
				LOGGER.error("Unable to query state daily report, domain={}, period={}.", domain, new Date(startTime),
						e);
				Cat.logError(e);
			}
		}
		StateReport stateReport = merger.getStateReport();

		stateReport.setStartTime(start);
		stateReport.setEndTime(end);
		return stateReport;
	}

	private StateReport queryFromDailyBinary(long id, String domain) {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StateReport(domain);
		}
	}

	private StateReport queryFromHourlyBinary(int id, Date period, String domain) {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StateReport(domain);
		}
	}

	private StateReport queryFromMonthlyBinary(int id, String domain) {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StateReport(domain);
		}
	}

	private StateReport queryFromWeeklyBinary(int id, String domain) {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StateReport(domain);
		}
	}

	@Override
	public StateReport queryHourlyReport(String domain, Date start, Date end) {
		StateReportMerger merger = new StateReportMerger(new StateReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = StateAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao
										.findAllByDomainNamePeriod(new Date(startTime), domain, name);
			} catch (RuntimeException e) {
				LOGGER.error("Unable to query state hourly report list, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						StateReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);

						reportModel.accept(merger);
					} catch (EmptyResultDataAccessException e) {
						LOGGER.warn("State hourly report content is missing, domain={}, reportId={}, period={}.", domain,
								report.getId(), report.getPeriod(), e);
					} catch (Exception e) {
						LOGGER.error("Unable to parse state hourly report, domain={}, reportId={}, period={}.", domain,
								report.getId(), report.getPeriod(), e);
						Cat.logError(e);
					}
				}
			}
		}
		StateReport stateReport = merger.getStateReport();

		stateReport.setStartTime(start);
		stateReport.setEndTime(new Date(end.getTime() - 1));
		return stateReport;
	}

	@Override
	public StateReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao
									.findReportByDomainNamePeriod(start, domain, StateAnalyzer.ID);

			return queryFromMonthlyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("State monthly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query state monthly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return new StateReport(domain);
	}

	@Override
	public StateReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao
									.findReportByDomainNamePeriod(start, domain, StateAnalyzer.ID);

			return queryFromWeeklyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("State weekly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query state weekly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return new StateReport(domain);
	}

}
