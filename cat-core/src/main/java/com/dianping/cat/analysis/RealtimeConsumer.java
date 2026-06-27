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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.support.Threads;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

@Component("messageConsumer")
public class RealtimeConsumer implements MessageConsumer {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(RealtimeConsumer.class);

	public static final long MINUTE = 60 * 1000L;

	public static final long HOUR = 60 * MINUTE;

	@Resource(name = "messageAnalyzerManager")
	private MessageAnalyzerManager messageAnalyzerManager;

	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager serverStatisticManager;

	private PeriodManager periodManager;

	private volatile boolean initialized;

	@Override
	public void consume(MessageTree tree) {
		initialize();

		long timestamp = getTimestamp(tree);
		Period period = periodManager.findPeriod(timestamp);

		if (period != null) {
			period.distribute(tree);
		} else {
			SLF4J_LOGGER.warn("No realtime period found for message tree, timestamp={}, tree={}.", timestamp, tree);
			serverStatisticManager.addNetworkTimeError(1);
		}
	}

	public void doCheckpoint() {
		initialize();

		info("starting do checkpoint.");
		Transaction t = Cat.newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			long currentStartTime = getCurrentStartTime();
			Period period = periodManager.findPeriod(currentStartTime);

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
	public List<MessageAnalyzer> getCurrentAnalyzer(String name) {
		initialize();

		long currentStartTime = getCurrentStartTime();
		Period period = periodManager.findPeriod(currentStartTime);

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
		initialize();

		long lastStartTime = getCurrentStartTime() - HOUR;
		Period period = periodManager.findPeriod(lastStartTime);

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

	@PostConstruct
	public synchronized void initialize() {
		if (initialized) {
			return;
		}

		if (messageAnalyzerManager == null) {
			throw new IllegalStateException("MessageAnalyzerManager is required for RealtimeConsumer.");
		}
		if (serverStatisticManager == null) {
			throw new IllegalStateException("ServerStatisticManager is required for RealtimeConsumer.");
		}

		periodManager = new PeriodManager(HOUR, messageAnalyzerManager, serverStatisticManager);
		periodManager.init();

		Threads.forGroup("Cat").start(periodManager);
		initialized = true;
		SLF4J_LOGGER.info("Initialized realtime consumer.");
	}

	private void info(String message) {
		SLF4J_LOGGER.info(message);
	}

	public void setAnalyzerManager(MessageAnalyzerManager analyzerManager) {
		messageAnalyzerManager = analyzerManager;
	}

	public void setServerStateManager(ServerStatisticManager serverStateManager) {
		serverStatisticManager = serverStateManager;
	}

}
