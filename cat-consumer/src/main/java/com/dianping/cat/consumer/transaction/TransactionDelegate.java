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
package com.dianping.cat.consumer.transaction;

import com.dianping.cat.Constants;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.spring.CatSpringContext;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

import java.util.Date;
import java.util.Map;

public class TransactionDelegate implements ReportDelegate<TransactionReport> {

	private TaskManager m_taskManager;

	private ServerFilterConfigManager m_configManager;

	private AllReportConfigManager m_transactionManager;

	private ServerConfigManager m_serverConfigManager;

	private AtomicMessageConfigManager m_atomicMessageConfigManager;

	private TransactionStatisticsComputer m_computer = new TransactionStatisticsComputer();

	@Override
	public void afterLoad(Map<String, TransactionReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, TransactionReport> reports) {
	}

	@Override
	public byte[] buildBinary(TransactionReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(TransactionReport report) {
		refreshSpringBeans();

		report.accept(m_computer);

		new TransactionReportCountFilter(m_serverConfigManager.getMaxTypeThreshold(),
								m_atomicMessageConfigManager.getMaxNameThreshold(report.getDomain()),
								m_serverConfigManager.getTypeNameLengthLimit()).visitTransactionReport(report);

		return report.toString();
	}

	@Override
	public boolean createHourlyTask(TransactionReport report) {
		refreshSpringBeans();

		String domain = report.getDomain();

		if (domain.equals(Constants.ALL) || m_configManager.validateDomain(domain)) {
			return m_taskManager.createTask(report.getStartTime(), domain, TransactionAnalyzer.ID,
			      TaskProlicy.ALL_EXCLUED_HOURLY);
		} else {
			return true;
		}
	}

	@Override
	public String getDomain(TransactionReport report) {
		return report.getDomain();
	}

	@Override
	public TransactionReport makeReport(String domain, long startTime, long duration) {
		TransactionReport report = new TransactionReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public TransactionReport mergeReport(TransactionReport old, TransactionReport other) {
		TransactionReportMerger merger = new TransactionReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public TransactionReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public TransactionReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}

	private void refreshSpringBeans() {
		TaskManager taskManager = CatSpringContext.getBeanIfAvailable(TaskManager.class);
		ServerFilterConfigManager configManager = CatSpringContext.getBeanIfAvailable(ServerFilterConfigManager.class);
		AllReportConfigManager transactionManager = CatSpringContext.getBeanIfAvailable(AllReportConfigManager.class);
		ServerConfigManager serverConfigManager = CatSpringContext.getBeanIfAvailable(ServerConfigManager.class);
		AtomicMessageConfigManager atomicMessageConfigManager = CatSpringContext
		      .getBeanIfAvailable(AtomicMessageConfigManager.class);

		if (taskManager != null) {
			m_taskManager = taskManager;
		}
		if (configManager != null) {
			m_configManager = configManager;
		}
		if (transactionManager != null) {
			m_transactionManager = transactionManager;
		}
		if (serverConfigManager != null) {
			m_serverConfigManager = serverConfigManager;
		}
		if (atomicMessageConfigManager != null) {
			m_atomicMessageConfigManager = atomicMessageConfigManager;
		}
	}

	public void setTaskManager(TaskManager taskManager) {
		m_taskManager = taskManager;
	}

	public void setConfigManager(ServerFilterConfigManager configManager) {
		m_configManager = configManager;
	}

	public void setTransactionManager(AllReportConfigManager transactionManager) {
		m_transactionManager = transactionManager;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		m_serverConfigManager = serverConfigManager;
	}

	public void setAtomicMessageConfigManager(AtomicMessageConfigManager atomicMessageConfigManager) {
		m_atomicMessageConfigManager = atomicMessageConfigManager;
	}
}
