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
package com.dianping.cat.report.alert.summary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.AlertSummary;
import com.dianping.cat.core.mybatis.repository.alert.summary.AlertSummaryRepository;
import com.dianping.cat.spring.CatSpringContext;

public class AlertSummaryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertSummaryService.class);

	private AlertSummaryRepository m_alertSummaryDao;

	public void insert(com.dianping.cat.home.alert.summary.entity.AlertSummary alertSummary) {
		refreshSpringBeans();
		AlertSummary summary = new AlertSummary();
		String content = alertSummary.toString();

		summary.setDomain(alertSummary.getDomain());
		summary.setAlertTime(alertSummary.getAlertDate());
		summary.setContent(content);

		try {
			m_alertSummaryDao.insert(summary);
		} catch (DalException e) {
			LOGGER.error("Unable to insert alert summary, domain={}, alertTime={}.", alertSummary.getDomain(),
					alertSummary.getAlertDate(), e);
			Cat.logError("insert alert summary error: " + content, e);
		}
	}

	private void refreshSpringBeans() {
		AlertSummaryRepository alertSummaryDao = CatSpringContext.getBeanIfAvailable(AlertSummaryRepository.class);

		if (alertSummaryDao != null) {
			m_alertSummaryDao = alertSummaryDao;
		}
	}

	public void setAlertSummaryDao(AlertSummaryRepository alertSummaryDao) {
		m_alertSummaryDao = alertSummaryDao;
	}

}
