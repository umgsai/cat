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
package com.dianping.cat.report.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.cat.support.Threads;
import com.dianping.cat.support.Threads.Task;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.spring.CatSpringContext;

public class AlarmManager extends ContainerHolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlarmManager.class);

	private BusinessAlert m_businessAlert;

	private EventAlert m_eventAlert;

	private ExceptionAlert m_exceptionAlert;

	private HeartbeatAlert m_heartbeatAlert;

	private TransactionAlert m_transactionAlert;

	public void startAlarm() {
		BusinessAlert businessAlert = getAlert(BusinessAlert.class, m_businessAlert);
		ExceptionAlert exceptionAlert = getAlert(ExceptionAlert.class, m_exceptionAlert);
		HeartbeatAlert heartbeatAlert = getAlert(HeartbeatAlert.class, m_heartbeatAlert);
		TransactionAlert transactionAlert = getAlert(TransactionAlert.class, m_transactionAlert);
		EventAlert eventAlert = getAlert(EventAlert.class, m_eventAlert);

		Threads.forGroup("cat").start(businessAlert);
		Threads.forGroup("cat").start(exceptionAlert);
		Threads.forGroup("cat").start(heartbeatAlert);
		Threads.forGroup("cat").start(transactionAlert);
		Threads.forGroup("cat").start(eventAlert);
		LOGGER.info("Started alert tasks, springConfigured={}, alerts=[business,exception,heartbeat,transaction,event].",
		      isSpringConfigured());
	}

	private <T extends Task> T getAlert(Class<T> type, T configuredAlert) {
		if (configuredAlert != null) {
			return configuredAlert;
		}
		T springAlert = CatSpringContext.getBeanIfAvailable(type);

		if (springAlert != null) {
			LOGGER.info("Resolved alert task from Spring context, alertType={}.", type.getName());
			return springAlert;
		}
		LOGGER.info("Resolved alert task from Plexus fallback, alertType={}.", type.getName());
		return lookup(type);
	}

	private boolean isSpringConfigured() {
		return m_businessAlert != null || m_exceptionAlert != null || m_heartbeatAlert != null
		      || m_transactionAlert != null || m_eventAlert != null;
	}

	public void setBusinessAlert(BusinessAlert businessAlert) {
		m_businessAlert = businessAlert;
	}

	public void setEventAlert(EventAlert eventAlert) {
		m_eventAlert = eventAlert;
	}

	public void setExceptionAlert(ExceptionAlert exceptionAlert) {
		m_exceptionAlert = exceptionAlert;
	}

	public void setHeartbeatAlert(HeartbeatAlert heartbeatAlert) {
		m_heartbeatAlert = heartbeatAlert;
	}

	public void setTransactionAlert(TransactionAlert transactionAlert) {
		m_transactionAlert = transactionAlert;
	}
}
