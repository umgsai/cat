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
package com.dianping.cat.report.task.utilization;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultSaxParser;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportMerger;

public class UtilizationReportMergerTest {
	@Test
	public void testUtilizationReportMerge() throws Exception {
		String oldXml = new String(getClass().getResourceAsStream("UtilizationReportOld.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		String newXml = new String(getClass().getResourceAsStream("UtilizationReportNew.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		UtilizationReport reportOld = DefaultSaxParser.parse(oldXml);
		UtilizationReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = new String(getClass().getResourceAsStream("UtilizationReportResult.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""),
								merger.getUtilizationReport().toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}

}
