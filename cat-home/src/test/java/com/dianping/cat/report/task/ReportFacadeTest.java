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
package com.dianping.cat.report.task;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ReportFacadeTest {

	@Test
	@SuppressWarnings("unchecked")
	public void testSpringBuilderMapUsesBuilderIdAlias() throws Exception {
		ReportFacade facade = new ReportFacade();
		TaskBuilder builder = new AliasTaskBuilder();
		Map<String, TaskBuilder> springBuilders = new HashMap<String, TaskBuilder>();

		springBuilders.put("aliasTaskBuilder", builder);

		Method method = ReportFacade.class.getDeclaredMethod("buildReportBuilderMap", Map.class);
		method.setAccessible(true);
		Map<String, TaskBuilder> reportBuilders = (Map<String, TaskBuilder>) method.invoke(facade, springBuilders);

		Assert.assertSame(builder, reportBuilders.get("aliasTaskBuilder"));
		Assert.assertSame(builder, reportBuilders.get(AliasTaskBuilder.ID));
	}

	public static class AliasTaskBuilder implements TaskBuilder {
		public static final String ID = "alias-report";

		@Override
		public boolean buildDailyTask(String name, String domain, Date period) {
			return true;
		}

		@Override
		public boolean buildHourlyTask(String name, String domain, Date period) {
			return true;
		}

		@Override
		public boolean buildMonthlyTask(String name, String domain, Date period) {
			return true;
		}

		@Override
		public boolean buildWeeklyTask(String name, String domain, Date period) {
			return true;
		}
	}
}
