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

import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.transaction.TransactionAlert;

public class AlarmManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlarmManager.class);

	private BusinessAlert m_businessAlert;

	private EventAlert m_eventAlert;

	private ExceptionAlert m_exceptionAlert;

	private HeartbeatAlert m_heartbeatAlert;

	private TransactionAlert m_transactionAlert;

	public void startAlarm() {
		Threads.forGroup("cat").start(m_businessAlert);
		Threads.forGroup("cat").start(m_exceptionAlert);
		Threads.forGroup("cat").start(m_heartbeatAlert);
		Threads.forGroup("cat").start(m_transactionAlert);
		Threads.forGroup("cat").start(m_eventAlert);
		LOGGER.info("Started alert tasks, alerts=[business,exception,heartbeat,transaction,event].");
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
