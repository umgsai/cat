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
package com.dianping.cat.report.page.statistics.service;

import java.util.Date;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;

@Component
public class ClientReportService extends AbstractReportService<ClientReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientReportService.class);

	@Override
	public ClientReport makeReport(String domain, Date start, Date end) {
		ClientReport report = new ClientReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public ClientReport queryDailyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		String name = Constants.REPORT_CLIENT;

		try {
			DailyReport report = dailyReportRepository
									.findByDomainNamePeriod(domain, name, new Date(startTime));
			return queryFromDailyBinary(report.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Client daily report is missing, domain={}, period={}.", domain, new Date(startTime), e);
		} catch (Exception e) {
			LOGGER.error("Unable to query client daily report, domain={}, period={}.", domain, new Date(startTime), e);
			Cat.logError(e);
		}
		ClientReport report = new ClientReport(Constants.CAT);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	private ClientReport queryFromDailyBinary(long id, String domain) {
		DailyReportContent content = dailyReportContentRepository.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new ClientReport(domain);
		}
	}

	@Override
	public ClientReport queryHourlyReport(String domain, Date start, Date end) {
		throw new RuntimeException("Client report service do not suppot queryHourlyReport feature");
	}

	@Override
	public ClientReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("Client report service do not suppot queryMonthlyReport feature");
	}

	@Override
	public ClientReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("Client report service do not suppot queryWeeklyReport feature");
	}

}
