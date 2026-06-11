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
import org.unidal.initialization.DefaultModuleManager;
import org.unidal.initialization.ModuleManager;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatConstants;
import com.dianping.cat.CatHomeModule;
import com.dianping.cat.build.report.DependencyComponentConfigurator;
import com.dianping.cat.build.report.EventComponentConfigurator;
import com.dianping.cat.build.report.HeartbeatComponentConfigurator;
import com.dianping.cat.build.report.MetricComponentConfigurator;
import com.dianping.cat.build.report.OfflineComponentConfigurator;
import com.dianping.cat.build.report.ProblemComponentConfigurator;
import com.dianping.cat.build.report.ReportComponentConfigurator;
import com.dianping.cat.build.report.StorageComponentConfigurator;
import com.dianping.cat.build.report.TransactionComponentConfigurator;
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
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.HourlyReportContentTableProvider;
import com.dianping.cat.report.HourlyReportTableProvider;
import com.dianping.cat.report.graph.svg.DefaultGraphBuilder;
import com.dianping.cat.report.graph.svg.DefaultValueTranslater;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.system.page.permission.ResourceConfigManager;
import com.dianping.cat.system.page.permission.UserConfigManager;

public class ComponentsConfigurator extends AbstractJdbcResourceConfigurator {
	private static final String[] CORE_REPLACED_DAO_ROLES = { "com.dianping.cat.core.config.ConfigDao",
			"com.dianping.cat.core.config.BusinessConfigDao", "com.dianping.cat.core.dal.DailyReportDao",
			"com.dianping.cat.core.dal.DailyReportContentDao", "com.dianping.cat.core.dal.HostinfoDao",
			"com.dianping.cat.core.dal.HourlyReportDao", "com.dianping.cat.core.dal.HourlyReportContentDao",
			"com.dianping.cat.core.dal.MonthlyReportDao", "com.dianping.cat.core.dal.MonthlyReportContentDao",
			"com.dianping.cat.core.dal.ProjectDao", "com.dianping.cat.core.dal.TaskDao",
			"com.dianping.cat.core.dal.WeeklyReportDao", "com.dianping.cat.core.dal.WeeklyReportContentDao" };

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	private List<Component> defineCommonComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(JsonBuilder.class));

		all.add(A(DefaultValueTranslater.class));

		all.add(A(DefaultGraphBuilder.class));

		all.add(A(PayloadNormalizer.class));

		return all;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineCommonComponents());

		all.addAll(defineConfigComponents());

		// must define in home module instead of core
		all.addAll(defineTableProviderComponents());

		all.add(A(CatHomeModule.class));

		all.add(A(UserConfigManager.class));

		all.add(A(ResourceConfigManager.class));

		all.add(C(ModuleManager.class, DefaultModuleManager.class) //
								.config(E("topLevelModules").value(CatHomeModule.ID)));

		all.addAll(new TransactionComponentConfigurator().defineComponents());

		all.addAll(new EventComponentConfigurator().defineComponents());

		all.addAll(new MetricComponentConfigurator().defineComponents());

		all.addAll(new HeartbeatComponentConfigurator().defineComponents());

		all.addAll(new ProblemComponentConfigurator().defineComponents());

		all.addAll(new StorageComponentConfigurator().defineComponents());

		all.addAll(new DependencyComponentConfigurator().defineComponents());

		all.addAll(new ReportComponentConfigurator().defineComponents());

		all.addAll(new OfflineComponentConfigurator().defineComponents());

		all.add(defineJdbcDataSourceConfigurationManagerComponent("datasources.xml")
				.config(E("baseDirRef").value("CAT_HOME"))
				.config(E("defaultBaseDir").value(CatConstants.CAT_HOME_DEFAULT_DIR)));

		all.addAll(new CatDatabaseConfigurator().defineComponents());

		// for alarm module
		all.addAll(new HomeAlarmComponentConfigurator().defineComponents());

		// web, please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		removeReplacedDaoComponents(all);
		addCoreRepositoryComponents(all);

		return all;
	}

	private void addCoreRepositoryComponents(List<Component> components) {
		components.add(C(ConfigRepository.class).req(DataSourceManager.class));
		components.add(C(DailyReportRepository.class).req(DataSourceManager.class));
		components.add(C(BusinessConfigRepository.class).req(DataSourceManager.class));
		components.add(C(DailyReportContentRepository.class).req(DataSourceManager.class));
		components.add(C(HostinfoRepository.class).req(DataSourceManager.class));
		components.add(C(HourlyReportRepository.class).req(DataSourceManager.class));
		components.add(C(HourlyReportContentRepository.class).req(DataSourceManager.class));
		components.add(C(MonthlyReportRepository.class).req(DataSourceManager.class));
		components.add(C(MonthlyReportContentRepository.class).req(DataSourceManager.class));
		components.add(C(ProjectRepository.class).req(DataSourceManager.class));
		components.add(C(TaskRepository.class).req(DataSourceManager.class));
		components.add(C(WeeklyReportRepository.class).req(DataSourceManager.class));
		components.add(C(WeeklyReportContentRepository.class).req(DataSourceManager.class));
	}

	private List<Component> defineConfigComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DomainGroupConfigManager.class));

		return all;
	}

	private List<Component> defineTableProviderComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(HourlyReportTableProvider.class));
		all.add(A(HourlyReportContentTableProvider.class));

		return all;
	}

	private void removeReplacedDaoComponents(List<Component> components) {
		components.removeIf(component -> isCoreReplacedDaoRole(component.getModel().getRole()));
	}

	private boolean isCoreReplacedDaoRole(String role) {
		for (String replacedDaoRole : CORE_REPLACED_DAO_ROLES) {
			if (replacedDaoRole.equals(role)) {
				return true;
			}
		}
		return false;
	}
}
