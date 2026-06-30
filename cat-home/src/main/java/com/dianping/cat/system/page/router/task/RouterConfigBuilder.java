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
package com.dianping.cat.system.page.router.task;

import java.util.Date;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.transform.DefaultNativeBuilder;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.system.page.router.config.RouterConfigAdjustor;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.service.RouterConfigService;

@Component(RouterConfigBuilder.ID)
public class RouterConfigBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_ROUTER;

	@Resource
	private RouterConfigHandler routerConfigHandler;

	@Resource
	private RouterConfigAdjustor routerConfigAdjustor;

	@Resource
	private RouterConfigService reportService;

	@Resource
	private ServerConfigManager serverConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		RouterConfig routerConfig = routerConfigHandler.buildRouterConfig(domain, period);
		DailyReportDO dailyReport = new DailyReportDO();

		dailyReport.setCreateTime(new Date());
		dailyReport.setDomain(domain);
		dailyReport.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		dailyReport.setName(name);
		dailyReport.setPeriod(period);
		dailyReport.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(routerConfig);

		reportService.insertDailyReport(dailyReport, binaryContent);
		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		if (serverConfigManager.isRouterAdjustEnabled()) {
			routerConfigAdjustor.Adjust(period);
		}
		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder doesn't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder doesn't support weekly task");
	}

	public void setReportService(RouterConfigService reportService) {
		this.reportService = reportService;
	}

	public void setRouterAdjustor(RouterConfigAdjustor routerAdjustor) {
		this.routerConfigAdjustor = routerAdjustor;
	}

	public void setRouterConfigHandler(RouterConfigHandler routerConfigHandler) {
		this.routerConfigHandler = routerConfigHandler;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		this.serverConfigManager = serverConfigManager;
	}
}
