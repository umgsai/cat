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

import jakarta.annotation.Resource;

import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.service.ProjectService;

@Component(ClientReportBuilder.ID)
public class ClientReportBuilder implements TaskBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientReportBuilder.class);

	public static final String ID = Constants.REPORT_CLIENT;

	@Resource
	protected ClientReportService reportService;

	@Resource
	protected TransactionReportService transactionReportService;

	@Resource
	private ServerFilterConfigManager serverFilterConfigManager;

	@Resource
	private ProjectService projectService;

	@Resource
	private TransactionMergeHelper transactionMergeHelper;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		LOGGER.info("Building client daily report, name={}, domain={}, period={}.", name, domain, period);

		ClientReport clientReport = buildClientReport(period);
		DailyReportDO report = new DailyReportDO();

		report.setCreateTime(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(clientReport);

		return reportService.insertDailyReport(report, binaryContent);
	}

	private ClientReport buildClientReport(Date startTime) {
		Date endTime = TimeHelper.addDays(startTime, 1);
		Set<String> domains = projectService.findAllDomains();
		ClientReportStatistics statistics = new ClientReportStatistics();

		for (String domain : domains) {
			try {
				if (serverFilterConfigManager.validateDomain(domain)) {
					TransactionReport r = transactionReportService.queryReport(domain, startTime, endTime);
					r = transactionMergeHelper.mergeAllMachines(r, Constants.ALL);

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

	public void setConfigManager(ServerFilterConfigManager configManager) {
		this.serverFilterConfigManager = configManager;
	}

	public void setMergeHelper(TransactionMergeHelper mergeHelper) {
		this.transactionMergeHelper = mergeHelper;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setReportService(ClientReportService reportService) {
		this.reportService = reportService;
	}

	public void setTransactionReportService(TransactionReportService transactionReportService) {
		this.transactionReportService = transactionReportService;
	}

}
