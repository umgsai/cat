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

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.core.mybatis.repository.weekly.report.content.WeeklyReportContentRepository;
import com.dianping.cat.core.dal.WeeklyReportContentEntity;
import com.dianping.cat.core.mybatis.repository.weeklyreport.WeeklyReportRepository;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.core.mybatis.repository.overload.OverloadRepository;
import com.dianping.cat.spring.CatSpringContext;

@Named(type = CapacityUpdater.class, value = WeeklyCapacityUpdater.ID)
public class WeeklyCapacityUpdater implements CapacityUpdater {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyCapacityUpdater.class);

	public static final String ID = "weekly_capacity_updater";

	@Inject
	private WeeklyReportRepository m_weeklyReportDao;

	@Inject
	private WeeklyReportContentRepository m_weeklyReportContentDao;

	@Inject
	private OverloadRepository m_overloadDao;

	@Inject
	private CapacityUpdateStatusManager m_manager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity() throws DalException {
		refreshSpringBeans();

		int maxId = m_manager.getWeeklyStatus();
		LOGGER.info("Starting weekly report capacity scan, startMaxId={}.", maxId);

		while (true) {
			List<WeeklyReportContent> reports = m_weeklyReportContentDao
									.findOverloadReport(maxId,	WeeklyReportContentEntity.READSET_LENGTH);

			for (WeeklyReportContent content : reports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();

					if (contentLength >= CapacityUpdater.CAPACITY) {
						Overload overload = m_overloadDao.createLocal();

						overload.setReportId(reportId);
						overload.setReportSize(contentLength);
						overload.setReportType(CapacityUpdater.WEEKLY_TYPE);

						try {
							WeeklyReport report = m_weeklyReportDao.findByPK(reportId, WeeklyReportEntity.READSET_FULL);
							overload.setPeriod(report.getPeriod());
							m_overloadDao.insert(overload);
						} catch (DalNotFoundException e) {
							LOGGER.warn("Weekly report not found while recording overload report, reportId={}.", reportId);
						} catch (Exception e) {
							LOGGER.error("Unable to record weekly overload report, reportId={}, contentLength={}.",
							      reportId, contentLength, e);
							Cat.logError(e);
						}
					}
				} catch (Exception ex) {
					LOGGER.error("Unable to process weekly report capacity item, content={}.", content, ex);
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
		m_manager.updateWeeklyStatus(maxId);
		LOGGER.info("Finished weekly report capacity scan, finalMaxId={}.", maxId);
	}

	private void refreshSpringBeans() {
		WeeklyReportRepository weeklyReportDao = CatSpringContext.getBeanIfAvailable(WeeklyReportRepository.class);
		WeeklyReportContentRepository weeklyReportContentDao = CatSpringContext
		      .getBeanIfAvailable(WeeklyReportContentRepository.class);
		OverloadRepository overloadDao = CatSpringContext.getBeanIfAvailable(OverloadRepository.class);

		if (weeklyReportDao != null) {
			m_weeklyReportDao = weeklyReportDao;
		}
		if (weeklyReportContentDao != null) {
			m_weeklyReportContentDao = weeklyReportContentDao;
		}
		if (overloadDao != null) {
			m_overloadDao = overloadDao;
		}
	}

}
