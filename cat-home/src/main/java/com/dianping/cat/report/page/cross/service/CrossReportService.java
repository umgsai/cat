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
package com.dianping.cat.report.page.cross.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossReportMerger;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultNativeParser;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.mybatis.data.DailyReportContentDO;
import com.dianping.cat.mybatis.data.HourlyReportDO;
import com.dianping.cat.mybatis.data.HourlyReportContentDO;
import com.dianping.cat.mybatis.data.MonthReportDO;
import com.dianping.cat.mybatis.data.MonthlyReportContentDO;
import com.dianping.cat.mybatis.data.WeeklyReportDO;
import com.dianping.cat.mybatis.data.WeeklyReportContentDO;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;

@Component
public class CrossReportService extends AbstractReportService<CrossReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrossReportService.class);

	@Override
	public CrossReport makeReport(String domain, Date start, Date end) {
		CrossReport report = new CrossReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public CrossReport queryDailyReport(String domain, Date start, Date end) {
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = CrossAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReportDO report = dailyReportRepository
										.findByDomainNamePeriod(domain, name, new Date(startTime));
				CrossReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Cross daily report is missing, domain={}, period={}.", domain, new Date(startTime), e);
			} catch (Exception e) {
				LOGGER.error("Unable to query cross daily report, domain={}, period={}.", domain, new Date(startTime),
						e);
				Cat.logError(e);
			}
		}
		CrossReport crossReport = merger.getCrossReport();

		crossReport.setStartTime(start);
		crossReport.setEndTime(end);
		return crossReport;
	}

	private CrossReport queryFromDailyBinary(long id, String domain) {
		DailyReportContentDO content = dailyReportContentRepository.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrossReport(domain);
		}
	}

	private CrossReport queryFromHourlyBinary(long id, Date period, String domain) {
		HourlyReportContentDO content = hourlyReportContentRepository
								.findByPK(id, period);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrossReport(domain);
		}
	}

	private CrossReport queryFromMonthlyBinary(long id, String domain) {
		MonthlyReportContentDO content = monthlyReportContentRepository.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrossReport(domain);
		}
	}

	private CrossReport queryFromWeeklyBinary(long id, String domain) {
		WeeklyReportContentDO content = weeklyReportContentRepository.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrossReport(domain);
		}
	}

	@Override
	public CrossReport queryHourlyReport(String domain, Date start, Date end) {
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = CrossAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReportDO> reports = null;
			try {
				reports = hourlyReportRepository
										.findAllByDomainNamePeriod(new Date(startTime), domain, name);
			} catch (RuntimeException e) {
				LOGGER.error("Unable to query cross hourly report list, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReportDO report : reports) {
					try {
						CrossReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);

						reportModel.accept(merger);
					} catch (EmptyResultDataAccessException e) {
						LOGGER.warn("Cross hourly report content is missing, domain={}, reportId={}, period={}.", domain,
								report.getId(), report.getPeriod(), e);
					} catch (Exception e) {
						LOGGER.error("Unable to parse cross hourly report, domain={}, reportId={}, period={}.", domain,
								report.getId(), report.getPeriod(), e);
						Cat.logError(e);
					}
				}
			}
		}
		CrossReport crossReport = merger.getCrossReport();

		crossReport.setStartTime(start);
		crossReport.setEndTime(new Date(end.getTime() - 1));

		return crossReport;
	}

	@Override
	public CrossReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthReportDO entity = monthlyReportRepository
									.findReportByDomainNamePeriod(start, domain, CrossAnalyzer.ID);
			return queryFromMonthlyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Cross monthly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query cross monthly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return new CrossReport(domain);
	}

	@Override
	public CrossReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReportDO entity = weeklyReportRepository
									.findReportByDomainNamePeriod(start, domain, CrossAnalyzer.ID);

			return queryFromWeeklyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Cross weekly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query cross weekly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return new CrossReport(domain);
	}

}
