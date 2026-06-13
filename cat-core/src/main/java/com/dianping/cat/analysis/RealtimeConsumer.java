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

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

public class RealtimeConsumer extends ContainerHolder implements MessageConsumer, Initializable, LogEnabled {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(RealtimeConsumer.class);

	public static final long MINUTE = 60 * 1000L;

	public static final long HOUR = 60 * MINUTE;

	private MessageAnalyzerManager m_analyzerManager;

	private ServerStatisticManager m_serverStateManager;

	private PeriodManager m_periodManager;

	private Logger m_logger;

	@Override
	public void consume(MessageTree tree) {
		if (m_periodManager == null) {
			SLF4J_LOGGER.error("Realtime consumer is not initialized, tree={}.", tree);
			return;
		}

		long timestamp = getTimestamp(tree);
		Period period = m_periodManager.findPeriod(timestamp);

		if (period != null) {
			period.distribute(tree);
		} else {
			SLF4J_LOGGER.warn("No realtime period found for message tree, timestamp={}, tree={}.", timestamp, tree);
			m_serverStateManager.addNetworkTimeError(1);
		}
	}

	public void doCheckpoint() {
		info("starting do checkpoint.");
		Transaction t = Cat.newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			long currentStartTime = getCurrentStartTime();
			Period period = m_periodManager.findPeriod(currentStartTime);

			if (period == null) {
				SLF4J_LOGGER.warn("No current realtime period found when doing checkpoint, startTime={}.", currentStartTime);
			} else {
				for (MessageAnalyzer analyzer : period.getAnalyzers()) {
					try {
						analyzer.doCheckpoint(false);
					} catch (Exception e) {
						Cat.logError(e);
						SLF4J_LOGGER.error("Failed to checkpoint realtime analyzer, analyzer={}.", analyzer, e);
					}
				}
			}

			try {
				// wait dump analyzer store completed
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				// ignore
			}
			t.setStatus(Message.SUCCESS);
		} catch (RuntimeException e) {
			Cat.logError(e);
			SLF4J_LOGGER.error("Failed to do realtime checkpoint.", e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
		info("end do checkpoint.");
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public List<MessageAnalyzer> getCurrentAnalyzer(String name) {
		long currentStartTime = getCurrentStartTime();
		Period period = m_periodManager.findPeriod(currentStartTime);

		if (period != null) {
			return period.getAnalyzer(name);
		} else {
			return null;
		}
	}

	private long getCurrentStartTime() {
		long now = System.currentTimeMillis();

		return now - now % HOUR;
	}

	@Override
	public List<MessageAnalyzer> getLastAnalyzer(String name) {
		long lastStartTime = getCurrentStartTime() - HOUR;
		Period period = m_periodManager.findPeriod(lastStartTime);

		return period == null ? null : period.getAnalyzer(name);
	}

	private long getTimestamp(MessageTree tree) {
		Message message = tree.getMessage();

		if (message != null) {
			return message.getTimestamp();
		} else if (!tree.getMetrics().isEmpty()) {
			return tree.getMetrics().get(0).getTimestamp();
		} else {
			return 0;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_analyzerManager == null) {
			throw new InitializationException("MessageAnalyzerManager is required for RealtimeConsumer.");
		}
		if (m_serverStateManager == null) {
			throw new InitializationException("ServerStatisticManager is required for RealtimeConsumer.");
		}

		m_periodManager = new PeriodManager(HOUR, m_analyzerManager, m_serverStateManager, m_logger);
		m_periodManager.init();

		Threads.forGroup("Cat").start(m_periodManager);
	}

	private void info(String message) {
		if (m_logger != null) {
			m_logger.info(message);
		} else {
			SLF4J_LOGGER.info(message);
		}
	}

	public void setAnalyzerManager(MessageAnalyzerManager analyzerManager) {
		m_analyzerManager = analyzerManager;
	}

	public void setServerStateManager(ServerStatisticManager serverStateManager) {
		m_serverStateManager = serverStateManager;
	}

}
