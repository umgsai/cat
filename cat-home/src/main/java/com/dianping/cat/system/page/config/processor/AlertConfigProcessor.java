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
package com.dianping.cat.system.page.config.processor;

import jakarta.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

@Component("alertConfigProcessor")
public class AlertConfigProcessor {

	@Resource
	private AlertConfigManager alertConfigManager;

	@Resource
	private AlertPolicyManager alertPolicyManager;

	@Resource
	private ConfigHtmlParser configHtmlParser;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case ALERT_DEFAULT_RECEIVERS:
			String alertDefaultReceivers = payload.getContent();
			String allOnOrOff = payload.getAllOnOrOff();
			String xmlContent = alertConfigManager.buildReceiverContentByOnOff(alertDefaultReceivers, allOnOrOff);

			if (!StringUtils.isEmpty(alertDefaultReceivers)) {
				model.setOpState(alertConfigManager.insert(xmlContent));
			} else {
				model.setOpState(true);
			}
			model.setContent(configHtmlParser.parse(alertConfigManager.getAlertConfig().toString()));
			break;
		case ALERT_POLICY:
			String alertPolicy = payload.getContent();

			if (!StringUtils.isEmpty(alertPolicy)) {
				model.setOpState(alertPolicyManager.insert(alertPolicy));
			} else {
				model.setOpState(true);
			}
			model.setContent(configHtmlParser.parse(alertPolicyManager.getAlertPolicy().toString()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

}
