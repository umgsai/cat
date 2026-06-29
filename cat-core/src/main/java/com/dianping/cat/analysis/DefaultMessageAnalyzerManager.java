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
package com.dianping.cat.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;

@Component("messageAnalyzerManager")
public class DefaultMessageAnalyzerManager
						implements MessageAnalyzerManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessageAnalyzerManager.class);

	private static final long MINUTE = 60 * 1000L;

	private long duration = 60 * MINUTE;

	private long extraTime = 3 * MINUTE;

	private List<String> analyzerNames;

	@Resource(name = "containerMessageAnalyzerFactory")
	private MessageAnalyzerFactory analyzerFactory;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	private final Map<Long, Map<String, List<MessageAnalyzer>>> analyzerBuckets = new HashMap<Long, Map<String, List<MessageAnalyzer>>>();

	private volatile boolean initialized;

	@Override
	public List<MessageAnalyzer> getAnalyzer(String name, long startTime) {
		initialize();

		// remove last two hour analyzer
		try {
			Map<String, List<MessageAnalyzer>> temp = analyzerBuckets.remove(startTime - duration * 2);

			if (temp != null) {
				for (List<MessageAnalyzer> analyzers : temp.values()) {
					for (MessageAnalyzer analyzer : analyzers) {
						analyzer.destroy();
					}
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			LOGGER.warn("Unable to destroy expired message analyzers, startTime={}.", startTime, e);
		}

		Map<String, List<MessageAnalyzer>> map = analyzerBuckets.get(startTime);

		if (map == null) {
			synchronized (analyzerBuckets) {
				map = analyzerBuckets.get(startTime);

				if (map == null) {
					map = new HashMap<String, List<MessageAnalyzer>>();
					analyzerBuckets.put(startTime, map);
				}
			}
		}

		List<MessageAnalyzer> analyzers = map.get(name);

		if (analyzers == null) {
			synchronized (map) {
				analyzers = map.get(name);

				if (analyzers == null) {
					analyzers = new ArrayList<MessageAnalyzer>();

					MessageAnalyzer analyzer = createAnalyzer(name);

					analyzer.setIndex(0);
					analyzer.initialize(startTime, duration, extraTime);
					analyzers.add(analyzer);

					int count = analyzer.getAnalyzerCount(name);

					for (int i = 1; i < count; i++) {
						MessageAnalyzer tempAnalyzer = createAnalyzer(name);

						tempAnalyzer.setIndex(i);
						tempAnalyzer.initialize(startTime, duration, extraTime);
						analyzers.add(tempAnalyzer);
					}
					map.put(name, analyzers);
				}
			}
		}

		return analyzers;
	}

	@Override
	public List<String> getAnalyzerNames() {
		initialize();

		return analyzerNames;
	}

	@PostConstruct
	public synchronized void initialize() {
		if (initialized) {
			return;
		}

		Map<String, MessageAnalyzer> map = getAnalyzerMap();

		for (MessageAnalyzer analyzer : map.values()) {
			analyzer.destroy();
		}

		analyzerNames = new ArrayList<String>(map.keySet());

		Collections.sort(analyzerNames, new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				String state = "state";
				String top = "top";

				if (state.equals(str1)) {
					return 1;
				} else if (state.equals(str2)) {
					return -1;
				}
				if (top.equals(str1)) {
					return -1;
				} else if (top.equals(str2)) {
					return 1;
				}
				return str1.compareTo(str2);
			}
		});

		ServerConfigManager manager = getConfigManager();
		List<String> disables = new ArrayList<String>();

		for (String name : analyzerNames) {

			if (!manager.getEnableOfRealtimeAnalyzer(name)) {
				disables.add(name);
			}
		}
		for (String name : disables) {
			analyzerNames.remove(name);
		}
		initialized = true;
	}

	private MessageAnalyzer createAnalyzer(String name) {
		if (analyzerFactory != null) {
			return analyzerFactory.createAnalyzer(name);
		}
		throw new IllegalStateException("MessageAnalyzerFactory is required for DefaultMessageAnalyzerManager.");
	}

	private ServerConfigManager getConfigManager() {
		if (serverConfigManager != null) {
			return serverConfigManager;
		}
		throw new IllegalStateException("ServerConfigManager is required for DefaultMessageAnalyzerManager.");
	}

	private Map<String, MessageAnalyzer> getAnalyzerMap() {
		if (analyzerFactory != null) {
			return analyzerFactory.getAnalyzerMap();
		}
		throw new IllegalStateException("MessageAnalyzerFactory is required for DefaultMessageAnalyzerManager.");
	}

	public void setAnalyzerFactory(MessageAnalyzerFactory analyzerFactory) {
		this.analyzerFactory = analyzerFactory;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		serverConfigManager = configManager;
	}
}
