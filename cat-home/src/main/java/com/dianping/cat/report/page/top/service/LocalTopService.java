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
package com.dianping.cat.report.page.top.service;

import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopReportMerger;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.spring.CatSpringContext;

public class LocalTopService extends LocalModelService<TopReport> {

	public static final String ID = TopAnalyzer.ID;

	private ReportBucketManager m_bucketManager;

	public LocalTopService() {
		super(TopAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		List<TopReport> reports = super.getReport(period, domain);
		TopReport report = null;

		if (reports != null) {
			report = new TopReport(domain);
			TopReportMerger merger = new TopReportMerger(report);

			for (TopReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getDomains().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

			if (report == null) {
				report = new TopReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeHelper.ONE_HOUR - 1));
			}
		}
		if (report == null) {
			report = new TopReport(domain);
			report.setStartTime(new Date(request.getStartTime()));
			report.setEndTime(new Date(request.getStartTime() + TimeHelper.ONE_HOUR - 1));
		}
		return new TopReportFilter().buildXml(report);
	}

	private TopReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		TopReport report = new TopReport(domain);
		TopReportMerger merger = new TopReportMerger(report);
		ReportBucketManager bucketManager = getBucketManager();

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		if (bucketManager == null) {
			return report;
		}
		for (int i = 0; i < getAnalyzerCount(); i++) {
			ReportBucket bucket = null;
			try {
				bucket = bucketManager.getReportBucket(timestamp, TopAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					TopReport tmp = DefaultSaxParser.parse(xml);

					tmp.accept(merger);
				}
			} finally {
				if (bucket != null) {
					bucketManager.closeBucket(bucket);
				}
			}
		}
		return report;
	}

	private ReportBucketManager getBucketManager() {
		if (m_bucketManager == null) {
			m_bucketManager = CatSpringContext.getBeanIfAvailable(ReportBucketManager.class);
		}
		return m_bucketManager;
	}

	public void setBucketManager(ReportBucketManager bucketManager) {
		m_bucketManager = bucketManager;
	}

	public static class TopReportFilter extends com.dianping.cat.consumer.top.model.transform.DefaultXmlBuilder {
		public TopReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}
}
