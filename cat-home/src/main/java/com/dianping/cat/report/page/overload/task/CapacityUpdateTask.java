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

import java.util.Date;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.report.task.TaskBuilder;

@Named(type = TaskBuilder.class, value = CapacityUpdateTask.ID)
public class CapacityUpdateTask implements TaskBuilder, LogEnabled {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(CapacityUpdateTask.class);

	public static final String ID = Constants.REPORT_DATABASE_CAPACITY;

	protected Logger m_logger;

	@Inject(type = CapacityUpdater.class, value = HourlyCapacityUpdater.ID)
	private CapacityUpdater m_hourlyUpdater;

	@Inject(type = CapacityUpdater.class, value = DailyCapacityUpdater.ID)
	private CapacityUpdater m_dailyUpdater;

	@Inject(type = CapacityUpdater.class, value = WeeklyCapacityUpdater.ID)
	private CapacityUpdater m_weeklyUpdater;

	@Inject(type = CapacityUpdater.class, value = MonthlyCapacityUpdater.ID)
	private CapacityUpdater m_monthlyUpdater;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			SLF4J_LOGGER.info("Starting daily capacity update task, name={}, domain={}, period={}.", name, domain, period);
			m_dailyUpdater.updateDBCapacity();
			SLF4J_LOGGER.info("Finished daily capacity update task, name={}, domain={}, period={}.", name, domain, period);
			return true;
		} catch (DalException e) {
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
			m_hourlyUpdater.updateDBCapacity();
			SLF4J_LOGGER.info("Finished hourly capacity update task, name={}, domain={}, period={}.", name, domain, period);
			return true;
		} catch (DalException e) {
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
			m_monthlyUpdater.updateDBCapacity();
			SLF4J_LOGGER.info("Finished monthly capacity update task, name={}, domain={}, period={}.", name, domain,
			      period);
			return true;
		} catch (DalException e) {
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
			m_weeklyUpdater.updateDBCapacity();
			SLF4J_LOGGER.info("Finished weekly capacity update task, name={}, domain={}, period={}.", name, domain, period);
			return true;
		} catch (DalException e) {
			SLF4J_LOGGER.error("Unable to build weekly capacity update task, name={}, domain={}, period={}.", name,
			      domain, period, e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public void setDailyUpdater(CapacityUpdater dailyUpdater) {
		m_dailyUpdater = dailyUpdater;
	}

	public void setHourlyUpdater(CapacityUpdater hourlyUpdater) {
		m_hourlyUpdater = hourlyUpdater;
	}

	public void setMonthlyUpdater(CapacityUpdater monthlyUpdater) {
		m_monthlyUpdater = monthlyUpdater;
	}

	public void setWeeklyUpdater(CapacityUpdater weeklyUpdater) {
		m_weeklyUpdater = weeklyUpdater;
	}

}
