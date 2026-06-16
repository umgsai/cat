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
package com.dianping.cat.report.page.overload.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.mybatis.repository.overload.OverloadRepository;

public class CapacityUpdateStatusManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CapacityUpdateStatusManager.class);

	private static final String CONFIG_NAME = "capacityUpdateStatus";

	private ConfigRepository m_configDao;

	private OverloadRepository m_overloadDao;

	private int m_hourlyStatus;

	private int m_dailyStatus;

	private int m_weeklyStatus;

	private int m_monthlyStatus;

	private int m_configId;

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setOverloadDao(OverloadRepository overloadDao) {
		m_overloadDao = overloadDao;
	}

	private String buildConfigContent() {
		StringBuilder builder = new StringBuilder();

		builder.append("Hourly:").append(m_hourlyStatus).append(";");
		builder.append("Daily:").append(m_dailyStatus).append(";");
		builder.append("Weekly:").append(m_weeklyStatus).append(";");
		builder.append("Monthly:").append(m_monthlyStatus).append(";");
		return builder.toString();
	}

	private void extractStatus(String content) {
		m_hourlyStatus = Integer.parseInt(content.split("Hourly:")[1].split(";")[0]);
		m_dailyStatus = Integer.parseInt(content.split("Daily:")[1].split(";")[0]);
		m_weeklyStatus = Integer.parseInt(content.split("Weekly:")[1].split(";")[0]);
		m_monthlyStatus = Integer.parseInt(content.split("Monthly:")[1].split(";")[0]);
	}

	public int getDailyStatus() {
		return m_dailyStatus;
	}

	public int getHourlyStatus() {
		return m_hourlyStatus;
	}

	public int getMonthlyStatus() {
		return m_monthlyStatus;
	}

	public int getWeeklyStatus() {
		return m_weeklyStatus;
	}

	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME);
			String content = config.getContent();
			m_configId = config.getId();

			extractStatus(content);
		} catch (RuntimeException e) {
			LOGGER.warn("Unable to load capacity update status config, will initialize it from overload table.", e);

			try {
				m_hourlyStatus = m_overloadDao.findMaxIdByType(CapacityUpdater.HOURLY_TYPE)
										.getMaxId();
				m_dailyStatus = m_overloadDao.findMaxIdByType(CapacityUpdater.DAILY_TYPE).getMaxId();
				m_weeklyStatus = m_overloadDao.findMaxIdByType(CapacityUpdater.WEEKLY_TYPE)
										.getMaxId();
				m_monthlyStatus = m_overloadDao.findMaxIdByType(CapacityUpdater.MONTHLY_TYPE)
										.getMaxId();

				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(buildConfigContent());
				m_configDao.insert(config);

				m_configId = config.getId();
			} catch (RuntimeException ex) {
				LOGGER.error("Unable to initialize capacity update status config from overload table.", ex);
				Cat.logError(ex);
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
				config.setContent(buildConfigContent());
				m_configDao.updateByPK(config);
			} catch (Exception e) {
				LOGGER.error("Unable to store capacity update status config. content={}", buildConfigContent(), e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public void updateDailyStatus(int dailyStatus) {
		this.m_dailyStatus = dailyStatus;
		storeConfig();
	}

	public void updateHourlyStatus(int hourlyStatus) {
		this.m_hourlyStatus = hourlyStatus;
		storeConfig();
	}

	public void updateMonthlyStatus(int monthlyStatus) {
		this.m_monthlyStatus = monthlyStatus;
		storeConfig();
	}

	public void updateWeeklyStatus(int weeklyStatus) {
		this.m_weeklyStatus = weeklyStatus;
		storeConfig();
	}
}
