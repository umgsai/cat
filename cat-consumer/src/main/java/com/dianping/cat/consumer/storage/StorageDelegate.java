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
package com.dianping.cat.consumer.storage;

import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.storage.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.storage.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class StorageDelegate implements ReportDelegate<StorageReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageDelegate.class);

	private TaskManager m_taskManager;

	private ServerFilterConfigManager m_configManager;

	private StorageReportUpdater m_reportUpdater;

	@Override
	public void afterLoad(Map<String, StorageReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, StorageReport> reports) {
		for (StorageReport report : reports.values()) {

			m_reportUpdater.updateStorageIds(report.getId(), reports.keySet(), report);
		}
	}

	@Override
	public byte[] buildBinary(StorageReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(StorageReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(StorageReport report) {
		String id = report.getId();

		if (m_configManager.validateDomain(id)) {
			return m_taskManager.createTask(report.getStartTime(), id, StorageAnalyzer.ID, TaskProlicy.ALL_EXCLUED_HOURLY);
		} else {
			return true;
		}
	}

	@Override
	public String getDomain(StorageReport report) {
		return report.getId();
	}

	@Override
	public StorageReport makeReport(String id, long startTime, long duration) {
		StorageReport report = new StorageReport(id);
		int index = id.lastIndexOf("-");

		if (index <= 0 || index >= id.length() - 1) {
			LOGGER.warn("Invalid storage report id, id={}, expectedFormat=<name>-<type>, startTime={}, duration={}.",
			      id, startTime, duration);
			report.setName(id);
		} else {
			String name = id.substring(0, index);
			String type = id.substring(index + 1);

			report.setName(name).setType(type);
		}
		report.setStartTime(new Date(startTime)).setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public StorageReport mergeReport(StorageReport old, StorageReport other) {
		StorageReportMerger merger = new StorageReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public StorageReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public StorageReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}

	public void setTaskManager(TaskManager taskManager) {
		m_taskManager = taskManager;
	}

	public void setConfigManager(ServerFilterConfigManager configManager) {
		m_configManager = configManager;
	}

	public void setReportUpdater(StorageReportUpdater reportUpdater) {
		m_reportUpdater = reportUpdater;
	}

}
