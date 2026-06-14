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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.storage.StorageReportUpdater.StorageUpdateItem;
import com.dianping.cat.consumer.storage.builder.StorageBuilder;
import com.dianping.cat.consumer.storage.builder.StorageItem;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.spring.CatSpringContext;

public class StorageAnalyzer extends AbstractMessageAnalyzer<StorageReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageAnalyzer.class);

	public static final String ID = "storage";

	private ReportManager<StorageReport> m_reportManager;

	private DatabaseParser m_databaseParser;

	private StorageReportUpdater m_updater;

	private Map<String, StorageBuilder> m_storageBuilders;

	private volatile boolean m_initialized;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
			m_databaseParser.showErrorCon();
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public StorageReport getReport(String id) {
		long period = getStartTime();
		StorageReport report = m_reportManager.getHourlyReport(period, id, false);

		m_updater.updateStorageIds(id, m_reportManager.getDomains(period), report);
		return report;
	}

	@Override
	public ReportManager<StorageReport> getReportManager() {
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
		refreshSpringBuilders();

		if (m_storageBuilders == null) {
			try {
				m_storageBuilders = lookupMap(StorageBuilder.class);
				LOGGER.info("Loaded storage analyzer builders from Plexus fallback, types={}.",
				      m_storageBuilders.keySet());
			} catch (RuntimeException e) {
				m_storageBuilders = Collections.emptyMap();
				LOGGER.warn("Unable to load storage analyzer builders from Spring or Plexus, keep empty builder map.", e);
			}
		}
		m_initialized = true;
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		if (tree.getTransactions().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	protected void process(MessageTree tree) {
		ensureInitialized();

		List<Transaction> transactions = tree.getTransactions();

		for (Transaction t : transactions) {
			String domain = tree.getDomain();
			Collection<StorageBuilder> builders = m_storageBuilders.values();

			for (StorageBuilder builder : builders) {
				if (builder.isEligable(t)) {
					StorageItem item = builder.build(t);
					String id = item.getId();

					if (StringUtils.isNotEmpty(id)) {
						StorageReport report = m_reportManager.getHourlyReport(getStartTime(), item.getReportId(), true);
						StorageUpdateItem param = new StorageUpdateItem();

						param.setDomain(domain).setIp(item.getIp()).setMethod(item.getMethod()).setTransaction(t)
												.setThreshold(item.getThreshold());
						m_updater.updateStorageReport(report, param);
					}
				}
			}
		}
	}

	private void refreshSpringBuilders() {
		Map<String, StorageBuilder> springBuilders = CatSpringContext.getBeansIfAvailable(StorageBuilder.class);

		if (!springBuilders.isEmpty()) {
			Map<String, StorageBuilder> builders = new LinkedHashMap<String, StorageBuilder>();

			for (StorageBuilder builder : springBuilders.values()) {
				builders.put(builder.getType(), builder);
			}
			m_storageBuilders = builders;
			LOGGER.info("Loaded storage analyzer builders from Spring, types={}.", m_storageBuilders.keySet());
		}
	}

}
