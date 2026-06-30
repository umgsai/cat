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
package com.dianping.cat.report.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.mybatis.DailyReportContentRepository;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.mybatis.HourlyReportContentRepository;
import com.dianping.cat.mybatis.HourlyReportRepository;
import com.dianping.cat.mybatis.data.HourlyReportContentDO;
import com.dianping.cat.mybatis.data.HourlyReportDO;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.mybatis.MonthlyReportContentRepository;
import com.dianping.cat.mybatis.MonthlyReportRepository;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.mybatis.WeeklyReportContentRepository;
import com.dianping.cat.mybatis.WeeklyReportRepository;
import com.dianping.cat.mybatis.DailyReportRepository;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;

public abstract class AbstractReportService<T> implements ReportService<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReportService.class);

	public static final int s_hourly = 1;

	public static final int s_daily = 2;

	public static final int s_weekly = 3;

	public static final int s_monthly = 4;

	public static final int s_customer = 5;

	@Resource
	protected HourlyReportRepository hourlyReportRepository;

	@Resource
	protected HourlyReportContentRepository hourlyReportContentRepository;

	@Resource
	protected DailyReportRepository dailyReportRepository;

	@Resource
	protected DailyReportContentRepository dailyReportContentRepository;

	@Resource
	protected WeeklyReportRepository weeklyReportRepository;

	@Resource
	protected WeeklyReportContentRepository weeklyReportContentRepository;

	@Resource
	protected MonthlyReportRepository monthlyReportRepository;

	@Resource
	protected MonthlyReportContentRepository monthlyReportContentRepository;

	private Map<String, Set<String>> domainCache = new LinkedHashMap<String, Set<String>>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, Set<String>> eldest) {
			return size() > 1000;
		}
	};

	public int computeQueryType(Date start, Date end) {
		long duration = end.getTime() - start.getTime();

		if (duration == TimeHelper.ONE_HOUR) {
			return s_hourly;
		}
		if (duration == TimeHelper.ONE_DAY) {
			return s_daily;
		}
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(start);

		if (duration == TimeHelper.ONE_WEEK && startCal.get(Calendar.DAY_OF_WEEK) == 7) {
			return s_weekly;
		}
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(end);

		if (startCal.get(Calendar.DAY_OF_MONTH) == 1 && endCal.get(Calendar.DAY_OF_MONTH) == 1) {
			return s_monthly;
		}
		return s_customer;
	}

	@Override
	public boolean insertDailyReport(DailyReportDO report, byte[] content) {
		ensureReportRepositories();
		try {
			dailyReportRepository.insert(report);

			long id = report.getId();
			DailyReportContent proto = dailyReportContentRepository.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			dailyReportContentRepository.insert(proto);
			return true;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to insert daily report, domain={}, name={}, period={}.", report.getDomain(),
					report.getName(), report.getPeriod(), e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertHourlyReport(HourlyReportDO report, byte[] content) {
		ensureReportRepositories();
		try {
			hourlyReportRepository.insert(report);

			long id = report.getId();
			HourlyReportContentDO proto = hourlyReportContentRepository.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			proto.setPeriod(report.getPeriod());
			hourlyReportContentRepository.insert(proto);
			return true;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to insert hourly report, domain={}, name={}, period={}.", report.getDomain(),
					report.getName(), report.getPeriod(), e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertMonthlyReport(MonthlyReport report, byte[] content) {
		ensureReportRepositories();
		try {
			MonthlyReport monthReport = monthlyReportRepository
									.findReportByDomainNamePeriod(report.getPeriod(),	report.getDomain(), report.getName());

			if (monthReport != null) {
				MonthlyReportContent reportContent = monthlyReportContentRepository.createLocal();

				reportContent.setKeyReportId(monthReport.getId());
				reportContent.setReportId(monthReport.getId());
				monthlyReportRepository.deleteReportByDomainNamePeriod(report);
				monthlyReportContentRepository.deleteByPK(reportContent);
			}
		} catch (EmptyResultDataAccessException e) {
		} catch (Exception e) {
			LOGGER.error("Unable to clear existing monthly report, domain={}, name={}, period={}.", report.getDomain(),
					report.getName(), report.getPeriod(), e);
			Cat.logError(e);
		}

		try {
			monthlyReportRepository.insert(report);

			long id = report.getId();
			MonthlyReportContent proto = monthlyReportContentRepository.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			monthlyReportContentRepository.insert(proto);

			return true;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to insert monthly report, domain={}, name={}, period={}.", report.getDomain(),
					report.getName(), report.getPeriod(), e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertWeeklyReport(WeeklyReport report, byte[] content) {
		ensureReportRepositories();
		try {
			WeeklyReport weeklyReport = weeklyReportRepository
									.findReportByDomainNamePeriod(report.getPeriod(),	report.getDomain(), report.getName());

			if (weeklyReport != null) {
				WeeklyReportContent reportContent = weeklyReportContentRepository.createLocal();

				reportContent.setKeyReportId(weeklyReport.getId());
				reportContent.setReportId(weeklyReport.getId());
				weeklyReportContentRepository.deleteByPK(reportContent);
				weeklyReportRepository.deleteReportByDomainNamePeriod(report);
			}
		} catch (EmptyResultDataAccessException e) {
		} catch (Exception e) {
			LOGGER.error("Unable to clear existing weekly report, domain={}, name={}, period={}.", report.getDomain(),
					report.getName(), report.getPeriod(), e);
			Cat.logError(e);
		}

		try {
			weeklyReportRepository.insert(report);

			long id = report.getId();
			WeeklyReportContent proto = weeklyReportContentRepository.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			weeklyReportContentRepository.insert(proto);
			return true;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to insert weekly report, domain={}, name={}, period={}.", report.getDomain(),
					report.getName(), report.getPeriod(), e);
			Cat.logError(e);
			return false;
		}
	}

	public abstract T makeReport(String domain, Date start, Date end);

	public Set<String> queryAllDomainNames(Date start, Date end, String name) {
		ensureReportRepositories();
		Set<String> domains = new HashSet<String>();
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			domains.addAll(queryAllDomains(new Date(startTime), name));
		}
		return domains;
	}

	private Set<String> queryAllDomains(Date date, String name) {
		String key = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date) + ":" + name;
		Set<String> domains = domainCache.get(key);

		if (domains == null) {
			domains = new HashSet<String>();
			try {
					List<HourlyReportDO> reports = hourlyReportRepository
											.findAllByPeriodName(date, name);

					if (reports != null) {
						for (HourlyReportDO report : reports) {
							domains.add(report.getDomain());
						}
					}
				Cat.logEvent("FindDomain", key, Event.SUCCESS, domains.toString());
				domainCache.put(key, domains);
			} catch (RuntimeException e) {
				LOGGER.error("Unable to query report domains, date={}, name={}.", date, name, e);
				Cat.logError(e);
			}
		}
		return domains;
	}

	@Override
	public abstract T queryDailyReport(String domain, Date start, Date end);

	@Override
	public abstract T queryHourlyReport(String domain, Date start, Date end);

	@Override
	public abstract T queryMonthlyReport(String domain, Date start);

	public T queryReport(String domain, Date start, Date end) {
		ensureReportRepositories();
		int type = computeQueryType(start, end);
		T report = null;

		if (type == s_hourly) {
			report = queryHourlyReport(domain, start, end);
		} else if (type == s_daily) {
			report = queryDailyReport(domain, start, end);
		} else if (type == s_weekly) {
			report = queryWeeklyReport(domain, start);
		} else if (type == s_monthly) {
			report = queryMonthlyReport(domain, start);
		} else {
			report = queryDailyReport(domain, start, end);
		}
		if (report == null) {
			report = makeReport(domain, start, end);
		}
		return report;
	}

	@Override
	public abstract T queryWeeklyReport(String domain, Date start);

	protected void ensureReportRepositories() {
		if (hourlyReportRepository == null || hourlyReportContentRepository == null || dailyReportRepository == null
								|| dailyReportContentRepository == null || weeklyReportRepository == null
								|| weeklyReportContentRepository == null || monthlyReportRepository == null
								|| monthlyReportContentRepository == null) {
			throw new IllegalStateException("Report repositories are required for " + getClass().getSimpleName() + ".");
		}
	}

	public void setDailyReportContentDao(DailyReportContentRepository dailyReportContentDao) {
		dailyReportContentRepository = dailyReportContentDao;
	}

	public void setDailyReportDao(DailyReportRepository dailyReportDao) {
		dailyReportRepository = dailyReportDao;
	}

	public void setHourlyReportContentDao(HourlyReportContentRepository hourlyReportContentDao) {
		hourlyReportContentRepository = hourlyReportContentDao;
	}

	public void setHourlyReportDao(HourlyReportRepository hourlyReportDao) {
		hourlyReportRepository = hourlyReportDao;
	}

	public void setMonthlyReportContentDao(MonthlyReportContentRepository monthlyReportContentDao) {
		monthlyReportContentRepository = monthlyReportContentDao;
	}

	public void setMonthlyReportDao(MonthlyReportRepository monthlyReportDao) {
		monthlyReportRepository = monthlyReportDao;
	}

	public void setWeeklyReportContentDao(WeeklyReportContentRepository weeklyReportContentDao) {
		weeklyReportContentRepository = weeklyReportContentDao;
	}

	public void setWeeklyReportDao(WeeklyReportRepository weeklyReportDao) {
		weeklyReportRepository = weeklyReportDao;
	}

}
