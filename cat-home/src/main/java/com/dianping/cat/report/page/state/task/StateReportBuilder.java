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
package com.dianping.cat.report.page.state.task;

import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.consumer.state.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask.CurrentWeeklyMonthlyTask;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.spring.CatSpringContext;

public class StateReportBuilder implements TaskBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateReportBuilder.class);

	public static final String ID = StateAnalyzer.ID;

	protected StateReportService m_reportService;

	protected ServerConfigManager m_serverConfigManager;

	protected ServerFilterConfigManager m_serverFilterConfigManager;

	private ProjectService m_projectService;

	private HostinfoService m_hostinfoService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		refreshSpringBeans();
		LOGGER.info("Building state daily report, name={}, domain={}, period={}.", name, domain, period);

		StateReport stateReport = queryHourlyReportsByDuration(domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(stateReport);
		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		refreshSpringBeans();
		LOGGER.info("Building state hourly report side effects, name={}, domain={}, period={}.", name, domain, period);

		StateReport stateReport = m_reportService
								.queryReport(domain, period, new Date(period.getTime()	+ TimeHelper.ONE_HOUR));

		new StateReportVisitor().visitStateReport(stateReport);

		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		refreshSpringBeans();
		LOGGER.info("Building state monthly report, name={}, domain={}, period={}.", name, domain, period);

		StateReport stateReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(stateReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		refreshSpringBeans();
		LOGGER.info("Building state weekly report, name={}, domain={}, period={}.", name, domain, period);

		Date start = period;
		Date end = new Date(start.getTime() + TimeHelper.ONE_DAY * 7);

		StateReport stateReport = queryDailyReportsByDuration(domain, start, end);
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(stateReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	public void initialize() {
		refreshSpringBeans();

		CurrentWeeklyMonthlyReportTask.getInstance().register(new CurrentWeeklyMonthlyTask() {

			@Override
			public void buildCurrentMonthlyTask(String name, String domain, Date start) {
				if (Constants.CAT.equals(domain)) {
					buildMonthlyTask(name, domain, start);
				}
			}

			@Override
			public void buildCurrentWeeklyTask(String name, String domain, Date start) {
				if (Constants.CAT.equals(domain)) {
					buildWeeklyTask(name, domain, start);
				}
			}

			@Override
			public String getReportName() {
				return ID;
			}
		});
	}

	private StateReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		HistoryStateReportMerger merger = new HistoryStateReportMerger(new StateReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				StateReport reportModel = m_reportService
										.queryReport(domain, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_DAY));

				reportModel.accept(merger);
			} catch (Exception e) {
				LOGGER.error("Unable to merge state daily report into duration report, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
		}
		StateReport stateReport = merger.getStateReport();

		new ClearDetailInfo().visitStateReport(stateReport);
		stateReport.setStartTime(start);
		stateReport.setEndTime(end);
		return stateReport;
	}

	private StateReport queryHourlyReportsByDuration(String domain, Date period, Date endDate) {
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		HistoryStateReportMerger merger = new HistoryStateReportMerger(new StateReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			StateReport reportModel = m_reportService.queryReport(domain, date, new Date(date.getTime()	+ TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		StateReport stateReport = merger.getStateReport();

		new ClearDetailInfo().visitStateReport(stateReport);
		stateReport.setStartTime(period);
		stateReport.setEndTime(endDate);
		return stateReport;
	}

	private void updateProjectAndHost(String domain, String ip) {
		refreshSpringBeans();

		if (m_serverFilterConfigManager.validateDomain(domain)) {
			if (!m_projectService.contains(domain)) {
				LOGGER.info("State report discovered new project domain, domain={}, ip={}.", domain, ip);
				m_projectService.insert(domain);

			}
			Hostinfo info = m_hostinfoService.findByIp(ip);

			if (info == null) {
				LOGGER.info("State report discovered new host, domain={}, ip={}.", domain, ip);
				m_hostinfoService.insert(domain, ip);
			} else {
				String oldDomain = info.getDomain();

				if (!domain.equals(oldDomain) && !Constants.CAT.equals(oldDomain)) {
					LOGGER.warn("State report updates host domain, ip={}, oldDomain={}, newDomain={}.", ip, oldDomain,
							domain);
					m_hostinfoService.update(info.getId(), domain, ip);
				}
			}
		}
	}

	private void refreshSpringBeans() {
		StateReportService reportService = CatSpringContext.getBeanIfAvailable(StateReportService.class);
		ServerConfigManager serverConfigManager = CatSpringContext.getBeanIfAvailable(ServerConfigManager.class);
		ServerFilterConfigManager serverFilterConfigManager = CatSpringContext
		      .getBeanIfAvailable(ServerFilterConfigManager.class);
		ProjectService projectService = CatSpringContext.getBeanIfAvailable(ProjectService.class);
		HostinfoService hostinfoService = CatSpringContext.getBeanIfAvailable(HostinfoService.class);

		if (reportService != null) {
			m_reportService = reportService;
		}
		if (serverConfigManager != null) {
			m_serverConfigManager = serverConfigManager;
		}
		if (serverFilterConfigManager != null) {
			m_serverFilterConfigManager = serverFilterConfigManager;
		}
		if (projectService != null) {
			m_projectService = projectService;
		}
		if (hostinfoService != null) {
			m_hostinfoService = hostinfoService;
		}
	}

	public void setHostinfoService(HostinfoService hostinfoService) {
		m_hostinfoService = hostinfoService;
	}

	public void setProjectService(ProjectService projectService) {
		m_projectService = projectService;
	}

	public void setReportService(StateReportService reportService) {
		m_reportService = reportService;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		m_serverConfigManager = serverConfigManager;
	}

	public void setServerFilterConfigManager(ServerFilterConfigManager serverFilterConfigManager) {
		m_serverFilterConfigManager = serverFilterConfigManager;
	}

	public static class ClearDetailInfo extends BaseVisitor {

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			processDomain.getDetails().clear();
		}
	}

	public class StateReportVisitor extends BaseVisitor {

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			String domain = processDomain.getName();
			Set<String> ips = processDomain.getIps();

			for (String ip : ips) {
				if (m_serverFilterConfigManager.validateDomain(domain) && m_serverConfigManager.validateIp(ip)) {
					updateProjectAndHost(domain, ip);
				}
			}
		}
	}

}
