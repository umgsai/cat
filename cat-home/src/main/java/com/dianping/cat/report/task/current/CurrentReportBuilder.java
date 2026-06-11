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
package com.dianping.cat.report.task.current;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.spring.CatSpringContext;

public class CurrentReportBuilder implements TaskBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(CurrentReportBuilder.class);

	public static final String ID = Constants.CURRENT_REPORT;

	private ProjectService m_projectService;

	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		refreshSpringBeans();
		LOGGER.info("Building current weekly/monthly refresh task, name={}, domain={}, period={}.", name, domain, period);

		CurrentWeeklyMonthlyReportTask reportTask = CurrentWeeklyMonthlyReportTask.getInstance();

		try {
			List<Project> projects = m_projectService.findAll();
			List<String> domains = new ArrayList<String>();

			for (Project project : projects) {
				if (m_serverFilterConfigManager.validateDomain(project.getDomain())) {
					domains.add(project.getDomain());
				}
			}
			reportTask.setDomains(domains);
			LOGGER.info("Starting current weekly/monthly refresh task, domainCount={}.", domains.size());

			Threads.forGroup(Constants.CAT).start(reportTask);
		} catch (DalException e) {
			LOGGER.error("Unable to build current weekly/monthly refresh task, name={}, domain={}, period={}.", name,
					domain, period, e);
			Cat.logError(e);
		}
		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("current weekly monthly report builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("current weekly monthly report builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("current weekly monthly report builder don't support weekly task");
	}

	private void refreshSpringBeans() {
		ProjectService projectService = CatSpringContext.getBeanIfAvailable(ProjectService.class);
		ServerFilterConfigManager serverFilterConfigManager = CatSpringContext
		      .getBeanIfAvailable(ServerFilterConfigManager.class);

		if (projectService != null) {
			m_projectService = projectService;
		}
		if (serverFilterConfigManager != null) {
			m_serverFilterConfigManager = serverFilterConfigManager;
		}
	}

	public void setProjectService(ProjectService projectService) {
		m_projectService = projectService;
	}

	public void setServerFilterConfigManager(ServerFilterConfigManager serverFilterConfigManager) {
		m_serverFilterConfigManager = serverFilterConfigManager;
	}
}
