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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.cat.core.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;
import com.dianping.cat.configuration.reload.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

public class ReportReloadConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportReloadConfigManager.class);

	private static final String CONFIG_NAME = "report-reload-config";

	private static final String DEFAULT = "default";

	protected ConfigRepository m_configDao;

	protected ContentFetcher m_fetcher;

	private int m_configId;

	private long m_modifyTime;

	private ReportReloadConfig m_config;

	private volatile boolean m_initialized;

	public ReportReloadConfig getConfig() {
		ensureInitialized();
		return m_config;
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

		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
			m_config = DefaultSaxParser.parse(content);
			LOGGER.info("Loaded report reload config from repository, configId={}, modifyTime={}.", m_configId,
					m_modifyTime);
		} catch (DalNotFoundException e) {
			LOGGER.warn("Report reload config is missing in repository, loading default content from fetcher.", e);

			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
				LOGGER.info("Initialized report reload config from default content, configId={}.", m_configId);
			} catch (Exception ex) {
				LOGGER.error("Unable to initialize report reload config from default content.", ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load report reload config from repository.", e);
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new ReportReloadConfig();
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
		m_initialized = true;
	}

	public boolean insert(String xml) {
		ensureInitialized();

		try {
			m_config = DefaultSaxParser.parse(xml);

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

		ReportType reportType = m_config.findReportType(type);
		ArrayList<Date> results = new ArrayList<Date>();

		if (reportType == null) {
			reportType = m_config.findReportType(DEFAULT);
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
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				ReportReloadConfig reportReloadConfig = DefaultSaxParser.parse(content);

				m_config = reportReloadConfig;
				m_modifyTime = modifyTime;
				LOGGER.info("Refreshed report reload config, configId={}, modifyTime={}.", m_configId, m_modifyTime);
			}
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
				LOGGER.info("Stored report reload config, configId={}.", m_configId);
			} catch (Exception e) {
				LOGGER.error("Unable to store report reload config, configId={}.", m_configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
}
