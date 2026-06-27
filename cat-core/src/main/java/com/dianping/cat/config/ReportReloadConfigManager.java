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
package com.dianping.cat.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;
import com.dianping.cat.configuration.reload.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Component
public class ReportReloadConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportReloadConfigManager.class);

	private static final String CONFIG_NAME = "report-reload-config";

	private static final String DEFAULT = "default";

	@Resource
	protected ConfigRepository configRepository;

	@Resource
	protected ContentFetcher contentFetcher;

	private long configId;

	private long modifyTime;

	private ReportReloadConfig config;

	private volatile boolean initialized;

	public ReportReloadConfig getConfig() {
		ensureInitialized();
		return config;
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	@PostConstruct
	public synchronized void initialize() {
		if (initialized) {
			return;
		}

		try {
			Config config = configRepository.findByName(CONFIG_NAME);
			String content = config.getContent();

			configId = config.getId();
			modifyTime = config.getModifyDate().getTime();
			this.config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded report reload config from repository, configId={}, modifyTime={}.", configId,
					modifyTime);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Report reload config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				Config config = configRepository.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				configRepository.insert(config);
				configId = config.getId();
				this.config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized report reload config from default content, configId={}.", configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize report reload config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load report reload config from repository.", e);
			Cat.logError(e);
		}
		if (config == null) {
			config = new ReportReloadConfig();
			LOGGER.warn("Report reload config is empty after initialization, using a new empty config.");
		}

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return CONFIG_NAME;
			}

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}
		});
		initialized = true;
	}

	public boolean insert(String xml) {
		ensureInitialized();

		try {
			config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			LOGGER.error("Unable to parse report reload config xml for insert. xmlLength={}.",
					xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public List<Date> queryByReportType(String type) {
		ensureInitialized();

		ReportType reportType = config.findReportType(type);
		ArrayList<Date> results = new ArrayList<Date>();

		if (reportType == null) {
			reportType = config.findReportType(DEFAULT);
		}

		if (reportType != null) {
			List<ReportPeriod> reportPeriods = reportType.getReportPeriods();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");

			for (ReportPeriod rp : reportPeriods) {
				try {
					Date period = sdf.parse(rp.getId());

					results.add(period);
				} catch (ParseException e) {
					LOGGER.warn("Unable to parse report reload period, type={}, periodId={}.", type, rp.getId(), e);
					Cat.logError(e);
				}
			}
		}

		return results;
	}

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	private void refreshConfig() throws Exception {
		Config config = configRepository.findByName(CONFIG_NAME);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > this.modifyTime) {
				String content = config.getContent();
				ReportReloadConfig reportReloadConfig = DefaultSaxParser.parse(content);

				this.config = reportReloadConfig;
				this.modifyTime = modifyTime;
				LOGGER.info("Refreshed report reload config, configId={}, modifyTime={}.", configId, this.modifyTime);
			}
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = configRepository.createLocal();

				config.setId(configId);
				config.setKeyId(configId);
				config.setName(CONFIG_NAME);
				config.setContent(this.config.toString());
				configRepository.updateByPK(config);
				LOGGER.info("Stored report reload config, configId={}.", configId);
			} catch (Exception e) {
				LOGGER.error("Unable to store report reload config, configId={}.", configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
}
