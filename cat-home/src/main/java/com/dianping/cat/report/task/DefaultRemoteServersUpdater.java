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

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.server.ServersUpdater;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Component("remoteServersUpdater")
public class DefaultRemoteServersUpdater implements ServersUpdater {

	@Resource(name = "stateModelService")
	private ModelService<StateReport> stateModelService;

	@Resource(name = "localStateService")
	private LocalModelService<StateReport> localStateService;

	@Override
	public Map<String, Set<String>> buildServers(Date hour) {
		StateReport currentReport = queryStateReport(Constants.CAT, hour.getTime());
		StateReportVisitor visitor = new StateReportVisitor();

		if (currentReport != null) {
			visitor.visitStateReport(currentReport);
		}
		
		return visitor.getServers();
	}

	public StateReport queryStateReport(String domain, long time) {
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(domain, time);

			if (localStateService != null && localStateService.isEligible(request)) {
				try {
					String xml = localStateService.getReport(request, period, domain, new ApiPayload());

					return DefaultSaxParser.parse(xml);
				} catch (Exception e) {
					throw new RuntimeException("Unable to build local state report for " + request + "!", e);
				}
			} else if (stateModelService != null && stateModelService.isEligible(request)) {
				ModelResponse<StateReport> response = stateModelService.invoke(request);
				StateReport report = response.getModel();

				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable state report service registered for " + request	+ "!");
			}
		} else {
			throw new RuntimeException("Domain server update period is not right: " + period + ", time is: "	+ new Date(time));
		}
	}

	public void setService(ModelService<StateReport> service) {
		stateModelService = service;
	}

	public void setLocalService(LocalModelService<StateReport> localService) {
		localStateService = localService;
	}

	public static class StateReportVisitor extends BaseVisitor {

		private Map<String, Set<String>> servers = new ConcurrentHashMap<String, Set<String>>();

		private String ip;

		public Map<String, Set<String>> getServers() {
			return servers;
		}

		@Override
		public void visitMachine(Machine machine) {
			ip = machine.getIp();
			super.visitMachine(machine);
		}

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			if (processDomain.getTotal() > 0) {
				String domain = processDomain.getName();
				Set<String> currentServers = servers.get(domain);

				if (currentServers == null) {
					currentServers = new HashSet<String>();

					servers.put(domain, currentServers);
				}
				currentServers.add(ip);
			}
		}
	}

}
