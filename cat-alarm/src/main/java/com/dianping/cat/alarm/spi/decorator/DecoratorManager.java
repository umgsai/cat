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
package com.dianping.cat.alarm.spi.decorator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class DecoratorManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DecoratorManager.class);

	@Resource(name = "alertDecorators")
	private Map<String, Decorator> decorators = new HashMap<String, Decorator>();

	private volatile boolean initialized;

	public Pair<String, String> generateTitleAndContent(AlertEntity alert) {
		ensureInitialized();
		AlertType alertType = alert.getType();
		Decorator decorator = decorators.get(alertType.getName());

		if (decorator != null) {
			String title = decorator.generateTitle(alert);
			String content = decorator.generateContent(alert);

			return Pair.of(title, content);
		} else {
			LOGGER.error("Alert decorator is not configured, alertType={}, availableDecorators={}.", alertType.getName(),
			      decorators.keySet());
			throw new RuntimeException("error alert type:" + alert.getType());
		}
	}

	@PostConstruct
	public void initialize() {
		if (initialized) {
			return;
		}
		synchronized (this) {
			if (initialized) {
				return;
			}
			decorators = copyDecorators(decorators);
			if (decorators.isEmpty()) {
				LOGGER.warn("Alert decorator manager has no configured decorators.");
			} else {
				LOGGER.info("Initialized alert decorator manager from Spring injection, decoratorCount={}.",
				      decorators.size());
			}
			initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	private Map<String, Decorator> copyDecorators(Map<String, Decorator> decorators) {
		if (decorators == null || decorators.isEmpty()) {
			return new HashMap<String, Decorator>();
		}
		return new HashMap<String, Decorator>(decorators);
	}

	public void setDecorators(Map<String, Decorator> decorators) {
		this.decorators = copyDecorators(decorators);
		LOGGER.info("Configured alert decorators from Spring, decoratorKeys={}.", this.decorators.keySet());
	}

	public Map<String, Decorator> getDecorators() {
		ensureInitialized();
		return Collections.unmodifiableMap(decorators);
	}

}
