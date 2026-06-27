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
package com.dianping.cat.consumer.problem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

@Component(LongExecutionProblemHandler.ID)
public class LongExecutionProblemHandler extends ProblemHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(LongExecutionProblemHandler.class);

	public static final String ID = "long-execution";

	@Resource
	private ServerConfigManager serverConfigManager;

	private int[] defaultLongServiceDuration = { 50, 100, 500, 1000, 3000, 5000 };

	private int[] defaultLongSqlDuration = { 100, 500, 1000, 3000, 5000 };

	private int[] defaultLongUrlDuration = { 1000, 2000, 3000, 5000 };

	private int[] defaultLongCallDuration = { 100, 500, 1000, 3000, 5000 };

	private int[] defaultLongCacheDuration = { 10, 50, 100, 500 };

	private Map<String, Integer> longServiceThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> longSqlThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> longUrlThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> longCallThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> longCacheThresholds = new HashMap<String, Integer>();

	private volatile boolean initialized;

	public int computeLongDuration(long duration, String domain, int[] defaultLongDuration,
							Map<String, Integer> longThresholds) {
		int[] messageDuration = defaultLongDuration;

		for (int i = messageDuration.length - 1; i >= 0; i--) {
			if (duration >= messageDuration[i]) {
				return messageDuration[i];
			}
		}

		Integer value = longThresholds.get(domain);

		if (value != null && duration >= value) {
			return value;
		} else {
			return -1;
		}
	}

	@Override
	public void handle(Machine machine, MessageTree tree) {
		ensureInitialized();

		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			processTransaction(machine, transaction, tree);
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	public synchronized void initialize() {
		if (initialized) {
			return;
		}
		if (serverConfigManager == null) {
			LOGGER.warn("Server config manager is not configured for long execution problem handler.");
			initialized = true;
			return;
		}
		Map<String, Domain> domains = serverConfigManager.getLongConfigDomains();

		longServiceThresholds.clear();
		longUrlThresholds.clear();
		longSqlThresholds.clear();

		for (Domain domain : domains.values()) {
			Integer serviceThreshold = domain.getServiceThreshold();
			Integer urlThreshold = domain.getUrlThreshold();
			Integer sqlThreshold = domain.getSqlThreshold();

			if (serviceThreshold != null) {
				longServiceThresholds.put(domain.getName(), serviceThreshold);
			}
			if (urlThreshold != null) {
				longUrlThresholds.put(domain.getName(), urlThreshold);
			}
			if (sqlThreshold != null) {
				longSqlThresholds.put(domain.getName(), sqlThreshold);
			}
		}
		initialized = true;
	}

	private void processLongCache(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = ((Transaction) transaction).getDurationInMillis();
		long nomarizeDuration = computeLongDuration(duration, tree.getDomain(), defaultLongCacheDuration,
								longCacheThresholds);

		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_CACHE.getName();
			String status = transaction.getName();
			Entity entity = findOrCreateEntity(machine, type, status);

			updateEntity(tree, entity, (int) nomarizeDuration);
		}
	}

	private void processLongCall(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = transaction.getDurationInMillis();
		String domain = tree.getDomain();

		long nomarizeDuration = computeLongDuration(duration, domain, defaultLongCallDuration, longCallThresholds);
		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_CALL.getName();
			String status = transaction.getName();
			Entity entity = findOrCreateEntity(machine, type, status);

			updateEntity(tree, entity, (int) nomarizeDuration);
		}
	}

	private void processLongService(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = transaction.getDurationInMillis();
		String domain = tree.getDomain();
		long nomarizeDuration = computeLongDuration(duration, domain, defaultLongServiceDuration, longServiceThresholds);

		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_SERVICE.getName();
			String status = transaction.getName();
			Entity entity = findOrCreateEntity(machine, type, status);

			updateEntity(tree, entity, (int) nomarizeDuration);
		}
	}

	private void processLongSql(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = transaction.getDurationInMillis();
		String domain = tree.getDomain();

		long nomarizeDuration = computeLongDuration(duration, domain, defaultLongSqlDuration, longSqlThresholds);
		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_SQL.getName();
			String status = transaction.getName();
			Entity problem = findOrCreateEntity(machine, type, status);

			updateEntity(tree, problem, (int) nomarizeDuration);
		}
	}

	private void processLongUrl(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = (transaction).getDurationInMillis();
		String domain = tree.getDomain();

		long nomarizeDuration = computeLongDuration(duration, domain, defaultLongUrlDuration, longUrlThresholds);
		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_URL.getName();
			String status = transaction.getName();
			Entity problem = findOrCreateEntity(machine, type, status);

			updateEntity(tree, problem, (int) nomarizeDuration);
		}
	}

	private void processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		String type = transaction.getType();

		if (type.startsWith("Cache.")) {
			processLongCache(machine, transaction, tree);
		} else if (type.equals("SQL")) {
			processLongSql(machine, transaction, tree);
		} else if (serverConfigManager.isRpcClient(type)) {
			processLongCall(machine, transaction, tree);
		} else if (serverConfigManager.isRpcServer(type)) {
			processLongService(machine, transaction, tree);
		} else if ("URL".equals(type)) {
			processLongUrl(machine, transaction, tree);
		}

		List<Message> messageList = transaction.getChildren();

		for (Message message : messageList) {
			if (message instanceof Transaction) {
				processTransaction(machine, (Transaction) message, tree);
			}
		}
	}

	public void setConfigManager(ServerConfigManager configManager) {
		serverConfigManager = configManager;
	}

}
