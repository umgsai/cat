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
package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.BusinessDelegate;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossDelegate;
import com.dianping.cat.consumer.cross.IpConvertManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyDelegate;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventDelegate;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatDelegate;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixDelegate;
import com.dianping.cat.consumer.problem.DefaultProblemHandler;
import com.dianping.cat.consumer.problem.LongExecutionProblemHandler;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemDelegate;
import com.dianping.cat.consumer.problem.ProblemHandler;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateDelegate;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageDelegate;
import com.dianping.cat.consumer.storage.StorageReportUpdater;
import com.dianping.cat.consumer.storage.builder.StorageBuilderManager;
import com.dianping.cat.consumer.storage.builder.StorageCacheBuilder;
import com.dianping.cat.consumer.storage.builder.StorageRPCBuilder;
import com.dianping.cat.consumer.storage.builder.StorageSQLBuilder;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopDelegate;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionDelegate;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.mybatis.repository.hourly.report.content.HourlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourlyreport.HourlyReportRepository;
import com.dianping.cat.core.mybatis.repository.project.ProjectRepository;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.DefaultReportManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.task.TaskManager;
import org.unidal.cat.message.storage.MessageDumperManager;
import org.unidal.cat.message.storage.MessageFinderManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineTransactionComponents());
		all.addAll(defineEventComponents());
		all.addAll(defineProblemComponents());
		all.addAll(defineHeartbeatComponents());
		all.addAll(defineTopComponents());
		all.addAll(defineDumpComponents());
		all.addAll(defineStateComponents());
		all.addAll(defineCrossComponents());
		all.addAll(defineMatrixComponents());
		all.addAll(defineDependencyComponents());
		all.addAll(defineStorageComponents());
		all.addAll(defineBusinessComponents());

		all.add(C(AtomicMessageConfigManager.class) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(ContentFetcher.class, (String) null, "m_fetcher"));
		all.add(C(ServerConfigManager.class) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(ContentFetcher.class, (String) null, "m_fetcher"));
		all.add(C(TpValueStatisticConfigManager.class) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(ContentFetcher.class, (String) null, "m_fetcher") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(A(AllReportConfigManager.class));
		all.add(C(Module.class, CatConsumerModule.ID, CatConsumerModule.class));

		return all;
	}

	private Collection<Component> defineCrossComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = CrossAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, CrossAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(IpConvertManager.class, (String) null, "m_ipConvertManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, CrossDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_serverFilterConfigManager"));

		all.add(C(IpConvertManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineDependencyComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = DependencyAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, DependencyAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_serverFilterConfigManager") //
								.req(DatabaseParser.class, (String) null, "m_parser") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, DependencyDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager"));

		all.add(C(DatabaseParser.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineDumpComponents() {
		final List<Component> all = new ArrayList<Component>();
		all.add(C(MessageAnalyzer.class, DumpAnalyzer.ID, DumpAnalyzer.class).is(PER_LOOKUP) //
								.req(ServerStatisticManager.class, (String) null, "m_serverStateManager") //
								.req(MessageDumperManager.class, (String) null, "m_dumperManager") //
								.req(MessageFinderManager.class, (String) null, "m_finderManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));

		all.add(C(MessageBucketManager.class, LocalMessageBucketManager.ID, LocalMessageBucketManager.class) //
								.req(ServerConfigManager.class, PathBuilder.class, ServerStatisticManager.class));

		return all;
	}

	private Collection<Component> defineEventComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = EventAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, EventAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(AtomicMessageConfigManager.class, (String) null, "m_atomicMessageConfigManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, EventDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_configManager") //
								.req(AllReportConfigManager.class, (String) null, "m_allManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager") //
								.req(AtomicMessageConfigManager.class, (String) null, "m_atomicMessageConfigManager"));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineHeartbeatComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = HeartbeatAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, HeartbeatAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_serverFilterConfigManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, HeartbeatDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_manager"));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineMatrixComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = MatrixAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, MatrixAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, MatrixDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_configManager"));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineBusinessComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = BusinessAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, BusinessAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(BusinessConfigManager.class, (String) null, "m_configManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, BusinessDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager"));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));
		return all;
	}

	private Collection<Component> defineProblemComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = ProblemAnalyzer.ID;

		all.add(C(ProblemHandler.class, DefaultProblemHandler.ID, DefaultProblemHandler.class)//
								.config(E("errorType").value("Error,RuntimeException,Exception"))//
								.req(ServerConfigManager.class));

		all.add(C(ProblemHandler.class, LongExecutionProblemHandler.ID, LongExecutionProblemHandler.class) //
								.req(ServerConfigManager.class));

		all.add(C(MessageAnalyzer.class, ID, ProblemAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager")
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager")
								.req(ProblemHandler.class, //
														new String[] { DefaultProblemHandler.ID, LongExecutionProblemHandler.ID }, "m_handlers"));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		all.add(C(ReportDelegate.class, ID, ProblemDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_configManager"));

		return all;
	}

	private Collection<Component> defineStateComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = StateAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, StateAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(ServerStatisticManager.class, (String) null, "m_serverStateManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_serverFilterConfigManager") //
								.req(ProjectService.class, (String) null, "m_projectService") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, StateDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager") //
								.req(ReportBucketManager.class, (String) null, "m_bucketManager"));

		all.add(C(ProjectService.class) //
								.req(ProjectRepository.class, (String) null, "m_projectDao") //
								.req(ServerConfigManager.class, (String) null, "m_manager"));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineTopComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TopAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, TopAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_serverFilterConfigManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager")
								.config(E("errorType").value("Error,RuntimeException,Exception")));
		all.add(C(ReportDelegate.class, ID, TopDelegate.class));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineTransactionComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TransactionAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, TransactionAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_filterConfigManager") //
								.req(TpValueStatisticConfigManager.class, (String) null, "m_statisticManager") //
								.req(AtomicMessageConfigManager.class, (String) null, "m_atomicMessageConfigManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, TransactionDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_configManager") //
								.req(AllReportConfigManager.class, (String) null, "m_transactionManager") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager") //
								.req(AtomicMessageConfigManager.class, (String) null, "m_atomicMessageConfigManager"));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineStorageComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = StorageAnalyzer.ID;

		all.add(A(StorageReportUpdater.class));
		all.add(A(StorageBuilderManager.class));
		all.add(A(StorageSQLBuilder.class));
		all.add(A(StorageCacheBuilder.class));
		all.add(A(StorageRPCBuilder.class));

		all.add(C(MessageAnalyzer.class, ID, StorageAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID, "m_reportManager") //
								.req(DatabaseParser.class, (String) null, "m_databaseParser") //
								.req(StorageReportUpdater.class, (String) null, "m_updater") //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager"));
		all.add(C(ReportDelegate.class, ID, StorageDelegate.class) //
								.req(TaskManager.class, (String) null, "m_taskManager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_configManager") //
								.req(StorageReportUpdater.class, (String) null, "m_reportUpdater"));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportRepository.class, HourlyReportContentRepository.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}
}
