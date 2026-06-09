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
package com.dianping.cat.report.task.current;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;

public class CurrentWeeklyMonthlyReportTask implements Task {
	private static final Logger LOGGER = LoggerFactory.getLogger(CurrentWeeklyMonthlyReportTask.class);

	private static CurrentWeeklyMonthlyReportTask m_instance = new CurrentWeeklyMonthlyReportTask();

	private List<CurrentWeeklyMonthlyTask> m_tasks = new ArrayList<CurrentWeeklyMonthlyTask>();

	private List<String> m_domains;

	public static CurrentWeeklyMonthlyReportTask getInstance() {
		return m_instance;
	}

	@Override
	public String getName() {
		return "Cached-Report-Task";
	}

	public void register(CurrentWeeklyMonthlyTask handler) {
		synchronized (this) {
			m_tasks.add(handler);
			LOGGER.info("Registered current weekly/monthly report task, reportName={}, taskCount={}.",
					handler.getReportName(), m_tasks.size());
		}
	}

	private void reloadCurrentMonthly() {
		for (String domain : m_domains) {
			Transaction t = Cat.newTransaction("ReloadTask", "Reload-Month-" + domain);

			for (CurrentWeeklyMonthlyTask task : m_tasks) {
				try {
					LOGGER.info("Reloading current monthly report, reportName={}, domain={}.", task.getReportName(),
							domain);
					task.buildCurrentMonthlyTask(task.getReportName(), domain, TimeHelper.getCurrentMonth());
				} catch (Exception e) {
					LOGGER.error("Unable to reload current monthly report, reportName={}, domain={}.",
							task.getReportName(), domain, e);
					Cat.logError(e);
				}
			}

			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
	}

	private void reloadCurrentWeekly() {
		for (String domain : m_domains) {
			Transaction t = Cat.newTransaction("ReloadTask", "Reload-Week-" + domain);

			for (CurrentWeeklyMonthlyTask task : m_tasks) {
				try {
					LOGGER.info("Reloading current weekly report, reportName={}, domain={}.", task.getReportName(),
							domain);
					task.buildCurrentWeeklyTask(task.getReportName(), domain, TimeHelper.getCurrentWeek());
				} catch (Exception e) {
					LOGGER.error("Unable to reload current weekly report, reportName={}, domain={}.", task.getReportName(),
							domain, e);
					Cat.logError(e);
				}
			}

			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
	}

	@Override
	public void run() {
		LOGGER.info("Current weekly/monthly reload task started, domainCount={}, taskCount={}.",
				m_domains == null ? 0 : m_domains.size(), m_tasks.size());
		reloadCurrentWeekly();
		reloadCurrentMonthly();
		LOGGER.info("Current weekly/monthly reload task finished.");
	}

	public void setDomains(List<String> domains) {
		m_domains = domains;
	}

	@Override
	public void shutdown() {
	}

	public static interface CurrentWeeklyMonthlyTask {
		public void buildCurrentMonthlyTask(String name, String domain, Date start);

		public void buildCurrentWeeklyTask(String name, String domain, Date start);

		public String getReportName();
	}

}
