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

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.spring.CatSpringContext;

public class DefaultMessageHandler extends ContainerHolder implements MessageHandler, LogEnabled {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(DefaultMessageHandler.class);

	private MessageConsumer m_consumer;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void handle(MessageTree tree) {
		if (m_consumer == null) {
			m_consumer = CatSpringContext.getBeanIfAvailable(MessageConsumer.class);

			if (m_consumer == null) {
				m_consumer = lookup(MessageConsumer.class);
				SLF4J_LOGGER.info("Resolved message consumer from Plexus fallback, consumer={}.", m_consumer);
			} else {
				SLF4J_LOGGER.info("Resolved message consumer from Spring context, consumer={}.", m_consumer);
			}
		}

		try {
			m_consumer.consume(tree);
		} catch (Throwable e) {
			if (m_logger != null) {
				m_logger.error("Error when consuming message in " + m_consumer + "! tree: " + tree, e);
			} else {
				SLF4J_LOGGER.error("Error when consuming message, consumer={}, tree={}.", m_consumer, tree, e);
			}
		}
	}

	public void setConsumer(MessageConsumer consumer) {
		m_consumer = consumer;
	}
}
