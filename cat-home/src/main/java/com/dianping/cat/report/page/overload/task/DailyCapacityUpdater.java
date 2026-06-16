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

import java.util.List;

import com.dianping.cat.core.dal.jdbc.DalException;
import com.dianping.cat.core.dal.jdbc.DalNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.mybatis.repository.daily.report.content.DailyReportContentRepository;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.report.daily.repository.DailyReportRepository;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.core.mybatis.repository.overload.OverloadRepository;

public class DailyCapacityUpdater implements CapacityUpdater {
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyCapacityUpdater.class);

	public static final String ID = "daily_capacity_updater";

	private DailyReportContentRepository m_dailyReportContentDao;

	private DailyReportRepository m_dailyReportDao;

	private OverloadRepository m_overloadDao;

	private CapacityUpdateStatusManager m_manager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity() throws DalException {
		int maxId = m_manager.getDailyStatus();
		LOGGER.info("Starting daily report capacity scan, startMaxId={}.", maxId);

		while (true) {
			List<DailyReportContent> reports = m_dailyReportContentDao
									.findOverloadReport(maxId,	DailyReportContentEntity.READSET_LENGTH);

			for (DailyReportContent content : reports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();

					if (contentLength >= CapacityUpdater.CAPACITY) {
						Overload overload = m_overloadDao.createLocal();

						overload.setReportId(reportId);
						overload.setReportSize(contentLength);
						overload.setReportType(CapacityUpdater.DAILY_TYPE);

						try {
							DailyReport report = m_dailyReportDao.findByPK(reportId, DailyReportEntity.READSET_FULL);
							overload.setPeriod(report.getPeriod());
							m_overloadDao.insert(overload);
						} catch (DalNotFoundException e) {
							LOGGER.warn("Daily report not found while recording overload report, reportId={}.", reportId);
						} catch (Exception e) {
							LOGGER.error("Unable to record daily overload report, reportId={}, contentLength={}.",
							      reportId, contentLength, e);
							Cat.logError(e);
						}
					}
				} catch (Exception ex) {
					LOGGER.error("Unable to process daily report capacity item, content={}.", content, ex);
					Cat.logError(ex);
				}
			}

			int size = reports.size();
			if (size == 0) {
				break;
			} else {
				maxId = reports.get(size - 1).getReportId();
			}
		}
		m_manager.updateDailyStatus(maxId);
		LOGGER.info("Finished daily report capacity scan, finalMaxId={}.", maxId);
	}

	public void setDailyReportContentDao(DailyReportContentRepository dailyReportContentDao) {
		m_dailyReportContentDao = dailyReportContentDao;
	}

	public void setDailyReportDao(DailyReportRepository dailyReportDao) {
		m_dailyReportDao = dailyReportDao;
	}

	public void setOverloadDao(OverloadRepository overloadDao) {
		m_overloadDao = overloadDao;
	}

	public void setManager(CapacityUpdateStatusManager manager) {
		m_manager = manager;
	}

}
