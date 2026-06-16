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
package com.dianping.cat.consumer.transaction;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Constants;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.TestHelper;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.DefaultMessageTree;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportDelegate;

public class TransactionAnalyzerTest {
	private long m_timestamp;

	private TransactionAnalyzer m_analyzer;

	private String m_domain = "group";
	
	public static void main(String[] args) {
		try {
			System.out.println("==>");
		TransactionAnalyzerTest test = new TransactionAnalyzerTest();
		for (int i = 1; i <= 1000; i++) {
			MessageTree tree = ((DefaultMessageTree) test.generateMessageTree(i)).copyForTest();
			System.out.println("==>"+i+" "+tree);
		}
		
		}catch(Throwable  ex) {
			ex.printStackTrace();
		}
	}

	@Before
	public void setUp() throws Exception {
		m_timestamp = System.currentTimeMillis() - System.currentTimeMillis() % (3600 * 1000);
		m_analyzer = createAnalyzer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 1000; i++) {
			MessageTree tree = ((DefaultMessageTree) generateMessageTree(i)).copyForTest();

			m_analyzer.process(tree);
		}

		TransactionReport report = m_analyzer.getReport(m_domain);

		report.accept(new TransactionStatisticsComputer());

		String expected = new String(getClass().getResourceAsStream("transaction_analyzer.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		Assert.assertTrue( TestHelper.isEquals(com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser.parse(expected), report));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t = new DefaultTransaction("A", "n" + i % 2);
		DefaultTransaction t2 = new DefaultTransaction("A-1", "n" + i % 3);

		if (i % 2 == 0) {
			t2.setStatus("ERROR");
		} else {
			t2.setStatus(Message.SUCCESS);
		}

		t2.setCompleted();
		t2.setDurationInMillis(i);

		t.addChild(t2);

		if (i % 2 == 0) {
			t.setStatus("ERROR");
		} else {
			t.setStatus(Message.SUCCESS);
		}

		t.setCompleted();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		t2.setTimestamp(m_timestamp + 2000);
		tree.setMessage(t);

		return tree;
	}

	private TransactionAnalyzer createAnalyzer() {
		TransactionAnalyzer analyzer = new TransactionAnalyzer();

		analyzer.setAtomicMessageConfigManager(new MockAtomicMessageConfigManager());
		analyzer.setFilterConfigManager(new MockServerFilterConfigManager());
		analyzer.setReportManager(new MockTransactionReportManager());
		analyzer.setServerConfigManager(new MockServerConfigManager());
		analyzer.setStatisticManager(new MockTpValueStatisticConfigManager());
		return analyzer;
	}

	private static class MockAtomicMessageConfigManager extends AtomicMessageConfigManager {

		@Override
		public int getMaxNameThreshold(String domain) {
			return 200;
		}
	}

	private static class MockServerConfigManager extends ServerConfigManager {

		@Override
		public int getMaxTypeThreshold() {
			return 100;
		}

		@Override
		public int getTpValueExpireMinute() {
			return 1;
		}

		@Override
		public int getTypeNameLengthLimit() {
			return 256;
		}
	}

	private static class MockServerFilterConfigManager extends ServerFilterConfigManager {

		@Override
		public boolean discardTransaction(String type, String name) {
			return false;
		}
	}

	private static class MockTpValueStatisticConfigManager extends TpValueStatisticConfigManager {

		@Override
		public boolean shouldStatistic(String type, String domain) {
			return false;
		}
	}

	private static class MockTransactionReportManager extends MockReportManager<TransactionReport> {
		private final ReportDelegate<TransactionReport> m_delegate = new TransactionDelegate();

		private TransactionReport m_report;

		@Override
		public TransactionReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		@Override
		public void destory() {
		}
	}
}
