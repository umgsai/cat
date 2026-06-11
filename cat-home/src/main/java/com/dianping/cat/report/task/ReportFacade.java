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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.spring.CatSpringContext;
import com.dianping.cat.task.TaskManager;

@Named
public class ReportFacade extends ContainerHolder implements LogEnabled, Initializable {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(ReportFacade.class);

	private static final int EXPECTED_REPORT_BUILDER_COUNT = 19;

	private Logger m_logger;

	private Map<String, TaskBuilder> m_reportBuilders = new HashMap<String, TaskBuilder>();

	public boolean builderReport(Task task) {
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
					m_logger.error(task.toString());
				}
			}
		} catch (Exception e) {
			SLF4J_LOGGER.error("Error when building report, task={}.", task, e);
			m_logger.error("Error when building report," + e.getMessage(), e);
			Cat.logError(e);
			return false;
		}
		return false;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private TaskBuilder getReportBuilder(String reportName) {
		return m_reportBuilders.get(reportName);
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, TaskBuilder> springBuilders = CatSpringContext.getBeansIfAvailable(TaskBuilder.class);
		Map<String, TaskBuilder> springReportBuilders = buildReportBuilderMap(springBuilders);

		if (springBuilders.size() >= EXPECTED_REPORT_BUILDER_COUNT) {
			m_reportBuilders = springReportBuilders;
			SLF4J_LOGGER.info("Initialized report facade from Spring, builderCount={}, builders={}.",
					m_reportBuilders.size(), m_reportBuilders.keySet());
			return;
		}

		Map<String, TaskBuilder> plexusBuilders = lookupMap(TaskBuilder.class);
		Map<String, TaskBuilder> builders = new HashMap<String, TaskBuilder>();

		builders.putAll(plexusBuilders);
		builders.putAll(springReportBuilders);
		m_reportBuilders = builders;
		SLF4J_LOGGER.info(
				"Initialized report facade, builderCount={}, springBuilderCount={}, plexusBuilderCount={}, builders={}.",
				m_reportBuilders.size(), springBuilders.size(), plexusBuilders.size(), m_reportBuilders.keySet());
	}

	private Map<String, TaskBuilder> buildReportBuilderMap(Map<String, TaskBuilder> springBuilders) {
		Map<String, TaskBuilder> reportBuilders = new HashMap<String, TaskBuilder>();

		for (Map.Entry<String, TaskBuilder> entry : springBuilders.entrySet()) {
			String beanName = entry.getKey();
			TaskBuilder builder = entry.getValue();
			String reportName = getReportName(builder);

			reportBuilders.put(beanName, builder);
			if (reportName != null && reportName.length() > 0) {
				reportBuilders.put(reportName, builder);
			}
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

}
