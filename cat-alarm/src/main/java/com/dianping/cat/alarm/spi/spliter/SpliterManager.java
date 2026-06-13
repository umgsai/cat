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
package com.dianping.cat.alarm.spi.spliter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.spring.CatSpringContext;

public class SpliterManager extends ContainerHolder implements Initializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpliterManager.class);

	private Map<String, Spliter> m_spliters = new HashMap<String, Spliter>();

	@Override
	@SuppressWarnings("unchecked")
	public void initialize() throws InitializationException {
		if (m_spliters.isEmpty()) {
			Map<String, Spliter> springSpliters = CatSpringContext.getBeanIfAvailable("alertSpliters", Map.class);

			if (springSpliters != null && !springSpliters.isEmpty()) {
				setSpliters(springSpliters);
				LOGGER.info("Initialized alert splitter manager from Spring context bridge, splitterCount={}.",
				      m_spliters.size());
				return;
			}
			try {
				m_spliters = lookupMap(Spliter.class);
				LOGGER.warn("Initialized alert splitter manager from Plexus fallback, splitterCount={}.", m_spliters.size());
			} catch (RuntimeException e) {
				LOGGER.warn("Unable to initialize alert splitter manager from Plexus fallback, keep empty splitters.", e);
			}
		} else {
			LOGGER.info("Initialized alert splitter manager from Spring injection, splitterCount={}.", m_spliters.size());
		}
	}

	public String process(String content, AlertChannel channel) {
		String channelName = channel.getName();
		Spliter splitter = m_spliters.get(channelName);

		if (splitter == null) {
			LOGGER.error("Alert splitter is not configured, channel={}, availableSplitters={}.", channelName,
			      m_spliters.keySet());
			throw new IllegalStateException("Alert splitter is not configured for channel: " + channelName);
		}
		return splitter.process(content);
	}

	public void setSpliters(Map<String, Spliter> spliters) {
		if (spliters == null || spliters.isEmpty()) {
			m_spliters = new HashMap<String, Spliter>();
		} else {
			m_spliters = new HashMap<String, Spliter>(spliters);
		}
		LOGGER.info("Configured alert splitters from Spring, splitterKeys={}.", m_spliters.keySet());
	}

	public Map<String, Spliter> getSpliters() {
		return Collections.unmodifiableMap(m_spliters);
	}

}
