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
package com.dianping.cat.consumer.cross;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.DefaultMessageTree;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportDelegate;

public class CrossAnalyzerTest {

	private long m_timestamp;

	private CrossAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void setUp() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);

		m_analyzer = createAnalyzer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 100; i++) {
			MessageTree tree = ((DefaultMessageTree) generateMessageTree(i)).copyForTest();

			m_analyzer.process(tree);
		}

		CrossReport report = m_analyzer.getReport(m_domain);
		String expected = new String(getClass().getResourceAsStream("cross_analyzer.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));

		CrossReport reportCaller = m_analyzer.getReport("server");
		String expectedCaller = new String(getClass().getResourceAsStream("cross_analyzer_caller.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		Assert.assertEquals(expectedCaller.replaceAll("\r", ""), reportCaller.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t;

		if (i % 2 == 0) {
			t = new DefaultTransaction("PigeonCall", "Cat-Test-Call");
			DefaultEvent event = new DefaultEvent("PigeonCall.server", "192.168.1.0:3000:class:method1");

			event.setTimestamp(m_timestamp + 5 * 60 * 1000);
			event.setStatus(Message.SUCCESS);
			t.addChild(event);

			DefaultEvent eventApp = new DefaultEvent("PigeonCall.app", "server");

			eventApp.setTimestamp(m_timestamp + 5 * 60 * 1000 + 100);
			eventApp.setStatus(Message.SUCCESS);
			t.addChild(eventApp);
		} else {
			t = new DefaultTransaction("PigeonService", "Cat-Test-Service");
			DefaultEvent event = new DefaultEvent("PigeonService.client", "192.168.1.2:3000:class:method2");

			event.setTimestamp(m_timestamp + 5 * 60 * 1000);
			event.setStatus(Message.SUCCESS);
			t.addChild(event);

			DefaultEvent eventApp = new DefaultEvent("PigeonService.app", "client");

			eventApp.setTimestamp(m_timestamp + 5 * 60 * 1000 + 100);
			eventApp.setStatus(Message.SUCCESS);
			t.addChild(eventApp);
		}

		t.setCompleted();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		tree.setMessage(t);

		return tree;
	}

	@Test
	public void testFormatIp() {
		IpConvertManager analyzer = new IpConvertManager();

		Assert.assertEquals(true, analyzer.isIPAddress("10.1.6.128"));
		Assert.assertEquals(false, analyzer.isIPAddress("10.1.6.328"));
		Assert.assertEquals(false, analyzer.isIPAddress("2886.1.6.128"));
		Assert.assertEquals(false, analyzer.isIPAddress("2886.1.6.1228"));

		Assert.assertEquals("10.1.6.128", analyzer.convertHostNameToIP("10.1.6.128"));
	}

	private CrossAnalyzer createAnalyzer() {
		CrossAnalyzer analyzer = new CrossAnalyzer();

		analyzer.setIpConvertManager(new IpConvertManager());
		analyzer.setReportManager(new MockCrossReportManager());
		analyzer.setServerConfigManager(new MockServerConfigManager());
		return analyzer;
	}

	private static class MockCrossReportManager extends MockReportManager<CrossReport> {
		private final ReportDelegate<CrossReport> m_delegate = new CrossDelegate();

		private Map<Long, Map<String, CrossReport>> m_reports = new ConcurrentHashMap<Long, Map<String, CrossReport>>();

		@Override
		public CrossReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			Map<String, CrossReport> reports = m_reports.get(startTime);

			if (reports == null && createIfNotExist) {
				reports = new ConcurrentHashMap<String, CrossReport>();
				m_reports.put(startTime, reports);
			}

			CrossReport report = reports.get(domain);

			if (report == null && createIfNotExist) {
				report = m_delegate.makeReport(domain, startTime, Constants.HOUR);
				reports.put(domain, report);
			}
			return report;
		}

		@Override
		public void destroy() {
		}
	}

	private static class MockServerConfigManager extends ServerConfigManager {

		@Override
		public boolean isRpcClient(String type) {
			return "PigeonCall".equals(type) || "Call".equals(type);
		}

		@Override
		public boolean isRpcServer(String type) {
			return "PigeonService".equals(type) || "Service".equals(type);
		}
	}

}
