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

import org.springframework.dao.EmptyResultDataAccessException;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.mybatis.repository.daily.report.content.DailyReportContentRepository;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.mybatis.repository.hourly.report.content.HourlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourlyreport.HourlyReportRepository;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.mybatis.repository.monthly.report.content.MonthlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.monthreport.MonthlyReportRepository;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.core.mybatis.repository.weekly.report.content.WeeklyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.weeklyreport.WeeklyReportRepository;
import com.dianping.cat.core.report.daily.repository.DailyReportRepository;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;

public abstract class AbstractReportService<T> implements ReportService<T> {

	public static final int s_hourly = 1;

	public static final int s_daily = 2;

	public static final int s_weekly = 3;

	public static final int s_monthly = 4;

	public static final int s_customer = 5;

	protected HourlyReportRepository m_hourlyReportDao;

	protected HourlyReportContentRepository m_hourlyReportContentDao;

	protected DailyReportRepository m_dailyReportDao;

	protected DailyReportContentRepository m_dailyReportContentDao;

	protected WeeklyReportRepository m_weeklyReportDao;

	protected WeeklyReportContentRepository m_weeklyReportContentDao;

	protected MonthlyReportRepository m_monthlyReportDao;

	protected MonthlyReportContentRepository m_monthlyReportContentDao;

	private Map<String, Set<String>> m_domains = new LinkedHashMap<String, Set<String>>() {

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
	public boolean insertDailyReport(DailyReport report, byte[] content) {
		ensureReportRepositories();
		try {
			m_dailyReportDao.insert(report);

			long id = report.getId();
			DailyReportContent proto = m_dailyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			m_dailyReportContentDao.insert(proto);
			return true;
		} catch (RuntimeException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertHourlyReport(HourlyReport report, byte[] content) {
		ensureReportRepositories();
		try {
			m_hourlyReportDao.insert(report);

			int id = report.getId();
			HourlyReportContent proto = m_hourlyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			proto.setPeriod(report.getPeriod());
			m_hourlyReportContentDao.insert(proto);
			return true;
		} catch (RuntimeException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertMonthlyReport(MonthlyReport report, byte[] content) {
		ensureReportRepositories();
		try {
			MonthlyReport monthReport = m_monthlyReportDao
									.findReportByDomainNamePeriod(report.getPeriod(),	report.getDomain(), report.getName());

			if (monthReport != null) {
				MonthlyReportContent reportContent = m_monthlyReportContentDao.createLocal();

				reportContent.setKeyReportId(monthReport.getId());
				reportContent.setReportId(monthReport.getId());
				m_monthlyReportDao.deleteReportByDomainNamePeriod(report);
				m_monthlyReportContentDao.deleteByPK(reportContent);
			}
		} catch (EmptyResultDataAccessException e) {
		} catch (Exception e) {
			Cat.logError(e);
		}

		try {
			m_monthlyReportDao.insert(report);

			long id = report.getId();
			MonthlyReportContent proto = m_monthlyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			m_monthlyReportContentDao.insert(proto);

			return true;
		} catch (RuntimeException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertWeeklyReport(WeeklyReport report, byte[] content) {
		ensureReportRepositories();
		try {
			WeeklyReport weeklyReport = m_weeklyReportDao
									.findReportByDomainNamePeriod(report.getPeriod(),	report.getDomain(), report.getName());

			if (weeklyReport != null) {
				WeeklyReportContent reportContent = m_weeklyReportContentDao.createLocal();

				reportContent.setKeyReportId(weeklyReport.getId());
				reportContent.setReportId(weeklyReport.getId());
				m_weeklyReportContentDao.deleteByPK(reportContent);
				m_weeklyReportDao.deleteReportByDomainNamePeriod(report);
			}
		} catch (EmptyResultDataAccessException e) {
		} catch (Exception e) {
			Cat.logError(e);
		}

		try {
			m_weeklyReportDao.insert(report);

			long id = report.getId();
			WeeklyReportContent proto = m_weeklyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			m_weeklyReportContentDao.insert(proto);
			return true;
		} catch (RuntimeException e) {
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
		Set<String> domains = m_domains.get(key);

		if (domains == null) {
			domains = new HashSet<String>();
			try {
				List<HourlyReport> reports = m_hourlyReportDao
										.findAllByPeriodName(date, name);

				if (reports != null) {
					for (HourlyReport report : reports) {
						domains.add(report.getDomain());
					}
				}
				Cat.logEvent("FindDomain", key, Event.SUCCESS, domains.toString());
				m_domains.put(key, domains);
			} catch (RuntimeException e) {
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
		if (m_hourlyReportDao == null || m_hourlyReportContentDao == null || m_dailyReportDao == null
								|| m_dailyReportContentDao == null || m_weeklyReportDao == null
								|| m_weeklyReportContentDao == null || m_monthlyReportDao == null
								|| m_monthlyReportContentDao == null) {
			throw new IllegalStateException("Report repositories are required for " + getClass().getSimpleName() + ".");
		}
	}

	public void setDailyReportContentDao(DailyReportContentRepository dailyReportContentDao) {
		m_dailyReportContentDao = dailyReportContentDao;
	}

	public void setDailyReportDao(DailyReportRepository dailyReportDao) {
		m_dailyReportDao = dailyReportDao;
	}

	public void setHourlyReportContentDao(HourlyReportContentRepository hourlyReportContentDao) {
		m_hourlyReportContentDao = hourlyReportContentDao;
	}

	public void setHourlyReportDao(HourlyReportRepository hourlyReportDao) {
		m_hourlyReportDao = hourlyReportDao;
	}

	public void setMonthlyReportContentDao(MonthlyReportContentRepository monthlyReportContentDao) {
		m_monthlyReportContentDao = monthlyReportContentDao;
	}

	public void setMonthlyReportDao(MonthlyReportRepository monthlyReportDao) {
		m_monthlyReportDao = monthlyReportDao;
	}

	public void setWeeklyReportContentDao(WeeklyReportContentRepository weeklyReportContentDao) {
		m_weeklyReportContentDao = weeklyReportContentDao;
	}

	public void setWeeklyReportDao(WeeklyReportRepository weeklyReportDao) {
		m_weeklyReportDao = weeklyReportDao;
	}

}
