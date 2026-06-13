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
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
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
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.HourlyReportContentTableProvider;
import com.dianping.cat.report.HourlyReportTableProvider;
import com.dianping.cat.report.graph.svg.DefaultGraphBuilder;
import com.dianping.cat.report.graph.svg.DefaultValueTranslater;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.graph.svg.ValueTranslater;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.event.transform.EventMergeHelper;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.state.service.LocalStateService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.server.ServersUpdater;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.report.task.DefaultRemoteServersUpdater;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.ReportFacade;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.config.ReportReloadConfigManager;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.processor.AlertConfigProcessor;
import com.dianping.cat.system.page.config.processor.DependencyConfigProcessor;
import com.dianping.cat.system.page.config.processor.EventConfigProcessor;
import com.dianping.cat.system.page.config.processor.ExceptionConfigProcessor;
import com.dianping.cat.system.page.config.processor.GlobalConfigProcessor;
import com.dianping.cat.system.page.config.processor.HeartbeatConfigProcessor;
import com.dianping.cat.system.page.config.processor.StorageConfigProcessor;
import com.dianping.cat.system.page.config.processor.TransactionConfigProcessor;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.login.service.CookieManager;
import com.dianping.cat.system.page.login.service.SessionManager;
import com.dianping.cat.system.page.login.service.SigninService;
import com.dianping.cat.system.page.login.service.TokenBuilder;
import com.dianping.cat.system.page.login.service.TokenManager;
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

		all.add(C(JsonBuilder.class));

		all.add(C(ValueTranslater.class, DefaultValueTranslater.class));

		all.add(C(GraphBuilder.class, DefaultGraphBuilder.class) //
								.req(ValueTranslater.class, (String) null, "m_translater"));

		all.add(C(EventMergeHelper.class));

		all.add(A(PayloadNormalizer.class));

		return all;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineCommonComponents());

		all.addAll(defineConfigComponents());

		all.addAll(defineConfigProcessorComponents());

		// must define in home module instead of core
		all.addAll(defineTableProviderComponents());

		all.addAll(defineTaskComponents());

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

		all.add(C(com.dianping.cat.report.page.heartbeat.Handler.class) //
								.req(GraphBuilder.class, (String) null, "m_builder") //
								.req(com.dianping.cat.report.page.heartbeat.HistoryGraphs.class, (String) null,
												"m_historyGraphs") //
								.req(com.dianping.cat.report.page.heartbeat.JspViewer.class, (String) null, "m_jspViewer") //
								.req(com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService.class, (String) null,
												"m_reportService") //
								.req(ModelService.class, HeartbeatAnalyzer.ID, "m_service") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager.class,
												(String) null, "m_manager"));
		all.add(C(com.dianping.cat.report.page.heartbeat.HistoryGraphs.class) //
								.req(com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService.class, (String) null,
												"m_reportService") //
								.req(com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager.class,
												(String) null, "m_manager"));
		all.add(C(com.dianping.cat.report.page.heartbeat.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.top.Handler.class) //
								.req(com.dianping.cat.report.page.top.JspViewer.class, (String) null, "m_jspViewer") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(com.dianping.cat.report.page.dependency.ExternalInfoBuilder.class, (String) null,
												"m_externalInfoBuilder") //
								.req(com.dianping.cat.report.page.state.StateBuilder.class, (String) null, "m_stateBuilder") //
								.req(ModelService.class, TopAnalyzer.ID, "m_topService") //
								.req(ModelService.class, TransactionAnalyzer.ID, "m_transactionService") //
								.req(ModelService.class, ProblemAnalyzer.ID, "m_problemService") //
								.req(com.dianping.cat.report.page.top.service.TopReportService.class, (String) null,
												"m_topReportService") //
								.req(com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper.class,
												(String) null, "m_mergeHelper") //
								.req(com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager.class, (String) null,
												"m_configManager") //
								.req(JsonBuilder.class, (String) null, "m_builder"));
		all.add(C(com.dianping.cat.report.page.top.JspViewer.class).req(ModelHandler.class));
		all.add(C(com.dianping.cat.report.page.state.Handler.class) //
								.req(com.dianping.cat.report.page.state.JspViewer.class, (String) null, "m_jspViewer") //
								.req(com.dianping.cat.report.page.state.service.StateReportService.class, (String) null,
												"m_reportService") //
								.req(com.dianping.cat.report.page.state.StateGraphBuilder.class, (String) null,
												"m_stateGraphs") //
								.req(com.dianping.cat.report.page.state.StateBuilder.class, (String) null, "m_stateBuilder") //
								.req(ModelService.class, StateAnalyzer.ID, "m_service") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(com.dianping.cat.config.server.ServerFilterConfigManager.class, (String) null,
												"m_serverFilterConfigManager"));
		all.add(C(com.dianping.cat.report.page.state.JspViewer.class).req(ModelHandler.class));
		all.add(C(com.dianping.cat.report.page.state.StateBuilder.class) //
								.req(com.dianping.cat.system.page.router.config.RouterConfigManager.class, (String) null,
												"m_routerManager") //
								.req(ModelService.class, StateAnalyzer.ID, "m_stateService"));
		all.add(C(com.dianping.cat.report.page.state.StateGraphBuilder.class) //
								.req(com.dianping.cat.report.page.state.service.StateReportService.class, (String) null,
												"m_reportService") //
								.req(com.dianping.cat.config.server.ServerFilterConfigManager.class, (String) null,
												"m_serverFilterConfigManager"));

		all.add(C(com.dianping.cat.report.page.storage.Handler.class) //
								.req(com.dianping.cat.report.page.storage.JspViewer.class, (String) null, "m_jspViewer") //
								.req(com.dianping.cat.report.page.storage.task.StorageReportService.class, (String) null,
												"m_reportService") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(ModelService.class, StorageAnalyzer.ID, "m_service") //
								.req(com.dianping.cat.report.page.storage.transform.StorageMergeHelper.class, (String) null,
												"m_mergeHelper") //
								.req(com.dianping.cat.report.page.storage.config.StorageGroupConfigManager.class,
												(String) null, "m_storageGroupConfigManager") //
								.req(JsonBuilder.class, (String) null, "m_jsonBuilder") //
								.req(AlterationRepository.class, (String) null, "m_alterationDao") //
								.req(com.dianping.cat.alarm.service.AlertService.class, (String) null, "m_alertService") //
								.req(com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder.class,
												(String) null, "m_alertInfoBuilder") //
								.req(com.dianping.cat.consumer.storage.builder.StorageBuilderManager.class, (String) null,
												"m_storageBuilderManager"));
		all.add(C(com.dianping.cat.report.page.storage.JspViewer.class).req(ModelHandler.class));
		all.add(C(com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder.class) //
								.req(com.dianping.cat.alarm.service.AlertService.class, (String) null, "m_alertService"));
		all.add(C(com.dianping.cat.report.page.storage.transform.StorageMergeHelper.class));

		all.add(C(com.dianping.cat.report.page.cross.Handler.class) //
								.req(com.dianping.cat.report.page.cross.JspViewer.class, (String) null, "m_jspViewer") //
								.req(com.dianping.cat.report.page.cross.service.CrossReportService.class, (String) null,
												"m_reportService") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(com.dianping.cat.service.HostinfoService.class, (String) null, "m_hostinfoService") //
								.req(ModelService.class, CrossAnalyzer.ID, "m_service"));
		all.add(C(com.dianping.cat.report.page.cross.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.matrix.Handler.class) //
								.req(com.dianping.cat.report.page.matrix.service.MatrixReportService.class, (String) null,
												"m_reportService") //
								.req(com.dianping.cat.report.page.matrix.JspViewer.class, (String) null, "m_jspViewer") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(ModelService.class, MatrixAnalyzer.ID, "m_service"));
		all.add(C(com.dianping.cat.report.page.matrix.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.overload.Handler.class) //
								.req(com.dianping.cat.report.page.overload.JspViewer.class, (String) null, "m_jspViewer") //
								.req(com.dianping.cat.report.page.overload.task.TableCapacityService.class, (String) null,
												"m_tableCapacityService"));
		all.add(C(com.dianping.cat.report.page.overload.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.logview.Handler.class) //
								.req(com.dianping.cat.report.page.logview.JspViewer.class, (String) null, "m_jspViewer") //
								.req(ModelService.class, "logview", "m_service") //
								.req(com.dianping.cat.config.server.ServerConfigManager.class, (String) null,
												"m_configManager"));
		all.add(C(com.dianping.cat.report.page.logview.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.statistics.Handler.class) //
								.req(com.dianping.cat.report.page.statistics.JspViewer.class, (String) null, "m_jspViewer") //
								.req(HeavyReportService.class, (String) null, "m_heavyReportService") //
								.req(UtilizationReportService.class, (String) null, "m_utilizationReportService") //
								.req(ServiceReportService.class, (String) null, "m_serviceReportService") //
								.req(ClientReportService.class, (String) null, "m_clientReportService") //
								.req(JarReportService.class, (String) null, "m_jarReportService") //
								.req(ProjectService.class, (String) null, "m_projectService") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(AlertSummaryExecutor.class, (String) null, "m_executor"));
		all.add(C(com.dianping.cat.report.page.statistics.JspViewer.class).req(ModelHandler.class));

		all.add(C(com.dianping.cat.report.page.business.Handler.class) //
								.req(com.dianping.cat.report.page.business.JspViewer.class, (String) null, "m_jspViewer") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(com.dianping.cat.service.ProjectService.class, (String) null, "m_projectService") //
								.req(com.dianping.cat.report.page.business.graph.BusinessGraphCreator.class, (String) null,
												"m_graphCreator") //
								.req(com.dianping.cat.system.page.business.config.BusinessTagConfigManager.class,
												(String) null, "m_tagConfigManager"));
		all.add(C(com.dianping.cat.report.page.business.JspViewer.class).req(ModelHandler.class));
		all.add(C(com.dianping.cat.report.page.business.graph.BusinessGraphCreator.class) //
								.req(com.dianping.cat.report.page.business.service.CachedBusinessReportService.class,
												(String) null, "m_reportService") //
								.req(com.dianping.cat.config.business.BusinessConfigManager.class, (String) null,
												"m_configManager") //
								.req(com.dianping.cat.report.page.business.graph.BusinessDataFetcher.class, (String) null,
												"m_dataFetcher") //
								.req(com.dianping.cat.service.ProjectService.class, (String) null, "m_projectService") //
								.req(com.dianping.cat.system.page.business.config.BusinessTagConfigManager.class,
												(String) null, "m_tagManager") //
								.req(com.dianping.cat.report.page.business.task.BusinessKeyHelper.class, (String) null,
												"m_keyHelper") //
								.req(com.dianping.cat.report.page.business.graph.CustomDataCalculator.class, (String) null,
												"m_customDataCalculator") //
								.req(com.dianping.cat.report.page.metric.service.BaselineService.class, (String) null,
												"m_baselineService") //
								.req(com.dianping.cat.report.graph.metric.DataExtractor.class, (String) null,
												"m_dataExtractor") //
								.req(com.dianping.cat.alarm.spi.AlertManager.class, (String) null, "m_alertManager"));
		all.add(C(com.dianping.cat.report.page.business.service.CachedBusinessReportService.class) //
								.req(com.dianping.cat.report.page.business.service.BusinessReportService.class, (String) null,
												"m_reportService") //
								.req(ModelService.class, BusinessAnalyzer.ID, "m_service"));
		all.add(C(com.dianping.cat.report.page.business.graph.BusinessDataFetcher.class) //
								.req(com.dianping.cat.report.page.business.task.BusinessKeyHelper.class, (String) null,
												"m_keyHelper"));
		all.add(C(com.dianping.cat.report.page.business.graph.CustomDataCalculator.class) //
								.req(com.dianping.cat.report.page.business.task.BusinessKeyHelper.class, (String) null,
												"m_keyHelper"));

		all.add(A(CatHomeModule.class));

		all.add(C(UserConfigManager.class));

		all.add(C(ResourceConfigManager.class));

		all.add(C(SigninService.class) //
								.req(TokenManager.class, (String) null, "m_tokenManager") //
								.req(SessionManager.class, (String) null, "m_sessionManager"));
		all.add(C(TokenManager.class) //
								.req(CookieManager.class, (String) null, "m_cookieManager") //
								.req(TokenBuilder.class, (String) null, "m_tokenBuilder"));
		all.add(C(CookieManager.class));
		all.add(C(TokenBuilder.class));
		all.add(C(SessionManager.class));

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
		all.add(C(SampleConfigManager.class) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(com.dianping.cat.config.content.ContentFetcher.class, (String) null, "m_fetcher"));
		all.add(C(ServerFilterConfigManager.class) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(com.dianping.cat.config.content.ContentFetcher.class, (String) null, "m_fetcher"));
		all.add(C(BusinessConfigManager.class) //
								.req(BusinessConfigRepository.class, (String) null, "m_configDao") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));

		return all;
	}

	private List<Component> defineConfigProcessorComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ConfigHtmlParser.class));
		all.add(C(RuleFTLDecorator.class));
		all.add(C(GlobalConfigProcessor.class) //
								.req(ProjectService.class, (String) null, "m_projectService") //
								.req(RouterConfigManager.class, (String) null, "m_routerConfigManager") //
								.req(DomainGroupConfigManager.class, (String) null, "m_domainGroupConfigManger") //
								.req(SenderConfigManager.class, (String) null, "m_senderConfigManager") //
								.req(StorageGroupConfigManager.class, (String) null, "m_groupConfigManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_serverFilterConfigManager") //
								.req(AllReportConfigManager.class, (String) null, "m_transactionConfigManager") //
								.req(ConfigHtmlParser.class, (String) null, "m_configHtmlParser") //
								.req(SampleConfigManager.class, (String) null, "m_sampleConfigManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager") //
								.req(ReportReloadConfigManager.class, (String) null, "m_reloadConfigManager"));
		all.add(C(TransactionConfigProcessor.class) //
								.req(TransactionRuleConfigManager.class, (String) null, "m_configManager") //
								.req(RuleFTLDecorator.class, (String) null, "m_ruleDecorator"));
		all.add(C(EventConfigProcessor.class) //
								.req(EventRuleConfigManager.class, (String) null, "m_configManager") //
								.req(RuleFTLDecorator.class, (String) null, "m_ruleDecorator"));
		all.add(C(StorageConfigProcessor.class) //
								.req(RuleFTLDecorator.class, (String) null, "m_ruleDecorator"));
		all.add(C(HeartbeatConfigProcessor.class) //
								.req(HeartbeatRuleConfigManager.class, (String) null, "m_heartbeatRuleConfigManager") //
								.req(HeartbeatDisplayPolicyManager.class, (String) null, "m_displayPolicyManager") //
								.req(ConfigHtmlParser.class, (String) null, "m_configHtmlParser") //
								.req(RuleFTLDecorator.class, (String) null, "m_ruleDecorator"));
		all.add(C(AlertConfigProcessor.class) //
								.req(AlertConfigManager.class, (String) null, "m_alertConfigManager") //
								.req(AlertPolicyManager.class, (String) null, "m_alertPolicyManager") //
								.req(ConfigHtmlParser.class, (String) null, "m_configHtmlParser"));
		all.add(C(ExceptionConfigProcessor.class) //
								.req(GlobalConfigProcessor.class, (String) null, "m_globalConfigProcessor") //
								.req(ExceptionRuleConfigManager.class, (String) null, "m_exceptionRuleConfigManager"));
		all.add(C(DependencyConfigProcessor.class) //
								.req(GlobalConfigProcessor.class, (String) null, "m_globalConfigManager") //
								.req(TopologyGraphConfigManager.class, (String) null, "m_topologyConfigManager") //
								.req(TopoGraphFormatConfigManager.class, (String) null, "m_formatConfigManager") //
								.req(ConfigHtmlParser.class, (String) null, "m_configHtmlParser"));

		return all;
	}

	private List<Component> defineTaskComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DefaultTaskConsumer.class) //
								.req(ReportFacade.class, (String) null, "m_reportFacade") //
								.req(TaskRepository.class, (String) null, "m_taskDao"));
		all.add(C(ProjectUpdateTask.class) //
								.req(HostinfoService.class, (String) null, "m_hostInfoService") //
								.req(ProjectService.class, (String) null, "m_projectService") //
								.req(TransactionReportService.class, (String) null, "m_reportService"));
		all.add(C(ServersUpdater.class, DefaultRemoteServersUpdater.class) //
								.req(LocalModelService.class, LocalStateService.ID, "m_localService") //
								.req(ModelService.class, StateAnalyzer.ID, "m_service"));

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
