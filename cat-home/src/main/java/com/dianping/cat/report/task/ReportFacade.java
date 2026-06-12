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

import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.task.TaskManager;

public class ReportFacade {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(ReportFacade.class);

	private static final int EXPECTED_REPORT_BUILDER_COUNT = 19;

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
		return m_reportBuilders.get(reportName);
	}

	public void initialize() {
		if (m_reportBuilders.size() < EXPECTED_REPORT_BUILDER_COUNT) {
			String message = String.format("Report facade requires %s Spring task builders but found %s, builders=%s.",
					EXPECTED_REPORT_BUILDER_COUNT, m_reportBuilders.size(), m_reportBuilders.keySet());

			SLF4J_LOGGER.error(message);
			throw new IllegalStateException(message);
		}

		SLF4J_LOGGER.info("Initialized report facade from Spring, builderCount={}, builders={}.",
				m_reportBuilders.size(), m_reportBuilders.keySet());
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

	public void setReportBuilders(Map<String, TaskBuilder> reportBuilders) {
		m_reportBuilders = buildReportBuilderMap(reportBuilders);
	}

}
