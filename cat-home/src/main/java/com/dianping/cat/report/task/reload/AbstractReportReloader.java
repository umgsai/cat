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
package com.dianping.cat.report.task.reload;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.cat.core.dal.jdbc.DalException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.mybatis.repository.hourly.report.content.HourlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourlyreport.HourlyReportRepository;

public abstract class AbstractReportReloader implements ReportReloader {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReportReloader.class);

	protected HourlyReportRepository m_hourlyReportDao;

	protected HourlyReportContentRepository m_hourlyReportContentDao;

	protected ServerConfigManager m_serverConfigManager;

	protected int getAnalyzerCount() {
		return m_serverConfigManager.getThreadsOfRealtimeAnalyzer(getId());
	}

	public boolean insertHourlyReport(ReportReloadEntity entity) {
		try {
			HourlyReport report = entity.getReport();
			m_hourlyReportDao.insert(report);

			int id = report.getId();
			HourlyReportContent proto = m_hourlyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(entity.getReportContent());
			proto.setPeriod(report.getPeriod());
			m_hourlyReportContentDao.insert(proto);
			return true;
		} catch (DalException e) {
			HourlyReport report = entity == null ? null : entity.getReport();
			LOGGER.error("Unable to insert reloaded hourly report, reloader={}, reportName={}, domain={}, period={}.",
					getId(), report == null ? null : report.getName(), report == null ? null : report.getDomain(),
					report == null ? null : report.getPeriod(), e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean reload(long time) {
		try {
			LOGGER.info("Reloading hourly reports, reloader={}, time={}.", getId(), time);
			List<ReportReloadEntity> reports = loadReport(time);

			LOGGER.info("Loaded reports for reload, reloader={}, time={}, reportCount={}.", getId(), time,
					reports == null ? 0 : reports.size());
			for (ReportReloadEntity entity : reports) {
				insertHourlyReport(entity);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to reload hourly reports, reloader={}, time={}.", getId(), time, e);
			Cat.logError(e);
		}
		return true;
	}

	public void setHourlyReportContentDao(HourlyReportContentRepository hourlyReportContentDao) {
		m_hourlyReportContentDao = hourlyReportContentDao;
	}

	public void setHourlyReportDao(HourlyReportRepository hourlyReportDao) {
		m_hourlyReportDao = hourlyReportDao;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		m_serverConfigManager = serverConfigManager;
	}

}
