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
package com.dianping.cat.report.manager;

import jakarta.annotation.Resource;

import com.dianping.cat.mybatis.HourlyReportContentRepository;
import com.dianping.cat.mybatis.HourlyReportRepository;
import com.dianping.cat.report.DefaultReportManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;

public abstract class AbstractSpringReportManager<T> extends DefaultReportManager<T> {
	@Resource(name = "reportBucketManager")
	private ReportBucketManager reportBucketManager;

	@Resource(name = "hourlyReportRepository")
	private HourlyReportRepository hourlyReportRepository;

	@Resource(name = "hourlyReportContentRepository")
	private HourlyReportContentRepository hourlyReportContentRepository;

	@Resource(name = "domainValidator")
	private DomainValidator domainValidator;

	protected void configure(String reportName, ReportDelegate<T> reportDelegate) {
		setReportDelegate(reportDelegate);
		setBucketManager(reportBucketManager);
		setReportDao(hourlyReportRepository);
		setReportContentDao(hourlyReportContentRepository);
		setValidator(domainValidator);
		setName(reportName);
		super.initialize();
	}
}
