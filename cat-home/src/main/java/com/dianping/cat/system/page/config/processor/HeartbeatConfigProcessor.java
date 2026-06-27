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

import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

@Component("heartbeatConfigProcessor")
public class HeartbeatConfigProcessor extends BaseProcesser {

	@Resource
	private HeartbeatRuleConfigManager heartbeatRuleConfigManager;

	@Resource
	private HeartbeatDisplayPolicyManager heartbeatDisplayPolicyManager;

	@Resource
	private ConfigHtmlParser configHtmlParser;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case HEARTBEAT_RULE_CONFIG_LIST:
			generateRuleItemList(heartbeatRuleConfigManager, model);
			break;
		case HEARTBEAT_RULE_ADD_OR_UPDATE:
			model.setHeartbeatExtensionMetrics(heartbeatDisplayPolicyManager.queryAlertMetrics());
			generateRuleConfigContent(payload.getKey(), heartbeatRuleConfigManager, model);
			break;
		case HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(heartbeatRuleConfigManager, payload.getRuleId(),
					payload.getMetrics(), payload.getConfigs(), payload.getAvailable()));
			generateRuleItemList(heartbeatRuleConfigManager, model);
			break;
		case HEARTBEAT_RULE_DELETE:
			model.setOpState(deleteRule(heartbeatRuleConfigManager, payload.getKey()));
			generateRuleItemList(heartbeatRuleConfigManager, model);
			break;
		case HEARTBEAT_DISPLAY_POLICY:
			String displayPoicy = payload.getContent();

			if (!StringUtils.isEmpty(displayPoicy)) {
				model.setOpState(heartbeatDisplayPolicyManager.insert(displayPoicy));
			} else {
				model.setOpState(true);
			}
			model.setContent(configHtmlParser.parse(heartbeatDisplayPolicyManager.getHeartbeatDisplayPolicy().toString()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

}
