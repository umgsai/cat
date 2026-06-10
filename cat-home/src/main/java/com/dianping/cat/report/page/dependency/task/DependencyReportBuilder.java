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
package com.dianping.cat.report.page.dependency.task;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.core.mybatis.repository.topologygraph.TopologyGraphRepository;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.spring.CatSpringContext;

@Named(type = TaskBuilder.class, value = DependencyReportBuilder.ID)
public class DependencyReportBuilder implements TaskBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyReportBuilder.class);

	public static final String ID = DependencyAnalyzer.ID;

	@Inject
	private DependencyReportService m_reportService;

	@Inject
	private TopologyGraphBuilder m_graphBuilder;

	@Inject
	private TopologyGraphRepository m_topologyGraphDao;

	@Override
	public boolean buildDailyTask(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no daily report builder for dependency!");
	}

	@Override
	public boolean buildHourlyTask(String name, String reportDomain, Date reportPeriod) {
		refreshSpringBeans();
		LOGGER.info("Building dependency hourly topology graph, name={}, reportDomain={}, period={}.", name, reportDomain,
				reportPeriod);

		Date end = new Date(reportPeriod.getTime() + TimeHelper.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(reportPeriod, end, DependencyAnalyzer.ID);
		boolean result = true;

		LOGGER.info("Preparing dependency topology graph, period={}, domainCount={}.", reportPeriod, domains.size());
		m_graphBuilder.getGraphs().clear();
		for (String domain : domains) {
			DependencyReport report = m_reportService.queryReport(domain, reportPeriod, end);

			m_graphBuilder.visitDependencyReport(report);
		}

		Map<Long, TopologyGraph> graphs = m_graphBuilder.getGraphs();
		for (Entry<Long, TopologyGraph> entry : graphs.entrySet()) {
			try {
				Date date = new Date(entry.getKey());
				TopologyGraph graph = entry.getValue();

				com.dianping.cat.home.dal.report.TopologyGraph proto = m_topologyGraphDao.createLocal();
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				proto.setType(3);
				proto.setPeriod(date);
				proto.setCreationDate(new Date());
				proto.setIp(ip);
				proto.setContent(DefaultNativeBuilder.build(graph));

				m_topologyGraphDao.insert(proto);
			} catch (Exception e) {
				result = false;
				LOGGER.error("Unable to insert dependency topology graph, reportDomain={}, period={}, graphPeriod={}.",
						reportDomain, reportPeriod, new Date(entry.getKey()), e);
				Cat.logError(e);
			}
		}
		return result;
	}

	@Override
	public boolean buildMonthlyTask(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no month report builder for dependency!");
	}

	@Override
	public boolean buildWeeklyTask(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no week report builder for dependency!");
	}

	private void refreshSpringBeans() {
		DependencyReportService reportService = CatSpringContext.getBeanIfAvailable(DependencyReportService.class);
		TopologyGraphRepository topologyGraphDao = CatSpringContext.getBeanIfAvailable(TopologyGraphRepository.class);
		TopologyGraphBuilder graphBuilder = CatSpringContext.getBeanIfAvailable(TopologyGraphBuilder.class);

		if (reportService != null) {
			m_reportService = reportService;
		}
		if (topologyGraphDao != null) {
			m_topologyGraphDao = topologyGraphDao;
		}
		if (graphBuilder != null) {
			m_graphBuilder = graphBuilder;
		}
	}

	public void setGraphBuilder(TopologyGraphBuilder graphBuilder) {
		m_graphBuilder = graphBuilder;
	}

	public void setReportService(DependencyReportService reportService) {
		m_reportService = reportService;
	}

	public void setTopologyGraphDao(TopologyGraphRepository topologyGraphDao) {
		m_topologyGraphDao = topologyGraphDao;
	}

}
