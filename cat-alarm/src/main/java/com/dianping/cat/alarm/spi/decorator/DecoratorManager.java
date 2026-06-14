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
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.spring.CatSpringContext;

public class DecoratorManager extends ContainerHolder {
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

	@SuppressWarnings("unchecked")
	public void initialize() {
		if (m_initialized) {
			return;
		}
		synchronized (this) {
			if (m_initialized) {
				return;
			}
			if (m_decorators.isEmpty()) {
				Map<String, Decorator> springDecorators = CatSpringContext.getBeanIfAvailable("alertDecorators", Map.class);

				if (springDecorators != null && !springDecorators.isEmpty()) {
					setDecorators(springDecorators);
					mergePlexusDecoratorsIfMissing();
					LOGGER.info("Initialized alert decorator manager from Spring context bridge, decoratorCount={}.",
					      m_decorators.size());
					m_initialized = true;
					return;
				}
				try {
					m_decorators = lookupMap(Decorator.class);
					LOGGER.warn("Initialized alert decorator manager from Plexus fallback, decoratorCount={}.",
					      m_decorators.size());
				} catch (RuntimeException e) {
					LOGGER.warn("Unable to initialize alert decorator manager from Plexus fallback, keep empty decorators.",
					      e);
				}
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

	private void mergePlexusDecoratorsIfMissing() {
		if (m_decorators.containsKey(AlertType.Business.getName())
		      && m_decorators.containsKey(AlertType.Exception.getName())) {
			return;
		}
		try {
			Map<String, Decorator> plexusDecorators = lookupMap(Decorator.class);

			if (plexusDecorators != null && !plexusDecorators.isEmpty()) {
				for (Map.Entry<String, Decorator> entry : plexusDecorators.entrySet()) {
					if (!m_decorators.containsKey(entry.getKey())) {
						m_decorators.put(entry.getKey(), entry.getValue());
					}
				}
				LOGGER.info("Merged Plexus alert decorators as fallback, decoratorCount={}, decoratorKeys={}.",
				      plexusDecorators.size(), plexusDecorators.keySet());
			}
		} catch (RuntimeException e) {
			LOGGER.warn("Unable to merge Plexus alert decorators as fallback.", e);
		}
	}

}
