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
import com.dianping.cat.analysis.ContainerMessageAnalyzerFactory;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.storage.StorageReportUpdater.StorageUpdateItem;
import com.dianping.cat.consumer.storage.builder.StorageBuilder;
import com.dianping.cat.consumer.storage.builder.StorageItem;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(ContainerMessageAnalyzerFactory.ANALYZER_BEAN_PREFIX + StorageAnalyzer.ID)
@Scope("prototype")
public class StorageAnalyzer extends AbstractMessageAnalyzer<StorageReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageAnalyzer.class);

	public static final String ID = "storage";

	@Resource(name = StorageAnalyzer.ID + "ReportManager")
	private ReportManager<StorageReport> storageReportManager;

	@Resource(name = "databaseParser")
	private DatabaseParser databaseParser;

	@Resource(name = "storageReportUpdater")
	private StorageReportUpdater storageReportUpdater;

	@Resource
	private List<StorageBuilder> storageBuilderList = Collections.emptyList();

	private Map<String, StorageBuilder> storageBuilders;

	private volatile boolean initialized;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			storageReportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
			databaseParser.showErrorCon();
		} else {
			storageReportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public StorageReport getReport(String id) {
		long period = getStartTime();
		StorageReport report = storageReportManager.getHourlyReport(period, id, false);

		storageReportUpdater.updateStorageIds(id, storageReportManager.getDomains(period), report);
		return report;
	}

	@Override
	public ReportManager<StorageReport> getReportManager() {
		return storageReportManager;
	}

	public void setDatabaseParser(DatabaseParser databaseParser) {
		this.databaseParser = databaseParser;
	}

	public void setReportManager(ReportManager<StorageReport> reportManager) {
		storageReportManager = reportManager;
	}

	public void setUpdater(StorageReportUpdater updater) {
		storageReportUpdater = updater;
	}

	public void setStorageBuilders(Map<String, StorageBuilder> storageBuilders) {
		this.storageBuilders = storageBuilders;
	}

	@Override
	@Resource(name = "serverConfigManager")
	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		super.setServerConfigManager(serverConfigManager);
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	private Map<String, StorageBuilder> buildStorageBuilders(List<StorageBuilder> builders) {
		Map<String, StorageBuilder> result = new LinkedHashMap<String, StorageBuilder>();

		if (builders == null || builders.isEmpty()) {
			return result;
		}
		for (StorageBuilder builder : builders) {
			if (builder == null) {
				continue;
			}
			String type = builder.getType();

			if (type == null || type.length() == 0) {
				LOGGER.warn("Ignore storage builder without type, builderClass={}.", builder.getClass().getName());
				continue;
			}
			StorageBuilder previous = result.put(type, builder);

			if (previous != null) {
				LOGGER.warn("Duplicate storage builder type detected, type={}, previousClass={}, currentClass={}.", type,
				      previous.getClass().getName(), builder.getClass().getName());
			}
		}
		return result;
	}

	public synchronized void initialize() {
		if (initialized) {
			return;
		}
		if (storageBuilders == null) {
			storageBuilders = buildStorageBuilders(storageBuilderList);
		} else {
			storageBuilders = new LinkedHashMap<String, StorageBuilder>(storageBuilders);
		}
		if (storageBuilders.isEmpty()) {
			LOGGER.warn("Storage analyzer has no configured builders, keep empty builder map.");
		}
		initialized = true;
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
		storageReportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	protected void process(MessageTree tree) {
		ensureInitialized();

		List<Transaction> transactions = tree.getTransactions();

		for (Transaction t : transactions) {
			String domain = tree.getDomain();
			Collection<StorageBuilder> builders = storageBuilders.values();

			for (StorageBuilder builder : builders) {
				if (builder.isEligable(t)) {
					StorageItem item = builder.build(t);
					String id = item.getId();

					if (StringUtils.isNotEmpty(id)) {
						StorageReport report = storageReportManager.getHourlyReport(getStartTime(), item.getReportId(), true);
						StorageUpdateItem param = new StorageUpdateItem();

						param.setDomain(domain).setIp(item.getIp()).setMethod(item.getMethod()).setTransaction(t)
												.setThreshold(item.getThreshold());
						storageReportUpdater.updateStorageReport(report, param);
					}
				}
			}
		}
	}

}
