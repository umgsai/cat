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
package com.dianping.cat.consumer.dump;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.ContainerMessageAnalyzerFactory;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.tree.MessageId;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.support.Threads;
import jakarta.annotation.Resource;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperManager;
import org.unidal.cat.message.storage.MessageFinderManager;

import java.util.concurrent.TimeUnit;

@Component(ContainerMessageAnalyzerFactory.ANALYZER_BEAN_PREFIX + DumpAnalyzer.ID)
@Scope("prototype")
public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DumpAnalyzer.class);

	public static final String ID = "dump";


	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager serverStatisticManager;

	@Resource(name = "messageDumperManager")
	private MessageDumperManager dumperManager;

	@Resource(name = "messageFinderManager")
	private MessageFinderManager finderManager;

	private int discardSize = 50000000;

	@Override
	@Resource(name = "serverConfigManager")
	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		super.setServerConfigManager(serverConfigManager);
	}

	private void closeStorage() {
		int hour = (int) TimeUnit.MILLISECONDS.toHours(m_startTime);
		Transaction t = Cat.newTransaction("Dumper", "Storage" + hour);

		try {
			finderManager.close(hour);
			dumperManager.close(hour);
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			LOGGER.error("Unable to close message storage, hour={}.", hour, e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd) {
			Threads.forGroup("cat").start(new Runnable() {
				@Override
				public void run() {
					closeStorage();
				}
			});
		} else {
			closeStorage();
		}
	}

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
	}

	@Override
	public ReportManager<?> getReportManager() {
		return null;
	}

	@Override
	public void initialize(long startTime, long duration, long extraTime) {
		super.initialize(startTime, duration, extraTime);
		int hour = (int) TimeUnit.MILLISECONDS.toHours(startTime);

		dumperManager.findOrCreate(hour);
	}

	@Override
	protected void loadReports() {
		// do nothing
	}

	@Override
	public void process(MessageTree tree) {
		try {
			MessageId messageId = MessageId.parse(tree.getMessageId());

			if (!shouldDiscard(messageId)) {
				processWithStorage(tree, messageId, messageId.getHour());
			}
		} catch (Exception ignored) {
		}
	}

	private void processWithStorage(MessageTree tree, MessageId messageId, int hour) {
		MessageDumper dumper = dumperManager.find(hour);

		tree.setFormatMessageId(messageId);

		if (dumper != null) {
			dumper.process(tree);
		} else {
			serverStatisticManager.addPigeonTimeError(1);
		}
	}

	public void setDumperManager(MessageDumperManager dumperManager) {
		this.dumperManager = dumperManager;
	}

	public void setFinderManager(MessageFinderManager finderManager) {
		this.finderManager = finderManager;
	}

	public void setServerStatisticManager(ServerStatisticManager serverStatisticManager) {
		this.serverStatisticManager = serverStatisticManager;
	}

	private boolean shouldDiscard(MessageId id) {
		int index = id.getIndex();

		return index > discardSize;
	}

}
