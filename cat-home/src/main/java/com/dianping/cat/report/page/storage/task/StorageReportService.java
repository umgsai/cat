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
package com.dianping.cat.report.page.storage.task;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageReportMerger;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultNativeParser;
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

public class StorageReportService extends AbstractReportService<StorageReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageReportService.class);

	@Override
	public StorageReport makeReport(String id, Date start, Date end) {
		StorageReport report = new StorageReport(id);
		int index = id.lastIndexOf("-");
		String name = id.substring(0, index);
		String type = id.substring(index + 1);

		report.setName(name).setType(type);
		report.setStartTime(start).setEndTime(end);
		return report;
	}

	public Set<String> queryAllIds(Date start, Date end) {
		Set<String> ids = new HashSet<String>();

		for (String id : queryAllDomainNames(start, end, StorageAnalyzer.ID)) {
			ids.add(id);
		}
		return ids;
	}

	private Set<String> queryAllIds(Date start, Date end, String name, String reportId) {
		Set<String> ids = new HashSet<String>();
		String type = reportId.substring(reportId.lastIndexOf("-"));

		for (String myId : queryAllDomainNames(start, end, name)) {
			if (myId.endsWith(type)) {
				String prefix = myId.substring(0, myId.lastIndexOf("-"));

				ids.add(prefix);
			}
		}
		return ids;
	}

	@Override
	public StorageReport queryDailyReport(String id, Date start, Date end) {
		StorageReportMerger merger = new StorageReportMerger(new StorageReport(id));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = StorageAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao
										.findByDomainNamePeriod(id, name, new Date(startTime));
				StorageReport reportModel = queryFromDailyBinary(report.getId(), id);

				reportModel.accept(merger);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Storage daily report is missing, reportId={}, period={}.", id, new Date(startTime), e);
			} catch (Exception e) {
				LOGGER.error("Unable to query storage daily report, reportId={}, period={}.", id, new Date(startTime), e);
				Cat.logError(e);
			}
		}
		StorageReport storageReport = merger.getStorageReport();

		storageReport.setStartTime(start);
		storageReport.setEndTime(end);
		return storageReport;
	}

	private StorageReport queryFromDailyBinary(long id, String domain) {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StorageReport(domain);
		}
	}

	private StorageReport queryFromHourlyBinary(long id, Date period, String reportId) {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StorageReport(reportId);
		}
	}

	private StorageReport queryFromMonthlyBinary(long id, String reportId) {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StorageReport(reportId);
		}
	}

	private StorageReport queryFromWeeklyBinary(long id, String reportId) {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StorageReport(reportId);
		}
	}

	@Override
	public StorageReport queryHourlyReport(String reportId, Date start, Date end) {
		StorageReportMerger merger = new StorageReportMerger(new StorageReport(reportId));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = StorageAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao
										.findAllByDomainNamePeriod(new Date(startTime), reportId, name);
			} catch (RuntimeException e) {
				LOGGER.error("Unable to query storage hourly report list, reportId={}, period={}.", reportId,
						new Date(startTime), e);
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						StorageReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), reportId);
						reportModel.accept(merger);
					} catch (EmptyResultDataAccessException e) {
						LOGGER.warn("Storage hourly report content is missing, reportId={}, reportDbId={}, period={}.",
								reportId, report.getId(), report.getPeriod(), e);
					} catch (Exception e) {
						LOGGER.error("Unable to parse storage hourly report, reportId={}, reportDbId={}, period={}.",
								reportId, report.getId(), report.getPeriod(), e);
						Cat.logError(e);
					}
				}
			}
		}
		StorageReport storageReport = merger.getStorageReport();

		storageReport.setStartTime(start);
		storageReport.setEndTime(new Date(end.getTime() - 1));
		Set<String> ids = queryAllIds(start, end, name, reportId);

		storageReport.getIds().addAll(ids);
		return storageReport;
	}

	@Override
	public StorageReport queryMonthlyReport(String reportId, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao
									.findReportByDomainNamePeriod(start, reportId, StorageAnalyzer.ID);

			return queryFromMonthlyBinary(entity.getId(), reportId);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Storage monthly report is missing, reportId={}, period={}.", reportId, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query storage monthly report, reportId={}, period={}.", reportId, start, e);
			Cat.logError(e);
		}
		return new StorageReport(reportId);
	}

	@Override
	public StorageReport queryWeeklyReport(String reportId, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao
									.findReportByDomainNamePeriod(start, reportId, StorageAnalyzer.ID);

			return queryFromWeeklyBinary(entity.getId(), reportId);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Storage weekly report is missing, reportId={}, period={}.", reportId, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query storage weekly report, reportId={}, period={}.", reportId, start, e);
			Cat.logError(e);
		}
		return new StorageReport(reportId);
	}

}
