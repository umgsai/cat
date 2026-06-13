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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.spring.CatSpringContext;

public class ContactorManager extends ContainerHolder implements Initializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContactorManager.class);

	private Map<String, Contactor> m_contactors = new HashMap<String, Contactor>();

	@Override
	@SuppressWarnings("unchecked")
	public void initialize() throws InitializationException {
		if (m_contactors.isEmpty()) {
			Map<String, Contactor> springContactors = CatSpringContext.getBeanIfAvailable("alertContactors", Map.class);

			if (springContactors != null && !springContactors.isEmpty()) {
				setContactors(springContactors);
				LOGGER.info("Initialized alert contactor manager from Spring context bridge, contactorCount={}.",
				      m_contactors.size());
				return;
			}
			try {
				m_contactors = lookupMap(Contactor.class);
				LOGGER.warn("Initialized alert contactor manager from Plexus fallback, contactorCount={}.",
				      m_contactors.size());
			} catch (RuntimeException e) {
				LOGGER.warn("Unable to initialize alert contactor manager from Plexus fallback, keep empty contactors.",
				      e);
			}
		} else {
			LOGGER.info("Initialized alert contactor manager from Spring injection, contactorCount={}.",
			      m_contactors.size());
		}
	}

	public List<String> queryReceivers(String group, AlertChannel channel, String type) {
		Contactor contactor = m_contactors.get(type);

		if (contactor == null) {
			LOGGER.error("Alert contactor is not configured, type={}, channel={}, group={}, availableContactors={}.", type,
			      channel, group, m_contactors.keySet());
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
		if (contactors == null || contactors.isEmpty()) {
			m_contactors = new HashMap<String, Contactor>();
		} else {
			m_contactors = new HashMap<String, Contactor>(contactors);
		}
		LOGGER.info("Configured alert contactors from Spring, contactorKeys={}.", m_contactors.keySet());
	}

	public Map<String, Contactor> getContactors() {
		return Collections.unmodifiableMap(m_contactors);
	}

}
