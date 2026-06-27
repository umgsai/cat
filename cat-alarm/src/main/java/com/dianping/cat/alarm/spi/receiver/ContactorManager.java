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
package com.dianping.cat.alarm.spi.receiver;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.alarm.spi.AlertChannel;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class ContactorManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContactorManager.class);

	@Resource
	private List<Contactor> contactorList = Collections.emptyList();

	private Map<String, Contactor> contactors;

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
			if (contactors == null) {
				contactors = buildContactors(contactorList);
			} else {
				contactors = copyContactors(contactors);
			}
			if (contactors.isEmpty()) {
				LOGGER.warn("Alert contactor manager has no configured contactors.");
			} else {
				LOGGER.info("Initialized alert contactor manager from Spring injection, contactorKeys={}.",
				      contactors.keySet());
			}
			initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	private Map<String, Contactor> buildContactors(List<Contactor> contactorList) {
		Map<String, Contactor> result = new LinkedHashMap<String, Contactor>();

		if (contactorList == null || contactorList.isEmpty()) {
			return result;
		}
		for (Contactor contactor : contactorList) {
			if (contactor == null) {
				continue;
			}
			String id = contactor.getId();

			if (id == null || id.length() == 0) {
				LOGGER.warn("Ignore alert contactor without id, contactorClass={}.", contactor.getClass().getName());
				continue;
			}
			Contactor previous = result.put(id, contactor);

			if (previous != null) {
				LOGGER.warn("Duplicate alert contactor id detected, id={}, previousClass={}, currentClass={}.", id,
				      previous.getClass().getName(), contactor.getClass().getName());
			}
		}
		return result;
	}

	private Map<String, Contactor> copyContactors(Map<String, Contactor> contactors) {
		if (contactors == null || contactors.isEmpty()) {
			return new LinkedHashMap<String, Contactor>();
		}
		return new LinkedHashMap<String, Contactor>(contactors);
	}

	public List<String> queryReceivers(String group, AlertChannel channel, String type) {
		ensureInitialized();
		Contactor contactor = contactors.get(type);

		if (contactor == null) {
			LOGGER.error("Alert contactor is not configured, type={}, channel={}, group={}, availableContactors={}.", type,
			      channel, group, contactors.keySet());
			throw new IllegalStateException("Alert contactor is not configured for type: " + type);
		}
		if (AlertChannel.MAIL == channel) {
			return contactor.queryEmailContactors(group);
		} else if (AlertChannel.SMS == channel) {
			return contactor.querySmsContactors(group);
		} else if (AlertChannel.WEIXIN == channel) {
			return contactor.queryWeiXinContactors(group);
		} else if (AlertChannel.DX == channel) {
			return contactor.queryDXContactors(group);
		} else {
			LOGGER.error("Unsupported alert receiver channel, type={}, channel={}, group={}.", type, channel, group);
			throw new RuntimeException("unsupported channel");
		}
	}

	public void setContactors(Map<String, Contactor> contactors) {
		this.contactors = copyContactors(contactors);
		LOGGER.info("Configured alert contactors from Spring, contactorKeys={}.", this.contactors.keySet());
	}

	public Map<String, Contactor> getContactors() {
		ensureInitialized();
		return Collections.unmodifiableMap(contactors);
	}

}
