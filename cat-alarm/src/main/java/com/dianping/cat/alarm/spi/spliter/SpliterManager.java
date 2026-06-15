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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.alarm.spi.AlertChannel;

public class SpliterManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpliterManager.class);

	private Map<String, Spliter> m_spliters = new HashMap<String, Spliter>();

	private volatile boolean m_initialized;

	public void initialize() {
		if (m_initialized) {
			return;
		}
		synchronized (this) {
			if (m_initialized) {
				return;
			}
			if (m_spliters.isEmpty()) {
				LOGGER.warn("Alert splitter manager has no configured splitters.");
			} else {
				LOGGER.info("Initialized alert splitter manager from Spring injection, splitterCount={}.",
				      m_spliters.size());
			}
			m_initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!m_initialized) {
			initialize();
		}
	}

	public String process(String content, AlertChannel channel) {
		ensureInitialized();
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
		ensureInitialized();
		return Collections.unmodifiableMap(m_spliters);
	}

}
