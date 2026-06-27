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
package com.dianping.cat.report.task.cmdb;

import java.util.Date;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.dianping.cat.support.Threads;

import com.dianping.cat.Constants;
import com.dianping.cat.report.task.TaskBuilder;

@Component(CmdbInfoReloadBuilder.ID)
public class CmdbInfoReloadBuilder implements TaskBuilder {

	public static final String ID = Constants.CMDB;

	private static final Logger LOGGER = LoggerFactory.getLogger(CmdbInfoReloadBuilder.class);

	@Resource
	private ProjectUpdateTask projectUpdateTask;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		throw new RuntimeException("project builder don't support hourly task");
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		if (projectUpdateTask == null) {
			LOGGER.warn("Project update task is not available, skip cmdb reload task, name={}, domain={}, period={}.",
					name, domain, period);
			return false;
		}
		Threads.forGroup(Constants.CAT).start(projectUpdateTask);
		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("project builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("project builder don't support weekly task");
	}

	public void setProjectUpdateTask(ProjectUpdateTask projectUpdateTask) {
		this.projectUpdateTask = projectUpdateTask;
	}

}
