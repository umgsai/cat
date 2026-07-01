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

import java.util.List;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.mybatis.data.MonthReportDO;
import com.dianping.cat.mybatis.data.MonthlyReportContentDO;
import com.dianping.cat.mybatis.MonthlyReportContentRepository;
import com.dianping.cat.mybatis.MonthlyReportRepository;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.mybatis.OverloadRepository;

@Component(MonthlyCapacityUpdater.ID)
public class MonthlyCapacityUpdater implements CapacityUpdater {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyCapacityUpdater.class);

	public static final String ID = "monthly_capacity_updater";

	@Resource
	private MonthlyReportRepository monthlyReportRepository;

	@Resource
	private MonthlyReportContentRepository monthlyReportContentRepository;

	@Resource
	private OverloadRepository overloadRepository;

	@Resource
	private CapacityUpdateStatusManager capacityUpdateStatusManager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity() {
		long maxId = capacityUpdateStatusManager.getMonthlyStatus();
		LOGGER.info("Starting monthly report capacity scan, startMaxId={}.", maxId);

		while (true) {
			List<MonthlyReportContentDO> reports = monthlyReportContentRepository
									.findOverloadReport(maxId);

			for (MonthlyReportContentDO content : reports) {
				try {
					long reportId = content.getReportId();
					double contentLength = content.getContentLength();

					if (contentLength >= CapacityUpdater.CAPACITY) {
						Overload overload = overloadRepository.createLocal();

						overload.setReportId(reportId);
						overload.setReportSize(contentLength);
						overload.setReportType(CapacityUpdater.MONTHLY_TYPE);

						try {
							MonthReportDO report = monthlyReportRepository.findByPK(reportId);
							overload.setPeriod(report.getPeriod());
							overloadRepository.insert(overload);
						} catch (EmptyResultDataAccessException e) {
							LOGGER.warn("Monthly report not found while recording overload report, reportId={}.", reportId);
						} catch (Exception e) {
							LOGGER.error("Unable to record monthly overload report, reportId={}, contentLength={}.",
							      reportId, contentLength, e);
							Cat.logError(e);
						}
					}
				} catch (Exception ex) {
					LOGGER.error("Unable to process monthly report capacity item, content={}.", content, ex);
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
		capacityUpdateStatusManager.updateMonthlyStatus(maxId);
		LOGGER.info("Finished monthly report capacity scan, finalMaxId={}.", maxId);
	}

	public void setMonthlyReportDao(MonthlyReportRepository monthlyReportDao) {
		this.monthlyReportRepository = monthlyReportDao;
	}

	public void setMonthlyReportContentDao(MonthlyReportContentRepository monthlyReportContentDao) {
		this.monthlyReportContentRepository = monthlyReportContentDao;
	}

	public void setOverloadDao(OverloadRepository overloadDao) {
		this.overloadRepository = overloadDao;
	}

	public void setManager(CapacityUpdateStatusManager manager) {
		this.capacityUpdateStatusManager = manager;
	}

}
