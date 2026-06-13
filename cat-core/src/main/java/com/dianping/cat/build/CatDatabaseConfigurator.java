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
package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.mybatis.repository.business.config.BusinessConfigRepository;
import com.dianping.cat.core.mybatis.repository.daily.report.content.DailyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hostinfo.HostinfoRepository;
import com.dianping.cat.core.mybatis.repository.hourly.report.content.HourlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourlyreport.HourlyReportRepository;
import com.dianping.cat.core.mybatis.repository.monthly.report.content.MonthlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.monthreport.MonthlyReportRepository;
import com.dianping.cat.core.mybatis.repository.project.ProjectRepository;
import com.dianping.cat.core.mybatis.repository.task.TaskRepository;
import com.dianping.cat.core.mybatis.repository.weekly.report.content.WeeklyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.weeklyreport.WeeklyReportRepository;
import com.dianping.cat.core.report.daily.repository.DailyReportRepository;

public final class CatDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
	private static final String[] REPLACED_DAO_ROLES = { "com.dianping.cat.core.config.ConfigDao",
			"com.dianping.cat.core.config.BusinessConfigDao", "com.dianping.cat.core.dal.DailyReportDao",
			"com.dianping.cat.core.dal.DailyReportContentDao", "com.dianping.cat.core.dal.HostinfoDao",
			"com.dianping.cat.core.dal.HourlyReportDao", "com.dianping.cat.core.dal.HourlyReportContentDao",
			"com.dianping.cat.core.dal.MonthlyReportDao", "com.dianping.cat.core.dal.MonthlyReportContentDao",
			"com.dianping.cat.core.dal.ProjectDao", "com.dianping.cat.core.dal.TaskDao",
			"com.dianping.cat.core.dal.WeeklyReportDao", "com.dianping.cat.core.dal.WeeklyReportContentDao" };

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		// all.add(defineJdbcDataSourceComponent("cat", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/cat", "root", "***", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

		defineSimpleTableProviderComponents(all, "cat", com.dianping.cat.core.dal._INDEX.getEntityClasses());
		defineDaoComponents(all, com.dianping.cat.core.dal._INDEX.getDaoClasses());

		defineSimpleTableProviderComponents(all, "cat", com.dianping.cat.core.config._INDEX.getEntityClasses());
		defineDaoComponents(all, com.dianping.cat.core.config._INDEX.getDaoClasses());
		removeReplacedDaoComponents(all);
		addRepositoryComponents(all);

		return all;
	}

	private void addRepositoryComponents(List<Component> components) {
		components.add(C(ConfigRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(DailyReportRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(BusinessConfigRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(DailyReportContentRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(HostinfoRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(HourlyReportRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(HourlyReportContentRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(MonthlyReportRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(MonthlyReportContentRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(ProjectRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(TaskRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(WeeklyReportRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
		components.add(C(WeeklyReportContentRepository.class).req(DataSourceManager.class, (String) null, "m_dataSourceManager"));
	}

	private boolean isReplacedDaoRole(String role) {
		for (String replacedDaoRole : REPLACED_DAO_ROLES) {
			if (replacedDaoRole.equals(role)) {
				return true;
			}
		}
		return false;
	}

	private void removeReplacedDaoComponents(List<Component> components) {
		components.removeIf(component -> isReplacedDaoRole(component.getModel().getRole()));
	}
}
