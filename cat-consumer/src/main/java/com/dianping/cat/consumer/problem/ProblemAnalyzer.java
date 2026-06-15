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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;

public class ProblemAnalyzer extends AbstractMessageAnalyzer<ProblemReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProblemAnalyzer.class);

	public static final String ID = "problem";

	private ReportManager<ProblemReport> m_reportManager;

	private List<ProblemHandler> m_handlers;

	private volatile boolean m_initialized;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	public Set<String> getDomains() {
		return m_reportManager.getDomains(getStartTime());
	}

	@Override
	public ProblemReport getReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, false);
	}

	@Override
	public ReportManager<ProblemReport> getReportManager() {
		return m_reportManager;
	}

	private void ensureInitialized() {
		if (!m_initialized) {
			initialize();
		}
	}

	public synchronized void initialize() {
		if (m_initialized) {
			return;
		}
		if (m_handlers == null) {
			m_handlers = Collections.emptyList();
			LOGGER.warn("Problem analyzer has no configured handlers, keep empty handler list.");
		} else {
			// Copy the container-provided list before it is read on the hot path.
			m_handlers = new ArrayList<ProblemHandler>(m_handlers);
		}
		m_initialized = true;
	}

	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	public void process(MessageTree tree) {
		ensureInitialized();

		String domain = tree.getDomain();
		ProblemReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);

		report.addIp(tree.getIpAddress());
		Machine machine = report.findOrCreateMachine(tree.getIpAddress());

		for (ProblemHandler handler : m_handlers) {
			handler.handle(machine, tree);
		}
	}

	public void setHandlers(List<ProblemHandler> handlers) {
		m_handlers = handlers;
	}

	public void setReportManager(ReportManager<ProblemReport> reportManager) {
		m_reportManager = reportManager;
	}

}
