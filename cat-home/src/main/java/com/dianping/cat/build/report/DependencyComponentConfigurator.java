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
package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;
import org.unidal.web.mvc.view.model.ModelHandler;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.core.mybatis.repository.topologygraph.TopologyGraphRepository;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.page.dependency.ExternalInfoBuilder;
import com.dianping.cat.report.page.dependency.Handler;
import com.dianping.cat.report.page.dependency.JspViewer;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.graph.DependencyItemBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.dependency.service.CompositeDependencyService;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.page.dependency.service.HistoricalDependencyService;
import com.dianping.cat.report.page.dependency.service.LocalDependencyService;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class DependencyComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DependencyItemBuilder.class).req(TopologyGraphConfigManager.class));

		all.add(C(TopologyGraphBuilder.class).req(DependencyItemBuilder.class));

		all.add(C(TopologyGraphManager.class) //
								.req(ModelService.class, DependencyAnalyzer.ID, "m_service") //
								.req(DependencyItemBuilder.class, (String) null, "m_itemBuilder") //
								.req(TopoGraphFormatConfigManager.class, (String) null, "m_configManager") //
								.req(ServerConfigManager.class, (String) null, "m_manager") //
								.req(ServerFilterConfigManager.class, (String) null, "m_serverFilterConfigManager") //
								.req(ProjectService.class, (String) null, "m_projectService") //
								.req(TopologyGraphRepository.class, (String) null, "m_topologyGraphDao"));

		all.add(C(TopologyGraphConfigManager.class));

		all.add(C(TopoGraphFormatConfigManager.class));

		all.add(C(ExternalInfoBuilder.class) //
								.req(ServerConfigManager.class, (String) null, "m_serverConfigManager") //
								.req(ModelService.class, ProblemAnalyzer.ID, "m_problemservice") //
								.req(DependencyReportService.class, (String) null, "m_reportService"));

		all.add(C(Handler.class) //
								.req(ModelService.class, DependencyAnalyzer.ID, "m_dependencyService") //
								.req(TopologyGraphManager.class, (String) null, "m_graphManager") //
								.req(ExternalInfoBuilder.class, (String) null, "m_externalInfoBuilder") //
								.req(JspViewer.class, (String) null, "m_jspViewer") //
								.req(PayloadNormalizer.class, (String) null, "m_normalizePayload") //
								.req(TopoGraphFormatConfigManager.class, (String) null, "m_formatConfigManager"));
		all.add(C(JspViewer.class).req(ModelHandler.class));

		all.add(C(DependencyReportService.class));

		all.add(C(LocalModelService.class, LocalDependencyService.ID, LocalDependencyService.class) //
								.req(ServerConfigManager.class) //
								.req(ReportBucketManager.class));
		all.add(C(ModelService.class, "dependency-historical", HistoricalDependencyService.class) //
								.req(DependencyReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, DependencyAnalyzer.ID, CompositeDependencyService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "dependency-historical" }, "m_services"));

		return all;
	}
}
