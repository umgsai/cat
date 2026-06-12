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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.initialization.DefaultModuleManager;
import org.unidal.initialization.ModuleManager;
import org.unidal.lookup.configuration.Component;
import org.unidal.web.mvc.view.model.ModelHandler;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.CatConstants;
import com.dianping.cat.CatHomeModule;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.build.report.DependencyComponentConfigurator;
import com.dianping.cat.build.report.EventComponentConfigurator;
import com.dianping.cat.build.report.HeartbeatComponentConfigurator;
import com.dianping.cat.build.report.MetricComponentConfigurator;
import com.dianping.cat.build.report.OfflineComponentConfigurator;
import com.dianping.cat.build.report.ProblemComponentConfigurator;
import com.dianping.cat.build.report.ReportComponentConfigurator;
import com.dianping.cat.build.report.StorageComponentConfigurator;
import com.dianping.cat.build.report.TransactionComponentConfigurator;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.core.mybatis.repository.alert.AlertRepository;
import com.dianping.cat.core.mybatis.repository.alteration.AlterationRepository;
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
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.event.transform.EventMergeHelper;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.service.ModelService;
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

		all.add(C(EventMergeHelper.class));

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

		all.add(C(com.dianping.cat.report.page.home.Handler.class) //
								.req(com.dianping.cat.report.page.home.JspViewer.class, (String) null, "m_jspViewer") //
								.req(TcpSocketReceiver.class, (String) null, "m_receiver") //
								.req(MessageConsumer.class, (String) null, "m_realtimeConsumer"));
		all.add(C(com.dianping.cat.report.page.home.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.alteration.Handler.class) //
								.req(com.dianping.cat.report.page.alteration.JspViewer.class, (String) null, "m_jspViewer") //
								.req(AlterationRepository.class, (String) null, "m_alterationDao"));
		all.add(C(com.dianping.cat.report.page.alteration.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.alert.Handler.class) //
								.req(com.dianping.cat.report.page.alert.JspViewer.class, (String) null, "m_jspViewer") //
								.req(SenderManager.class, (String) null, "m_senderManager") //
								.req(AlertRepository.class, (String) null, "m_alertDao"));
		all.add(C(com.dianping.cat.report.page.alert.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.cache.Handler.class) //
								.req(ModelService.class, EventAnalyzer.ID, "m_eventService") //
								.req(com.dianping.cat.report.page.cache.JspViewer.class, (String) null, "m_jspViewer") //
								.req(TransactionReportService.class, (String) null, "m_transactionReportService") //
								.req(EventReportService.class, (String) null, "m_eventReportService") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(ModelService.class, TransactionAnalyzer.ID, "m_transactionService"));
		all.add(C(com.dianping.cat.report.page.cache.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.event.Handler.class) //
								.req(GraphBuilder.class, (String) null, "m_builder") //
								.req(com.dianping.cat.report.page.event.JspViewer.class, (String) null, "m_jspViewer") //
								.req(EventReportService.class, (String) null, "m_reportService") //
								.req(com.dianping.cat.report.page.event.transform.EventMergeHelper.class, (String) null,
												"m_mergeHelper") //
								.req(ModelService.class, EventAnalyzer.ID, "m_service") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(DomainGroupConfigManager.class, (String) null, "m_configManager"));
		all.add(C(com.dianping.cat.report.page.event.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.transaction.Handler.class) //
								.req(GraphBuilder.class, (String) null, "m_builder") //
								.req(com.dianping.cat.report.page.transaction.JspViewer.class, (String) null, "m_jspViewer") //
								.req(com.dianping.cat.report.page.transaction.XmlViewer.class, (String) null, "m_xmlViewer") //
								.req(TransactionReportService.class, (String) null, "m_reportService") //
								.req(com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper.class,
												(String) null, "m_mergeHelper") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(DomainGroupConfigManager.class, (String) null, "m_configManager") //
								.req(ModelService.class, TransactionAnalyzer.ID, "m_service"));
		all.add(C(com.dianping.cat.report.page.transaction.JspViewer.class).req(ModelHandler.class));
		all.add(C(com.dianping.cat.report.page.transaction.XmlViewer.class));

		all.add(C(com.dianping.cat.report.page.problem.Handler.class) //
								.req(com.dianping.cat.report.page.problem.JspViewer.class, (String) null, "m_jspViewer") //
								.req(com.dianping.cat.config.server.ServerConfigManager.class, (String) null, "m_manager") //
								.req(ProblemReportService.class, (String) null, "m_reportService") //
								.req(ModelService.class, ProblemAnalyzer.ID, "m_service") //
								.req(DomainGroupConfigManager.class, (String) null, "m_configManager") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(JsonBuilder.class, (String) null, "m_jsonBuilder"));
		all.add(C(com.dianping.cat.report.page.problem.JspViewer.class).req(ModelHandler.class));

		all.add(A(CatHomeModule.class));

		all.add(C(UserConfigManager.class));

		all.add(C(ResourceConfigManager.class));

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
		removeDuplicateComponents(all);
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

		all.add(C(DomainGroupConfigManager.class));

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

	private void removeDuplicateComponents(List<Component> components) {
		Set<String> keys = new HashSet<String>();

		components.removeIf(component -> {
			String role = component.getModel().getRole();
			String roleHint = component.getModel().getRoleHint();
			String normalizedRoleHint = roleHint == null ? "" : roleHint;
			String key = role + ":" + normalizedRoleHint;

			return !keys.add(key);
		});
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
