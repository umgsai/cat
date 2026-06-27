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
public class SpliterManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpliterManager.class);

	@Resource
	private List<Spliter> spliterList = Collections.emptyList();

	private Map<String, Spliter> spliters;

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
			if (spliters == null) {
				spliters = buildSpliters(spliterList);
			} else {
				spliters = copySpliters(spliters);
			}
			if (spliters.isEmpty()) {
				LOGGER.warn("Alert splitter manager has no configured splitters.");
			} else {
				LOGGER.info("Initialized alert splitter manager from Spring injection, splitterKeys={}.",
				      spliters.keySet());
			}
			initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	private Map<String, Spliter> buildSpliters(List<Spliter> spliterList) {
		Map<String, Spliter> result = new LinkedHashMap<String, Spliter>();

		if (spliterList == null || spliterList.isEmpty()) {
			return result;
		}
		for (Spliter spliter : spliterList) {
			if (spliter == null) {
				continue;
			}
			String id = spliter.getID();

			if (id == null || id.length() == 0) {
				LOGGER.warn("Ignore alert splitter without id, splitterClass={}.", spliter.getClass().getName());
				continue;
			}
			Spliter previous = result.put(id, spliter);

			if (previous != null) {
				LOGGER.warn("Duplicate alert splitter id detected, id={}, previousClass={}, currentClass={}.", id,
				      previous.getClass().getName(), spliter.getClass().getName());
			}
		}
		return result;
	}

	private Map<String, Spliter> copySpliters(Map<String, Spliter> spliters) {
		if (spliters == null || spliters.isEmpty()) {
			return new LinkedHashMap<String, Spliter>();
		}
		return new LinkedHashMap<String, Spliter>(spliters);
	}

	public String process(String content, AlertChannel channel) {
		ensureInitialized();
		String channelName = channel.getName();
		Spliter splitter = spliters.get(channelName);

		if (splitter == null) {
			LOGGER.error("Alert splitter is not configured, channel={}, availableSplitters={}.", channelName,
			      spliters.keySet());
			throw new IllegalStateException("Alert splitter is not configured for channel: " + channelName);
		}
		return splitter.process(content);
	}

	public void setSpliters(Map<String, Spliter> spliters) {
		this.spliters = copySpliters(spliters);
		LOGGER.info("Configured alert splitters from Spring, splitterKeys={}.", this.spliters.keySet());
	}

	public Map<String, Spliter> getSpliters() {
		ensureInitialized();
		return Collections.unmodifiableMap(spliters);
	}

}
