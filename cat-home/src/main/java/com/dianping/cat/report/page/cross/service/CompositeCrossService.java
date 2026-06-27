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
package com.dianping.cat.report.page.cross.service;

import java.util.Collections;
import java.util.List;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossReportMerger;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component("crossModelService")
public class CompositeCrossService extends BaseCompositeModelService<CrossReport> {
	@Resource(name = "cross-historical")
	private ModelService<CrossReport> historicalCrossService;

	public CompositeCrossService() {
		super(CrossAnalyzer.ID);
	}

	@Override
	@PostConstruct
	public synchronized void initialize() {
		if (historicalCrossService != null) {
			setServices(Collections.singletonList(historicalCrossService));
		}
		super.initialize();
	}

	@Override
	protected BaseRemoteModelService<CrossReport> createRemoteService() {
		return new RemoteCrossService();
	}

	@Override
	protected CrossReport merge(ModelRequest request, List<ModelResponse<CrossReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(request.getDomain()));
		for (ModelResponse<CrossReport> response : responses) {
			CrossReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getCrossReport();
	}
}
