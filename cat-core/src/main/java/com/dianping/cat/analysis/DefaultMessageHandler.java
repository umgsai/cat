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

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.message.spi.BufReleaseHelper;
import com.dianping.cat.message.spi.MessageTree;

@Component("messageHandler")
public class DefaultMessageHandler implements MessageHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessageHandler.class);

	@Resource(name = "messageConsumer")
	private MessageConsumer messageConsumer;

	@Override
	public void handle(MessageTree tree) {
		if (messageConsumer == null) {
			LOGGER.warn("Message consumer is not configured, drop message tree={}.", tree);
			BufReleaseHelper.release(tree.getBuffer());
			return;
		}

		try {
			messageConsumer.consume(tree);
		} catch (Throwable e) {
			LOGGER.error("Error when consuming message, consumer={}, tree={}.", messageConsumer, tree, e);
		}
	}

	public void setConsumer(MessageConsumer consumer) {
		messageConsumer = consumer;
	}
}
