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
package com.dianping.cat.report.page.cross;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.cross.CrossReportMerger;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;

public class CrossReportMergerTest {
	@Test
	public void testCrossReportMerge() throws Exception {
		String oldXml = new String(getClass().getResourceAsStream("CrossReportOld.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		String newXml = new String(getClass().getResourceAsStream("CrossReportNew.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		CrossReport reportOld = DefaultSaxParser.parse(oldXml);
		CrossReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = new String(getClass().getResourceAsStream("CrossReportMergeResult.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""),
								merger.getCrossReport().toString().replaceAll("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\r", ""),
								reportNew.toString().replaceAll("\r", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\r", ""),
								reportOld.toString().replaceAll("\r", ""));
	}
}
