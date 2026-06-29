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
package com.dianping.cat.consumer.top;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.ContainerMessageAnalyzerFactory;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(ContainerMessageAnalyzerFactory.ANALYZER_BEAN_PREFIX + TopAnalyzer.ID)
@Scope("prototype")
public class TopAnalyzer extends AbstractMessageAnalyzer<TopReport> {
	public static final String ID = "top";

	@Resource(name = TopAnalyzer.ID + "ReportManager")
	private ReportManager<TopReport> topReportManager;

	@Resource(name = "serverFilterConfigManager")
	private ServerFilterConfigManager serverFilterConfigManager;

	private Set<String> errorTypes;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		long startTime = getStartTime();

		if (atEnd && !isLocalMode()) {
			topReportManager.storeHourlyReports(startTime, StoragePolicy.FILE_AND_DB, m_index);
		} else {
			topReportManager.storeHourlyReports(startTime, StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public TopReport getReport(String domain) {
		return topReportManager.getHourlyReport(getStartTime(), Constants.CAT, false);
	}

	@Override
	public ReportManager<TopReport> getReportManager() {
		return topReportManager;
	}

	@Override
	public boolean isEligible(MessageTree tree) {
		if (tree.getEvents().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void loadReports() {
		topReportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();

		if (serverFilterConfigManager.validateDomain(domain)) {
			TopReport report = topReportManager.getHourlyReport(getStartTime(), Constants.CAT, true);

			List<Event> events = tree.getEvents();

			for (Event e : events) {
				processEvent(report, tree, e);
			}
		}
	}

	private void processEvent(TopReport report, MessageTree tree, Event event) {
		String type = event.getType();

		if (errorTypes.contains(type)) {
			String domain = tree.getDomain();
			String ip = tree.getIpAddress();
			String exception = event.getName();
			long current = event.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Segment segment = report.findOrCreateDomain(domain).findOrCreateSegment(min).incError();

			segment.findOrCreateError(exception).incCount();
			segment.findOrCreateMachine(ip).incCount();
		}
	}

	@Value("Error,RuntimeException,Exception")
	public void setErrorType(String type) {
		errorTypes = Stream.of(type.split(",")).map(String::trim).filter(item -> !item.isEmpty()).collect(Collectors.toSet());
	}

	public void setReportManager(ReportManager<TopReport> reportManager) {
		topReportManager = reportManager;
	}

	public void setServerFilterConfigManager(ServerFilterConfigManager serverFilterConfigManager) {
		this.serverFilterConfigManager = serverFilterConfigManager;
	}

	@Override
	@Resource(name = "serverConfigManager")
	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		super.setServerConfigManager(serverConfigManager);
	}
}
