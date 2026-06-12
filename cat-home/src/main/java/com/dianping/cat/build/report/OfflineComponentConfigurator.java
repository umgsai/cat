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
package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.core.mybatis.repository.daily.report.content.DailyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourly.report.content.HourlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourlyreport.HourlyReportRepository;
import com.dianping.cat.core.mybatis.repository.monthly.report.content.MonthlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.monthreport.MonthlyReportRepository;
import com.dianping.cat.core.mybatis.repository.overload.OverloadRepository;
import com.dianping.cat.core.mybatis.repository.weekly.report.content.WeeklyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.weeklyreport.WeeklyReportRepository;
import com.dianping.cat.core.report.daily.repository.DailyReportRepository;
import com.dianping.cat.report.page.overload.task.CapacityUpdateStatusManager;
import com.dianping.cat.report.page.overload.task.CapacityUpdater;
import com.dianping.cat.report.page.overload.task.DailyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.HourlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.MonthlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.TableCapacityService;
import com.dianping.cat.report.page.overload.task.WeeklyCapacityUpdater;

public class OfflineComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(CapacityUpdateStatusManager.class));
		all.add(C(CapacityUpdater.class, HourlyCapacityUpdater.ID, HourlyCapacityUpdater.class) //
								.req(HourlyReportContentRepository.class, (String) null, "m_hourlyReportContentDao") //
								.req(HourlyReportRepository.class, (String) null, "m_hourlyReportDao") //
								.req(OverloadRepository.class, (String) null, "m_overloadDao") //
								.req(CapacityUpdateStatusManager.class, (String) null, "m_manager"));
		all.add(C(CapacityUpdater.class, DailyCapacityUpdater.ID, DailyCapacityUpdater.class) //
								.req(DailyReportContentRepository.class, (String) null, "m_dailyReportContentDao") //
								.req(DailyReportRepository.class, (String) null, "m_dailyReportDao") //
								.req(OverloadRepository.class, (String) null, "m_overloadDao") //
								.req(CapacityUpdateStatusManager.class, (String) null, "m_manager"));
		all.add(C(CapacityUpdater.class, WeeklyCapacityUpdater.ID, WeeklyCapacityUpdater.class) //
								.req(WeeklyReportRepository.class, (String) null, "m_weeklyReportDao") //
								.req(WeeklyReportContentRepository.class, (String) null, "m_weeklyReportContentDao") //
								.req(OverloadRepository.class, (String) null, "m_overloadDao") //
								.req(CapacityUpdateStatusManager.class, (String) null, "m_manager"));
		all.add(C(CapacityUpdater.class, MonthlyCapacityUpdater.ID, MonthlyCapacityUpdater.class) //
								.req(MonthlyReportRepository.class, (String) null, "m_monthlyReportDao") //
								.req(MonthlyReportContentRepository.class, (String) null, "m_monthlyReportContentDao") //
								.req(OverloadRepository.class, (String) null, "m_overloadDao") //
								.req(CapacityUpdateStatusManager.class, (String) null, "m_manager"));
		all.add(C(TableCapacityService.class) //
								.req(OverloadRepository.class, (String) null, "m_overloadDao") //
								.req(HourlyReportRepository.class, (String) null, "m_hourlyReportDao") //
								.req(DailyReportRepository.class, (String) null, "m_dailyReportDao") //
								.req(WeeklyReportRepository.class, (String) null, "m_weeklyReportDao") //
								.req(MonthlyReportRepository.class, (String) null, "m_monthlyReportDao"));

		return all;
	}
}
