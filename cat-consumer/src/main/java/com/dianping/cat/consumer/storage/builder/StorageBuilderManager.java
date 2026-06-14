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
package com.dianping.cat.consumer.storage.builder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.spring.CatSpringContext;

public class StorageBuilderManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageBuilderManager.class);

	private Map<String, StorageBuilder> m_storageBuilders;

	private volatile boolean m_initialized;

	public List<String> getDefaultMethods(String type) {
		ensureInitialized();

		StorageBuilder storageBuilder = m_storageBuilders.get(type);

		if (storageBuilder != null) {
			return storageBuilder.getDefaultMethods();
		} else {
			return Collections.emptyList();
		}
	}

	public StorageBuilder getStorageBuilder(String type) {
		ensureInitialized();

		return m_storageBuilders.get(type);
	}

	private void ensureInitialized() {
		if (!m_initialized) {
			initialize();
		}
	}

	public synchronized void initialize() {
		if (m_initialized) {
			return;
		}
		refreshSpringBuilders();

		if (m_storageBuilders == null) {
			m_storageBuilders = Collections.emptyMap();
			LOGGER.warn("Unable to load storage builders from Spring, keep empty builder map.");
		}
		m_initialized = true;
	}

	private void refreshSpringBuilders() {
		Map<String, StorageBuilder> springBuilders = CatSpringContext.getBeansIfAvailable(StorageBuilder.class);

		if (!springBuilders.isEmpty()) {
			Map<String, StorageBuilder> builders = new LinkedHashMap<String, StorageBuilder>();

			for (StorageBuilder builder : springBuilders.values()) {
				builders.put(builder.getType(), builder);
			}
			m_storageBuilders = builders;
			LOGGER.info("Loaded storage builders from Spring, types={}.", m_storageBuilders.keySet());
		}
	}

	public void setStorageBuilders(Map<String, StorageBuilder> storageBuilders) {
		m_storageBuilders = storageBuilders;
	}

}
