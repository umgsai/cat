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
package com.dianping.cat.report.task;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.mybatis.data.TaskDO;
import com.dianping.cat.report.page.business.task.BusinessBaselineReportBuilder;
import com.dianping.cat.report.page.cross.task.CrossReportBuilder;
import com.dianping.cat.report.page.dependency.task.DependencyReportBuilder;
import com.dianping.cat.report.page.event.task.EventReportBuilder;
import com.dianping.cat.report.page.heartbeat.task.HeartbeatReportBuilder;
import com.dianping.cat.report.page.matrix.task.MatrixReportBuilder;
import com.dianping.cat.report.page.overload.task.CapacityUpdateTask;
import com.dianping.cat.report.page.problem.task.ProblemReportBuilder;
import com.dianping.cat.report.page.state.task.StateReportBuilder;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportBuilder;
import com.dianping.cat.report.page.statistics.task.jar.JarReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ClientReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ServiceReportBuilder;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.report.page.storage.task.StorageReportBuilder;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;
import com.dianping.cat.report.task.cmdb.CmdbInfoReloadBuilder;
import com.dianping.cat.report.task.current.CurrentReportBuilder;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;
import com.dianping.cat.task.TaskManager;

@Component
public class ReportFacade {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(ReportFacade.class);

	private static final int EXPECTED_REPORT_BUILDER_COUNT = 19;

	@Resource(name = BusinessBaselineReportBuilder.ID)
	private TaskBuilder businessReportBuilder;

	@Resource(name = TransactionReportBuilder.ID)
	private TaskBuilder transactionReportBuilder;

	@Resource(name = CrossReportBuilder.ID)
	private TaskBuilder crossReportBuilder;

	@Resource(name = DependencyReportBuilder.ID)
	private TaskBuilder dependencyReportBuilder;

	@Resource(name = EventReportBuilder.ID)
	private TaskBuilder eventReportBuilder;

	@Resource(name = HeartbeatReportBuilder.ID)
	private TaskBuilder heartbeatReportBuilder;

	@Resource(name = MatrixReportBuilder.ID)
	private TaskBuilder matrixReportBuilder;

	@Resource(name = "problemReportBuilder")
	private TaskBuilder problemReportBuilder;

	@Resource(name = "storageReportBuilder")
	private TaskBuilder storageReportBuilder;

	@Resource(name = StateReportBuilder.ID)
	private TaskBuilder stateReportBuilder;

	@Resource(name = CurrentReportBuilder.ID)
	private TaskBuilder currentReportBuilder;

	@Resource(name = CmdbInfoReloadBuilder.ID)
	private TaskBuilder cmdbInfoReloadBuilder;

	@Resource(name = RouterConfigBuilder.ID)
	private TaskBuilder routerConfigBuilder;

	@Resource(name = CapacityUpdateTask.ID)
	private TaskBuilder capacityUpdateTask;

	@Resource(name = JarReportBuilder.ID)
	private TaskBuilder jarReportBuilder;

	@Resource(name = HeavyReportBuilder.ID)
	private TaskBuilder heavyReportBuilder;

	@Resource(name = ClientReportBuilder.ID)
	private TaskBuilder clientReportBuilder;

	@Resource(name = ServiceReportBuilder.ID)
	private TaskBuilder serviceReportBuilder;

	@Resource(name = UtilizationReportBuilder.ID)
	private TaskBuilder utilizationReportBuilder;

	private Map<String, TaskBuilder> taskBuilders = new LinkedHashMap<String, TaskBuilder>();

	public boolean builderReport(TaskDO task) {
		try {
			if (task == null) {
				SLF4J_LOGGER.warn("Report build skipped because task is null.");
				return false;
			}
			int type = task.getTaskType();
			String reportName = task.getReportName();
			String reportDomain = task.getReportDomain();
			Date reportPeriod = task.getReportPeriod();
			TaskBuilder reportBuilder = getReportBuilder(reportName);

			if (reportBuilder == null) {
				SLF4J_LOGGER.error("No report builder found, reportName={}, domain={}, type={}, period={}, taskId={}.",
						reportName, reportDomain, type, reportPeriod, task.getId());
				Cat.logError(new RuntimeException("no report builder for type:" + " " + reportName));
				return false;
			} else {
				boolean result = false;

				SLF4J_LOGGER.info("Building report task, reportName={}, domain={}, type={}, period={}, taskId={}.",
						reportName, reportDomain, type, reportPeriod, task.getId());
				if (type == TaskManager.REPORT_HOUR) {
					result = reportBuilder.buildHourlyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_DAILY) {
					result = reportBuilder.buildDailyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_WEEK) {
					result = reportBuilder.buildWeeklyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_MONTH) {
					result = reportBuilder.buildMonthlyTask(reportName, reportDomain, reportPeriod);
				}
				if (result) {
					return result;
				} else {
					SLF4J_LOGGER.error("Report builder returned false, reportName={}, domain={}, type={}, period={}, taskId={}.",
							reportName, reportDomain, type, reportPeriod, task.getId());
				}
			}
		} catch (Exception e) {
			SLF4J_LOGGER.error("Error when building report, task={}.", task, e);
			Cat.logError(e);
			return false;
		}
		return false;
	}

	private TaskBuilder getReportBuilder(String reportName) {
		return taskBuilders.get(reportName);
	}

	@PostConstruct
	public void initialize() {
		if (taskBuilders == null || taskBuilders.isEmpty()) {
			taskBuilders = buildInjectedReportBuilderMap();
		} else {
			setReportBuilders(taskBuilders);
		}

		if (taskBuilders.size() < EXPECTED_REPORT_BUILDER_COUNT) {
			String message = String.format("Report facade requires %s Spring task builders but found %s, builders=%s.",
					EXPECTED_REPORT_BUILDER_COUNT, taskBuilders.size(), taskBuilders.keySet());

			SLF4J_LOGGER.error(message);
			throw new IllegalStateException(message);
		}

		SLF4J_LOGGER.info("Initialized report facade from Spring, builderCount={}, builders={}.",
				taskBuilders.size(), taskBuilders.keySet());
	}

	private Map<String, TaskBuilder> buildInjectedReportBuilderMap() {
		Map<String, TaskBuilder> reportBuilders = new LinkedHashMap<String, TaskBuilder>();

		putReportBuilder(reportBuilders, BusinessBaselineReportBuilder.ID, businessReportBuilder);
		putReportBuilder(reportBuilders, TransactionReportBuilder.ID, transactionReportBuilder);
		putReportBuilder(reportBuilders, CrossReportBuilder.ID, crossReportBuilder);
		putReportBuilder(reportBuilders, DependencyReportBuilder.ID, dependencyReportBuilder);
		putReportBuilder(reportBuilders, EventReportBuilder.ID, eventReportBuilder);
		putReportBuilder(reportBuilders, HeartbeatReportBuilder.ID, heartbeatReportBuilder);
		putReportBuilder(reportBuilders, MatrixReportBuilder.ID, matrixReportBuilder);
		putReportBuilder(reportBuilders, ProblemReportBuilder.ID, problemReportBuilder);
		putReportBuilder(reportBuilders, "problemReportBuilder", problemReportBuilder);
		putReportBuilder(reportBuilders, StorageReportBuilder.ID, storageReportBuilder);
		putReportBuilder(reportBuilders, "storageReportBuilder", storageReportBuilder);
		putReportBuilder(reportBuilders, StateReportBuilder.ID, stateReportBuilder);
		putReportBuilder(reportBuilders, CurrentReportBuilder.ID, currentReportBuilder);
		putReportBuilder(reportBuilders, CmdbInfoReloadBuilder.ID, cmdbInfoReloadBuilder);
		putReportBuilder(reportBuilders, RouterConfigBuilder.ID, routerConfigBuilder);
		putReportBuilder(reportBuilders, CapacityUpdateTask.ID, capacityUpdateTask);
		putReportBuilder(reportBuilders, JarReportBuilder.ID, jarReportBuilder);
		putReportBuilder(reportBuilders, HeavyReportBuilder.ID, heavyReportBuilder);
		putReportBuilder(reportBuilders, ClientReportBuilder.ID, clientReportBuilder);
		putReportBuilder(reportBuilders, ServiceReportBuilder.ID, serviceReportBuilder);
		putReportBuilder(reportBuilders, UtilizationReportBuilder.ID, utilizationReportBuilder);
		return reportBuilders;
	}

	private Map<String, TaskBuilder> buildReportBuilderMap(Map<String, TaskBuilder> springBuilders) {
		Map<String, TaskBuilder> reportBuilders = new LinkedHashMap<String, TaskBuilder>();

		for (Map.Entry<String, TaskBuilder> entry : springBuilders.entrySet()) {
			String beanName = entry.getKey();
			TaskBuilder builder = entry.getValue();
			String reportName = getReportName(builder);

			putReportBuilder(reportBuilders, beanName, builder);
		}
		return reportBuilders;
	}

	private String getReportName(TaskBuilder builder) {
		if (builder == null) {
			return null;
		}
		try {
			Field field = builder.getClass().getField("ID");
			Object value = field.get(null);

			return value instanceof String ? (String) value : null;
		} catch (Exception e) {
			return null;
		}
	}

	private void putReportBuilder(Map<String, TaskBuilder> reportBuilders, String key, TaskBuilder builder) {
		if (key == null || key.length() == 0) {
			return;
		}
		if (builder == null) {
			SLF4J_LOGGER.error("Report task builder is not injected, key={}.", key);
			return;
		}
		if (reportBuilders.containsKey(key) && reportBuilders.get(key) != builder) {
			SLF4J_LOGGER.warn("Duplicate report task builder key found, key={}, oldClass={}, newClass={}.", key,
					reportBuilders.get(key).getClass().getName(), builder.getClass().getName());
		}
		reportBuilders.put(key, builder);
		String reportName = getReportName(builder);

		if (reportName != null && reportName.length() > 0 && !reportName.equals(key)) {
			putReportBuilder(reportBuilders, reportName, builder);
		}
	}

	public void setReportBuilders(Map<String, TaskBuilder> reportBuilders) {
		taskBuilders = buildReportBuilderMap(reportBuilders);
	}

}
