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
package com.dianping.cat.report.task.reload;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.dianping.cat.support.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.config.ReportReloadConfigManager;
import com.dianping.cat.helper.TimeHelper;

@Component
public class ReportReloadTask implements Task {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportReloadTask.class);

	private static final long DURATION = TimeHelper.ONE_HOUR;

	private static final int EXPECTED_RELOADER_COUNT = 11;

	@Resource
	private ReportReloadConfigManager reportReloadConfigManager;

	@Resource(name = "businessReportReloader")
	private ReportReloader businessReportReloader;

	@Resource(name = "transactionReportReloader")
	private ReportReloader transactionReportReloader;

	@Resource(name = "crossReportReloader")
	private ReportReloader crossReportReloader;

	@Resource(name = "dependencyReportReloader")
	private ReportReloader dependencyReportReloader;

	@Resource(name = "eventReportReloader")
	private ReportReloader eventReportReloader;

	@Resource(name = "heartbeatReportReloader")
	private ReportReloader heartbeatReportReloader;

	@Resource(name = "matrixReportReloader")
	private ReportReloader matrixReportReloader;

	@Resource(name = "problemReportReloader")
	private ReportReloader problemReportReloader;

	@Resource(name = "storageReportReloader")
	private ReportReloader storageReportReloader;

	@Resource(name = "topReportReloader")
	private ReportReloader topReportReloader;

	@Resource(name = "stateReportReloader")
	private ReportReloader stateReportReloader;

	private Map<String, ReportReloader> reportReloaders;

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@PostConstruct
	public void initialize() {
		if (reportReloaders == null) {
			reportReloaders = buildReportReloaderMap();
		}

		if (reportReloaders == null || reportReloaders.size() < EXPECTED_RELOADER_COUNT) {
			String message = String.format(
					"Report reload task requires %s Spring reloaders but found %s, reloaders=%s.",
					EXPECTED_RELOADER_COUNT, reportReloaders == null ? 0 : reportReloaders.size(),
					reportReloaders == null ? Collections.emptySet() : reportReloaders.keySet());

			LOGGER.error(message);
			throw new IllegalStateException(message);
		}

		LOGGER.info("Initialized report reload task from Spring, reloaderCount={}, reloaders={}.",
				reportReloaders.size(), reportReloaders.keySet());
	}

	private Map<String, ReportReloader> buildReportReloaderMap() {
		Map<String, ReportReloader> reloaders = new LinkedHashMap<String, ReportReloader>();

		putReloader(reloaders, businessReportReloader, "businessReportReloader");
		putReloader(reloaders, transactionReportReloader, "transactionReportReloader");
		putReloader(reloaders, crossReportReloader, "crossReportReloader");
		putReloader(reloaders, dependencyReportReloader, "dependencyReportReloader");
		putReloader(reloaders, eventReportReloader, "eventReportReloader");
		putReloader(reloaders, heartbeatReportReloader, "heartbeatReportReloader");
		putReloader(reloaders, matrixReportReloader, "matrixReportReloader");
		putReloader(reloaders, problemReportReloader, "problemReportReloader");
		putReloader(reloaders, storageReportReloader, "storageReportReloader");
		putReloader(reloaders, topReportReloader, "topReportReloader");
		putReloader(reloaders, stateReportReloader, "stateReportReloader");
		return reloaders;
	}

	private void putReloader(Map<String, ReportReloader> reloaders, ReportReloader reloader, String fallbackId) {
		if (reloader == null) {
			LOGGER.error("Report reloader is not injected, fallbackId={}.", fallbackId);
			return;
		}
		String id = reloader.getId();

		if (id == null || id.length() == 0) {
			LOGGER.error("Report reloader id is empty, fallbackId={}, className={}.", fallbackId,
					reloader.getClass().getName());
			id = fallbackId;
		}
		if (reloaders.containsKey(id)) {
			LOGGER.warn("Duplicate report reloader id found, id={}, oldClass={}, newClass={}.", id,
					reloaders.get(id).getClass().getName(), reloader.getClass().getName());
		}
		reloaders.put(id, reloader);
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();

			try {
				for (Entry<String, ReportReloader> entry : reportReloaders.entrySet()) {
					String type = entry.getKey();
					List<Date> dates = reportReloadConfigManager.queryByReportType(type);

					LOGGER.info("Report reload cycle found configured dates, type={}, dateCount={}.", type,
							dates == null ? 0 : dates.size());
					for (Date date : dates) {
						ReportReloader reloader = entry.getValue();

						LOGGER.info("Running report reloader, type={}, date={}.", type, date);
						reloader.reload(date.getTime());
					}
				}
			} catch (Exception e) {
				LOGGER.error("Report reload cycle failed.", e);
				Cat.logError(e);
			}
			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				LOGGER.warn("Report reload task interrupted.");
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {

	}

	public void setConfigManager(ReportReloadConfigManager configManager) {
		reportReloadConfigManager = configManager;
	}

	public void setReloaders(Map<String, ReportReloader> reloaders) {
		reportReloaders = reloaders;
	}
}
