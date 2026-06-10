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
package com.dianping.cat.report.page.statistics.task.service;

import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.spring.CatSpringContext;

@Named(type = TaskBuilder.class, value = ClientReportBuilder.ID)
public class ClientReportBuilder implements TaskBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientReportBuilder.class);

	public static final String ID = Constants.REPORT_CLIENT;

	@Inject
	protected ClientReportService m_reportService;

	@Inject
	protected TransactionReportService m_transactionReportService;

	@Inject
	private ServerFilterConfigManager m_configManger;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private TransactionMergeHelper m_mergeHelper;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		refreshSpringBeans();
		LOGGER.info("Building client daily report, name={}, domain={}, period={}.", name, domain, period);

		ClientReport clientReport = buildClientReport(period);
		DailyReport report = new DailyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(clientReport);

		return m_reportService.insertDailyReport(report, binaryContent);
	}

	private ClientReport buildClientReport(Date startTime) {
		refreshSpringBeans();

		Date endTime = TimeHelper.addDays(startTime, 1);
		Set<String> domains = m_projectService.findAllDomains();
		ClientReportStatistics statistics = new ClientReportStatistics();

		for (String domain : domains) {
			try {
				if (m_configManger.validateDomain(domain)) {
					TransactionReport r = m_transactionReportService.queryReport(domain, startTime, endTime);
					r = m_mergeHelper.mergeAllMachines(r, Constants.ALL);

					if (r != null) {
						statistics.visitTransactionReport(r);
					}
				}
			} catch (Exception e) {
				LOGGER.error("Unable to visit transaction report for client report, domain={}, start={}, end={}.", domain,
						startTime, endTime, e);
				Cat.logError(domain + " client report visitor error", e);
			}
		}
		return statistics.getClienReport();
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Service client report don't support hourly report!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Service client report don't support monthly report!");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("Service client report don't support weekly report!");
	}

	private void refreshSpringBeans() {
		ClientReportService reportService = CatSpringContext.getBeanIfAvailable(ClientReportService.class);
		TransactionReportService transactionReportService = CatSpringContext.getBeanIfAvailable(TransactionReportService.class);
		ServerFilterConfigManager configManger = CatSpringContext.getBeanIfAvailable(ServerFilterConfigManager.class);
		ProjectService projectService = CatSpringContext.getBeanIfAvailable(ProjectService.class);
		TransactionMergeHelper mergeHelper = CatSpringContext.getBeanIfAvailable(TransactionMergeHelper.class);

		if (reportService != null) {
			m_reportService = reportService;
		}
		if (transactionReportService != null) {
			m_transactionReportService = transactionReportService;
		}
		if (configManger != null) {
			m_configManger = configManger;
		}
		if (projectService != null) {
			m_projectService = projectService;
		}
		if (mergeHelper != null) {
			m_mergeHelper = mergeHelper;
		}
	}

	public void setConfigManager(ServerFilterConfigManager configManger) {
		m_configManger = configManger;
	}

	public void setMergeHelper(TransactionMergeHelper mergeHelper) {
		m_mergeHelper = mergeHelper;
	}

	public void setProjectService(ProjectService projectService) {
		m_projectService = projectService;
	}

	public void setReportService(ClientReportService reportService) {
		m_reportService = reportService;
	}

	public void setTransactionReportService(TransactionReportService transactionReportService) {
		m_transactionReportService = transactionReportService;
	}

}
