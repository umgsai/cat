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
package com.dianping.cat.consumer.heartbeat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.DefaultMessageTree;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportDelegate;

public class HeartbeatAnalyzerTest {

	private long m_timestamp;

	private HeartbeatAnalyzer m_analyzer;

	private String m_domain = "group";

	private String m_status;

	@Before
	public void setUp() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_timestamp = date.getTime();

		m_analyzer = createAnalyzer();
		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 10; i++) {
			MessageTree tree = ((DefaultMessageTree) generateMessageTree(i)).copyForTest();

			m_analyzer.process(tree);
		}

		HeartbeatReport report = m_analyzer.getReport(m_domain);

		String expected = new String(getClass().getResourceAsStream("heartbeat_analyzer.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) throws IOException {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t = new DefaultTransaction("A", "n" + i % 2);

		Heartbeat heartbeat = newHeartbeat("heartbeat", "fail", m_timestamp + i * 1000 * 60, "0");

		t.addChild(heartbeat);

		t.setCompleted();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		tree.setMessage(t);

		return tree;
	}

	private Heartbeat newHeartbeat(String type, String name, long timestamp, String status) throws IOException {
		DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);

		heartbeat.setStatus(status);
		heartbeat.setTimestamp(timestamp);

		if (m_status == null) {
			m_status = new String(getClass().getResourceAsStream("status_info.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		}

		heartbeat.addData(m_status);

		return heartbeat;
	}

	private HeartbeatAnalyzer createAnalyzer() {
		HeartbeatAnalyzer analyzer = new HeartbeatAnalyzer();

		analyzer.setReportManager(new MockHeartbeatReportManager());
		analyzer.setServerFilterConfigManager(new MockServerFilterConfigManager());
		return analyzer;
	}

	private static class MockHeartbeatReportManager extends MockReportManager<HeartbeatReport> {
		private final ReportDelegate<HeartbeatReport> m_delegate = new HeartbeatDelegate();

		private HeartbeatReport m_report;

		@Override
		public HeartbeatReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		@Override
		public void destory() {
		}
	}

	private static class MockServerFilterConfigManager extends ServerFilterConfigManager {

		@Override
		public boolean validateDomain(String domain) {
			return true;
		}
	}
}
