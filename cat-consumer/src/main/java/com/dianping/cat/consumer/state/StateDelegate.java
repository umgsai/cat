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
package com.dianping.cat.consumer.state;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.state.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

@Component("stateDelegate")
public class StateDelegate implements ReportDelegate<StateReport> {

	@Resource
	private TaskManager taskManager;

	@Resource
	private ReportBucketManager reportBucketManager;

	@Override
	public void afterLoad(Map<String, StateReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, StateReport> reports) {
	}

	@Override
	public byte[] buildBinary(StateReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(StateReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(StateReport report) {
		Date startTime = report.getStartTime();
		String domain = report.getDomain();

		taskManager.createTask(startTime, domain, StateAnalyzer.ID, TaskProlicy.ALL);
		taskManager.createTask(startTime, domain, Constants.REPORT_ROUTER, TaskProlicy.HOULY);
		taskManager.createTask(startTime, domain, Constants.APP_DATABASE_PRUNER, TaskProlicy.DAILY);
		taskManager.createTask(startTime, domain, Constants.METRIC_GRAPH_PRUNER, TaskProlicy.DAILY);
		taskManager.createTask(startTime, domain, Constants.WEB_DATABASE_PRUNER, TaskProlicy.DAILY);
		// taskManager.createTask(startTime, domain, Constants.CMDB, TaskProlicy.HOULY);
		taskManager.createTask(startTime, domain, Constants.REPORT_DATABASE_CAPACITY, TaskProlicy.ALL);
		taskManager.createTask(startTime, domain, Constants.REPORT_JAR, TaskProlicy.HOULY);
		taskManager.createTask(startTime, domain, Constants.REPORT_HEAVY, TaskProlicy.ALL);
		taskManager.createTask(startTime, domain, Constants.REPORT_UTILIZATION, TaskProlicy.ALL);
		taskManager.createTask(startTime, domain, Constants.REPORT_SERVICE, TaskProlicy.ALL);

		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);

		// for daily report aggreation done
		if (hour >= 4) {
			taskManager.createTask(startTime, domain, Constants.CURRENT_REPORT, TaskProlicy.DAILY);
			taskManager.createTask(startTime, domain, Constants.REPORT_CLIENT, TaskProlicy.DAILY);
		}
		// clear local report
		reportBucketManager.clearOldReports();
		return true;
	}

	@Override
	public String getDomain(StateReport report) {
		return report.getDomain();
	}

	@Override
	public StateReport makeReport(String domain, long startTime, long duration) {
		StateReport report = new StateReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public StateReport mergeReport(StateReport old, StateReport other) {
		StateReportMerger merger = new StateReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public StateReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public StateReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public void setBucketManager(ReportBucketManager bucketManager) {
		reportBucketManager = bucketManager;
	}
}
