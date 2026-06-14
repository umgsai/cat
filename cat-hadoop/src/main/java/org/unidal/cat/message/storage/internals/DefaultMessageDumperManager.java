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
package org.unidal.cat.message.storage.internals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperFactory;
import org.unidal.cat.message.storage.MessageDumperManager;

public class DefaultMessageDumperManager implements MessageDumperManager, Initializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessageDumperManager.class);

	private MessageDumperFactory m_messageDumperFactory;

	private Map<Integer, MessageDumper> m_dumpers = new LinkedHashMap<Integer, MessageDumper>();

	@Override
	public synchronized void close(int hour) {
		MessageDumper dumper = m_dumpers.remove(hour);

		if (dumper != null) {
			try {
				dumper.awaitTermination(hour);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public MessageDumper find(int hour) {
		return m_dumpers.get(hour);
	}

	@Override
	public MessageDumper findOrCreate(int hour) {
		MessageDumper dumper = m_dumpers.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_dumpers.get(hour);

				if (dumper == null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					dumper = createDumper(hour);

					m_dumpers.put(hour, dumper);
					LOGGER.info("create message dumper " + sdf.format(new Date(TimeUnit.HOURS.toMillis(hour))));
				}
			}
		}

		return dumper;
	}

	@Override
	public void initialize() throws InitializationException {
	}

	private MessageDumper createDumper(int hour) {
		if (m_messageDumperFactory == null) {
			throw new IllegalStateException("MessageDumperFactory is required.");
		}
		return m_messageDumperFactory.createMessageDumper(hour);
	}

	public void setMessageDumperFactory(MessageDumperFactory messageDumperFactory) {
		m_messageDumperFactory = messageDumperFactory;
	}
}
