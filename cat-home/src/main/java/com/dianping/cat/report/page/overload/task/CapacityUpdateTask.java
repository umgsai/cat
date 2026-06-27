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

import jakarta.annotation.Resource;

import java.util.Date;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.report.task.TaskBuilder;

@Component(CapacityUpdateTask.ID)
public class CapacityUpdateTask implements TaskBuilder {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(CapacityUpdateTask.class);

	public static final String ID = Constants.REPORT_DATABASE_CAPACITY;

	@Resource(name = HourlyCapacityUpdater.ID)
	private CapacityUpdater hourlyCapacityUpdater;

	@Resource(name = DailyCapacityUpdater.ID)
	private CapacityUpdater dailyCapacityUpdater;

	@Resource(name = WeeklyCapacityUpdater.ID)
	private CapacityUpdater weeklyCapacityUpdater;

	@Resource(name = MonthlyCapacityUpdater.ID)
	private CapacityUpdater monthlyCapacityUpdater;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			SLF4J_LOGGER.info("Starting daily capacity update task, name={}, domain={}, period={}.", name, domain, period);
			dailyCapacityUpdater.updateDBCapacity();
			SLF4J_LOGGER.info("Finished daily capacity update task, name={}, domain={}, period={}.", name, domain, period);
			return true;
		} catch (RuntimeException e) {
			SLF4J_LOGGER.error("Unable to build daily capacity update task, name={}, domain={}, period={}.", name,
			      domain, period, e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		try {
			SLF4J_LOGGER.info("Starting hourly capacity update task, name={}, domain={}, period={}.", name, domain, period);
			hourlyCapacityUpdater.updateDBCapacity();
			SLF4J_LOGGER.info("Finished hourly capacity update task, name={}, domain={}, period={}.", name, domain, period);
			return true;
		} catch (RuntimeException e) {
			SLF4J_LOGGER.error("Unable to build hourly capacity update task, name={}, domain={}, period={}.", name,
			      domain, period, e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		try {
			SLF4J_LOGGER.info("Starting monthly capacity update task, name={}, domain={}, period={}.", name, domain,
			      period);
			monthlyCapacityUpdater.updateDBCapacity();
			SLF4J_LOGGER.info("Finished monthly capacity update task, name={}, domain={}, period={}.", name, domain,
			      period);
			return true;
		} catch (RuntimeException e) {
			SLF4J_LOGGER.error("Unable to build monthly capacity update task, name={}, domain={}, period={}.", name,
			      domain, period, e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		try {
			SLF4J_LOGGER.info("Starting weekly capacity update task, name={}, domain={}, period={}.", name, domain, period);
			weeklyCapacityUpdater.updateDBCapacity();
			SLF4J_LOGGER.info("Finished weekly capacity update task, name={}, domain={}, period={}.", name, domain, period);
			return true;
		} catch (RuntimeException e) {
			SLF4J_LOGGER.error("Unable to build weekly capacity update task, name={}, domain={}, period={}.", name,
			      domain, period, e);
			Cat.logError(e);
			return false;
		}
	}

	public void setDailyUpdater(CapacityUpdater dailyUpdater) {
		this.dailyCapacityUpdater = dailyUpdater;
	}

	public void setHourlyUpdater(CapacityUpdater hourlyUpdater) {
		this.hourlyCapacityUpdater = hourlyUpdater;
	}

	public void setMonthlyUpdater(CapacityUpdater monthlyUpdater) {
		this.monthlyCapacityUpdater = monthlyUpdater;
	}

	public void setWeeklyUpdater(CapacityUpdater weeklyUpdater) {
		this.weeklyCapacityUpdater = weeklyUpdater;
	}

}
