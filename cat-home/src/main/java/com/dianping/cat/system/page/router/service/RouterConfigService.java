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
package com.dianping.cat.system.page.router.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.mybatis.data.DailyReportContentDO;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;
import com.dianping.cat.system.page.router.config.RouterConfigManager;

@Component
public class RouterConfigService extends AbstractReportService<RouterConfig> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RouterConfigService.class);

	@Resource
	private RouterConfigManager routerConfigManager;

	@Override
	public RouterConfig makeReport(String domain, Date start, Date end) {
		return null;
	}

	@Override
	public RouterConfig queryDailyReport(String domain, Date start, Date end) {
		long time = start.getTime();
		Map<Long, Pair<RouterConfig, Long>> routerConfigs = routerConfigManager.getRouterConfigs();
		Pair<RouterConfig, Long> pair = routerConfigs.get(time);

		if (pair == null) {
			String name = Constants.REPORT_ROUTER;

			try {
				DailyReportDO report = dailyReportRepository.findByDomainNamePeriod(domain, name, start);
				RouterConfig config = queryFromDailyBinary(report.getId());

				routerConfigs.put(time, Pair.of(config, report.getCreateTime().getTime()));
				return config;
			} catch (EmptyResultDataAccessException e) {
				// ignore
			} catch (Exception e) {
				LOGGER.error("Unable to query daily router config report, domain={}, start={}, end={}.", domain, start,
				      end, e);
				Cat.logError(e);
			}
			return null;
		} else {
			return pair.getKey();
		}
	}

	private RouterConfig queryFromDailyBinary(long id) {
		DailyReportContentDO content = dailyReportContentRepository.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return null;
		}
	}

	@Override
	public RouterConfig queryHourlyReport(String domain, Date start, Date end) {
		throw new RuntimeException("router report don't support hourly report");
	}

	public RouterConfig queryLastReport(String domain) {
		try {
			List<DailyReportDO> reports = dailyReportRepository
									.queryLatestReportsByDomainName(domain, Constants.REPORT_ROUTER, 1);

			if (reports.size() == 0) {
				return null;
			}

			DailyReportDO report = reports.get(0);
			RouterConfig config = queryFromDailyBinary(report.getId());

			return config;
		} catch (EmptyResultDataAccessException e) {
			// ignore
		} catch (Exception e) {
			LOGGER.error("Unable to query latest router config report, domain={}.", domain, e);
			Cat.logError(e);
		}
		return null;
	}

	@Override
	public RouterConfig queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("router report don't support monthly report");
	}

	@Override
	public RouterConfig queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("router report don't support weekly report");
	}

	public void setRouterConfigManager(RouterConfigManager routerConfigManager) {
		this.routerConfigManager = routerConfigManager;
	}

}
