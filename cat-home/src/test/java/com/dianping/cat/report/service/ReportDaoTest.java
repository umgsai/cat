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
package com.dianping.cat.report.service;

import java.util.Date;

import org.junit.Test;

import com.dianping.cat.SpringCatHomeTestSupport;
import com.dianping.cat.mybatis.data.HourlyReportDO;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.mybatis.data.WeeklyReportDO;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.mybatis.HourlyReportRepository;
import com.dianping.cat.mybatis.MonthlyReportRepository;
import com.dianping.cat.mybatis.WeeklyReportRepository;
import com.dianping.cat.mybatis.DailyReportRepository;

public class ReportDaoTest extends SpringCatHomeTestSupport {

	@Test
	public void test() throws Exception {
		HourlyReportRepository dao = lookup(HourlyReportRepository.class);
		HourlyReportDO proto = new HourlyReportDO();

		proto.setCreateTime(new Date());
		proto.setDomain("domain");
		proto.setName("name");
		proto.setIp("ip");
		proto.setPeriod(new Date());
		proto.setType(1);
		dao.insert(proto);
	}

	@Test
	public void testDaily() throws Exception {
		DailyReportRepository dao = lookup(DailyReportRepository.class);
		DailyReportDO proto = new DailyReportDO();

		proto.setCreateTime(new Date());
		proto.setDomain("domain");
		proto.setName("name");
		proto.setIp("ip");
		proto.setPeriod(new Date());
		proto.setType(1);
		dao.insert(proto);
	}

	@Test
	public void testWeek() throws Exception {
		WeeklyReportRepository dao = lookup(WeeklyReportRepository.class);
		WeeklyReportDO proto = new WeeklyReportDO();

		proto.setCreateTime(new Date());
		proto.setDomain("domain");
		proto.setName("name");
		proto.setIp("ip");
		proto.setPeriod(new Date());
		proto.setType(1);
		dao.insert(proto);
	}

	@Test
	public void testMonth() throws Exception {
		MonthlyReportRepository dao = lookup(MonthlyReportRepository.class);
		MonthlyReport proto = new MonthlyReport();

		proto.setCreationDate(new Date());
		proto.setDomain("domain");
		proto.setName("name");
		proto.setIp("ip");
		proto.setPeriod(new Date());
		proto.setType(1);
		dao.insert(proto);
	}

}
