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
package com.dianping.cat.alarm.spi.sender;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Event;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class SenderManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SenderManager.class);

	@Resource
	private ServerConfigManager serverConfigManager;

	@Resource(name = "alertSenders")
	private Map<String, Sender> senders = new HashMap<String, Sender>();

	private volatile boolean initialized;

	@PostConstruct
	public void initialize() {
		if (initialized) {
			return;
		}
		synchronized (this) {
			if (initialized) {
				return;
			}
			senders = copySenders(senders);
			if (senders.isEmpty()) {
				LOGGER.warn("Alert sender manager has no configured senders.");
			} else {
				LOGGER.info("Initialized alert sender manager from Spring injection, senderCount={}.", senders.size());
			}
			initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	private Map<String, Sender> copySenders(Map<String, Sender> senders) {
		if (senders == null || senders.isEmpty()) {
			return new HashMap<String, Sender>();
		}
		return new HashMap<String, Sender>(senders);
	}

	public void setSenders(Map<String, Sender> senders) {
		this.senders = copySenders(senders);
		LOGGER.info("Configured alert senders from Spring, senderKeys={}.", this.senders.keySet());
	}

	public void setConfigManager(ServerConfigManager configManager) {
		serverConfigManager = configManager;
	}

	public Map<String, Sender> getSenders() {
		ensureInitialized();
		return Collections.unmodifiableMap(senders);
	}

	public boolean sendAlert(AlertChannel channel, SendMessageEntity message) {
		ensureInitialized();
		String channelName = channel.getName();

		try {
			boolean result = false;
			String str = "nosend";

			if (serverConfigManager != null && serverConfigManager.isSendMachine()) {
				Sender sender = senders.get(channelName);

				if (sender == null) {
					LOGGER.warn("Alert sender is not configured, channel={}, messageType={}.", channelName,
					      message.getType());
					Cat.logEvent("Channel:" + channel, message.getType() + ":nosender", Event.SUCCESS, null);
					return false;
				}
				result = sender.send(message);
				str = String.valueOf(result);
			} else {
				LOGGER.info("Current machine is not configured as send machine, skip alert sending, channel={}, type={}.",
				      channelName, message.getType());
			}
			Cat.logEvent("Channel:" + channel, message.getType() + ":" + str, Event.SUCCESS, null);
			return result;
		} catch (Exception e) {
			LOGGER.error("Unable to send alert, channel={}, message={}.", channel, message, e);
			Cat.logError("Channel [" + channel + "] " + message.toString(), e);
			return false;
		}
	}

}
