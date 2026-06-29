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
package com.dianping.cat.consumer.state;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.statistic.ServerStatisticManager;

public class StateAnalyzerTest {

	private StateAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void setUp() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss:SS");
		Date date = sdf.parse("20120101 00:00:00:00");

		m_analyzer = createAnalyzer();
		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		StateReport report = m_analyzer.getReport(m_domain);

		String expected = new String(getClass().getResourceAsStream("state_analyzer.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	private StateAnalyzer createAnalyzer() {
		StateAnalyzer analyzer = new StateAnalyzer();

		analyzer.setMIp("192.168.1.1");
		analyzer.setProjectService(new MockProjectService());
		analyzer.setReportManager(new MockStateReportManager());
		analyzer.setServerFilterConfigManager(new MockServerFilterConfigManager());
		analyzer.setServerStateManager(new ServerStatisticManager());
		return analyzer;
	}

	private static class MockProjectService extends ProjectService {

		@Override
		public Project findProject(String domain) {
			return null;
		}

		@Override
		public boolean insert(String domain) {
			return true;
		}
	}

	private static class MockServerFilterConfigManager extends ServerFilterConfigManager {

		@Override
		public boolean validateDomain(String domain) {
			return true;
		}
	}

	private static class MockStateReportManager extends MockReportManager<StateReport> {
		private final ReportDelegate<StateReport> m_delegate = new StateDelegate();

		private StateReport m_report;

		@Override
		public StateReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		@Override
		public void destroy() {
		}
	}
}
