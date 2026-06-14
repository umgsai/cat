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
package com.dianping.cat.report.page.event;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.TestHelper;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.event.service.LocalEventService.EventReportFilter;

public class EventReportFilterTest {
	@Test
	public void test() throws Exception {
		String source = new String(getClass().getResourceAsStream("event_filter.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		EventReport report = DefaultSaxParser.parse(source);

		EventReportFilter f1 = new EventReportFilter(null, null, null);
		String expected1 = new String(getClass().getResourceAsStream("event_filter_type.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

		Assert.assertTrue(TestHelper.isEquals(DefaultSaxParser.parse(expected1), DefaultSaxParser.parse(f1.buildXml(report))));

		EventReportFilter f2 = new EventReportFilter("URL", null, null);
		String expected2 = new String(getClass().getResourceAsStream("event_filter_name.xml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

		Assert.assertTrue(TestHelper.isEquals(DefaultSaxParser.parse(expected2), DefaultSaxParser.parse(f2.buildXml(report))));
	}
}
