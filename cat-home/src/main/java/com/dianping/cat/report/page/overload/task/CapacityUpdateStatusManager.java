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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.mybatis.OverloadRepository;

@Component
public class CapacityUpdateStatusManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CapacityUpdateStatusManager.class);

	private static final String CONFIG_NAME = "capacityUpdateStatus";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private OverloadRepository overloadRepository;

	private long hourlyStatus;

	private long dailyStatus;

	private long weeklyStatus;

	private long monthlyStatus;

	private long configId;

	public void setConfigDao(ConfigRepository configDao) {
		this.configRepository = configDao;
	}

	public void setOverloadDao(OverloadRepository overloadDao) {
		this.overloadRepository = overloadDao;
	}

	private String buildConfigContent() {
		StringBuilder builder = new StringBuilder();

		builder.append("Hourly:").append(hourlyStatus).append(";");
		builder.append("Daily:").append(dailyStatus).append(";");
		builder.append("Weekly:").append(weeklyStatus).append(";");
		builder.append("Monthly:").append(monthlyStatus).append(";");
		return builder.toString();
	}

	private void extractStatus(String content) {
		hourlyStatus = Long.parseLong(content.split("Hourly:")[1].split(";")[0]);
		dailyStatus = Long.parseLong(content.split("Daily:")[1].split(";")[0]);
		weeklyStatus = Long.parseLong(content.split("Weekly:")[1].split(";")[0]);
		monthlyStatus = Long.parseLong(content.split("Monthly:")[1].split(";")[0]);
	}

	public long getDailyStatus() {
		return dailyStatus;
	}

	public long getHourlyStatus() {
		return hourlyStatus;
	}

	public long getMonthlyStatus() {
		return monthlyStatus;
	}

	public long getWeeklyStatus() {
		return weeklyStatus;
	}

	@PostConstruct
	public void initialize() {
		try {
			Config config = configRepository.findByName(CONFIG_NAME);
			String content = config.getContent();
			configId = config.getId();

			extractStatus(content);
		} catch (RuntimeException e) {
			LOGGER.warn("Unable to load capacity update status config, will initialize it from overload table.", e);

			try {
				hourlyStatus = overloadRepository.findMaxIdByType(CapacityUpdater.HOURLY_TYPE)
										.getMaxId();
				dailyStatus = overloadRepository.findMaxIdByType(CapacityUpdater.DAILY_TYPE).getMaxId();
				weeklyStatus = overloadRepository.findMaxIdByType(CapacityUpdater.WEEKLY_TYPE)
										.getMaxId();
				monthlyStatus = overloadRepository.findMaxIdByType(CapacityUpdater.MONTHLY_TYPE)
										.getMaxId();

				Config config = configRepository.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(buildConfigContent());
				configRepository.insert(config);

				configId = config.getId();
			} catch (RuntimeException ex) {
				LOGGER.error("Unable to initialize capacity update status config from overload table.", ex);
				Cat.logError(ex);
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
				config.setContent(buildConfigContent());
				configRepository.updateByPK(config);
			} catch (Exception e) {
				LOGGER.error("Unable to store capacity update status config. content={}", buildConfigContent(), e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public void updateDailyStatus(long dailyStatus) {
		this.dailyStatus = dailyStatus;
		storeConfig();
	}

	public void updateHourlyStatus(long hourlyStatus) {
		this.hourlyStatus = hourlyStatus;
		storeConfig();
	}

	public void updateMonthlyStatus(long monthlyStatus) {
		this.monthlyStatus = monthlyStatus;
		storeConfig();
	}

	public void updateWeeklyStatus(long weeklyStatus) {
		this.weeklyStatus = weeklyStatus;
		storeConfig();
	}
}
