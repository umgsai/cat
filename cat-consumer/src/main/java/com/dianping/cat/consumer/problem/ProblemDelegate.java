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
package com.dianping.cat.consumer.problem;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.problem.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

@Component("problemDelegate")
public class ProblemDelegate implements ReportDelegate<ProblemReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProblemDelegate.class);

	@Resource
	private TaskManager taskManager;

	@Resource
	private ServerFilterConfigManager serverFilterConfigManager;

	@Override
	public void afterLoad(Map<String, ProblemReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, ProblemReport> reports) {
		try {
			ProblemReportFilter problemReportURLFilter = new ProblemReportFilter();

			for (Entry<String, ProblemReport> entry : reports.entrySet()) {
				ProblemReport report = entry.getValue();

				problemReportURLFilter.visitProblemReport(report);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to filter problem reports before saving, reportCount={}.",
					reports == null ? 0 : reports.size(), e);
			Cat.logError(e);
		}
	}

	@Override
	public byte[] buildBinary(ProblemReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(ProblemReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(ProblemReport report) {
		String domain = report.getDomain();

		if (serverFilterConfigManager.validateDomain(domain)) {
			return taskManager.createTask(report.getStartTime(), domain, ProblemAnalyzer.ID,	TaskProlicy.ALL_EXCLUED_HOURLY);
		} else {
			return true;
		}
	}

	@Override
	public String getDomain(ProblemReport report) {
		return report.getDomain();
	}

	@Override
	public ProblemReport makeReport(String domain, long startTime, long duration) {
		ProblemReport report = new ProblemReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public ProblemReport mergeReport(ProblemReport old, ProblemReport other) {
		ProblemReportMerger merger = new ProblemReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public ProblemReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public ProblemReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public void setConfigManager(ServerFilterConfigManager configManager) {
		serverFilterConfigManager = configManager;
	}
}
