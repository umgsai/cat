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
package com.dianping.cat.alarm.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.Alert;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.core.mybatis.repository.alert.AlertRepository;

public class AlertService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertService.class);

	private AlertRepository m_alertDao;

	private Alert buildAlert(AlertEntity alertEntity, SendMessageEntity message) {
		Alert alert = new Alert();

		alert.setDomain(alertEntity.getDomain());
		alert.setAlertTime(alertEntity.getDate());
		alert.setCategory(alertEntity.getType().getName());
		alert.setType(alertEntity.getLevel().getLevel());
		alert.setContent(message.getTitle() + "<br/>" + message.getContent());
		alert.setMetric(alertEntity.getMetric());

		return alert;
	}

	public List<Alert> query(Date start, Date end, String type) {
		List<Alert> alerts = new LinkedList<Alert>();

		try {
			alerts = m_alertDao.queryAlertsByTimeCategory(start, end, type);
		} catch (EmptyResultDataAccessException e) {
			// ignore
		} catch (Exception e) {
			LOGGER.error("Unable to query alerts, start={}, end={}, type={}.", start, end, type, e);
			Cat.logError(e);
		}

		return alerts;
	}

	public void insert(AlertEntity alertEntity, SendMessageEntity message) {
		Alert alert = buildAlert(alertEntity, message);

		try {
			int count = m_alertDao.insert(alert);

			if (count != 1) {
				LOGGER.error("Unexpected alert insert count, count={}, domain={}, category={}, metric={}.", count,
				      alert.getDomain(), alert.getCategory(), alert.getMetric());
				Cat.logError("insert alert error: " + alert.toString(), new RuntimeException());
			}
		} catch (RuntimeException e) {
			LOGGER.error("Unable to insert alert, domain={}, type={}, metric={}.", alertEntity.getDomain(),
			      alertEntity.getType().getName(), alertEntity.getMetric(), e);
			Cat.logError(e);
		}
	}

	public void setAlertDao(AlertRepository alertDao) {
		m_alertDao = alertDao;
	}
}
