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
package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.mybatis.repository.user.define.rule.UserDefineRuleRepository;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager;
import com.dianping.cat.report.alert.transaction.TransactionAlert;

public class HomeAlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(C(AlarmManager.class) //
								.req(BusinessAlert.class, (String) null, "m_businessAlert") //
								.req(EventAlert.class, (String) null, "m_eventAlert") //
								.req(ExceptionAlert.class, (String) null, "m_exceptionAlert") //
								.req(HeartbeatAlert.class, (String) null, "m_heartbeatAlert") //
								.req(TransactionAlert.class, (String) null, "m_transactionAlert"));
		all.add(C(AlertConfigManager.class) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(ContentFetcher.class, (String) null, "m_fetcher"));
		all.add(C(SenderConfigManager.class) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(ContentFetcher.class, (String) null, "m_fetcher"));
		all.add(C(AlertPolicyManager.class) //
								.req(ConfigRepository.class, (String) null, "m_configDao") //
								.req(ContentFetcher.class, (String) null, "m_fetcher"));
		all.add(C(BaseRuleHelper.class));
		all.add(C(UserDefinedRuleManager.class) //
								.req(UserDefineRuleRepository.class, (String) null, "m_dao"));
		return all;
	}
}
