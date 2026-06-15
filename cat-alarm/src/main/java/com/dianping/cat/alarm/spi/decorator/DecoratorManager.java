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

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;

public class DecoratorManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DecoratorManager.class);

	private Map<String, Decorator> m_decorators = new HashMap<String, Decorator>();

	private volatile boolean m_initialized;

	public Pair<String, String> generateTitleAndContent(AlertEntity alert) {
		ensureInitialized();
		AlertType alertType = alert.getType();
		Decorator decorator = m_decorators.get(alertType.getName());

		if (decorator != null) {
			String title = decorator.generateTitle(alert);
			String content = decorator.generateContent(alert);

			return Pair.of(title, content);
		} else {
			LOGGER.error("Alert decorator is not configured, alertType={}, availableDecorators={}.", alertType.getName(),
			      m_decorators.keySet());
			throw new RuntimeException("error alert type:" + alert.getType());
		}
	}

	public void initialize() {
		if (m_initialized) {
			return;
		}
		synchronized (this) {
			if (m_initialized) {
				return;
			}
			if (m_decorators.isEmpty()) {
				LOGGER.warn("Alert decorator manager has no configured decorators.");
			} else {
				LOGGER.info("Initialized alert decorator manager from Spring injection, decoratorCount={}.",
				      m_decorators.size());
			}
			m_initialized = true;
		}
	}

	private void ensureInitialized() {
		if (!m_initialized) {
			initialize();
		}
	}

	public void setDecorators(Map<String, Decorator> decorators) {
		if (decorators == null || decorators.isEmpty()) {
			m_decorators = new HashMap<String, Decorator>();
		} else {
			m_decorators = new HashMap<String, Decorator>(decorators);
		}
		LOGGER.info("Configured alert decorators from Spring, decoratorKeys={}.", m_decorators.keySet());
	}

	public Map<String, Decorator> getDecorators() {
		ensureInitialized();
		return Collections.unmodifiableMap(m_decorators);
	}

}
