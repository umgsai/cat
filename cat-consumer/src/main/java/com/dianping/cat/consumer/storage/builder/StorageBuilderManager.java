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
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class StorageBuilderManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageBuilderManager.class);

	@Resource
	private List<StorageBuilder> storageBuilderList = Collections.emptyList();

	private Map<String, StorageBuilder> storageBuilders;

	private volatile boolean initialized;

	public List<String> getDefaultMethods(String type) {
		ensureInitialized();

		StorageBuilder storageBuilder = storageBuilders.get(type);

		if (storageBuilder != null) {
			return storageBuilder.getDefaultMethods();
		} else {
			return Collections.emptyList();
		}
	}

	public StorageBuilder getStorageBuilder(String type) {
		ensureInitialized();

		return storageBuilders.get(type);
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	private Map<String, StorageBuilder> buildStorageBuilders(List<StorageBuilder> builders) {
		Map<String, StorageBuilder> result = new LinkedHashMap<String, StorageBuilder>();

		if (builders == null || builders.isEmpty()) {
			return result;
		}
		for (StorageBuilder builder : builders) {
			if (builder == null) {
				continue;
			}
			String type = builder.getType();

			if (type == null || type.length() == 0) {
				LOGGER.warn("Ignore storage builder without type, builderClass={}.", builder.getClass().getName());
				continue;
			}
			StorageBuilder previous = result.put(type, builder);

			if (previous != null) {
				LOGGER.warn("Duplicate storage builder type detected, type={}, previousClass={}, currentClass={}.", type,
				      previous.getClass().getName(), builder.getClass().getName());
			}
		}
		return result;
	}

	@PostConstruct
	public synchronized void initialize() {
		if (initialized) {
			return;
		}
		if (storageBuilders == null) {
			storageBuilders = buildStorageBuilders(storageBuilderList);
		} else {
			storageBuilders = new LinkedHashMap<String, StorageBuilder>(storageBuilders);
		}
		if (storageBuilders.isEmpty()) {
			LOGGER.warn("Storage builder manager has no configured builders, keep empty builder map.");
		} else {
			LOGGER.info("Initialized storage builder manager, builderTypes={}.", storageBuilders.keySet());
		}
		initialized = true;
	}

	public void setStorageBuilders(Map<String, StorageBuilder> storageBuilders) {
		if (storageBuilders == null || storageBuilders.isEmpty()) {
			this.storageBuilders = new LinkedHashMap<String, StorageBuilder>();
		} else {
			this.storageBuilders = new LinkedHashMap<String, StorageBuilder>(storageBuilders);
		}
	}

}
