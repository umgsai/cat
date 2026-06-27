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
package com.dianping.cat.report.alert.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.dianping.cat.support.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.alert.exception.AlertExceptionBuilder.AlertException;
import com.dianping.cat.report.page.dependency.TopExceptionExclude;
import com.dianping.cat.report.page.dependency.TopMetric;
import com.dianping.cat.report.page.dependency.TopMetric.Item;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

import jakarta.annotation.Resource;

@Component
public class ExceptionAlert implements Task {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAlert.class);

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	protected static final int ALERT_PERIOD = 1;

	@Resource
	protected ExceptionRuleConfigManager exceptionRuleConfigManager;

	@Resource
	protected AlertExceptionBuilder alertExceptionBuilder;

	@Resource(name = "topModelService")
	protected ModelService<TopReport> topModelService;

	@Resource(name = "spiAlertManager")
	protected AlertManager alertManager;

	protected TopMetric buildTopMetric(Date date) {
		TopReport topReport = queryTopReport(date);
		TopMetric topMetric = new TopMetric(ALERT_PERIOD, Integer.MAX_VALUE, exceptionRuleConfigManager);

		topMetric.setStart(date).setEnd(new Date(date.getTime() + TimeHelper.ONE_MINUTE - 1));
		topMetric.visitTopReport(topReport);
		return topMetric;
	}

	public String getName() {
		return AlertType.Exception.getName();
	}

	private void handleExceptions(List<Item> itemList) {
		Map<String, List<AlertException>> alertExceptions = alertExceptionBuilder.buildAlertExceptions(itemList);

		//告警开关
		if (alertExceptions.isEmpty()) {
			return;
		}

		for (Entry<String, List<AlertException>> entry : alertExceptions.entrySet()) {
			try {
				String domain = entry.getKey();
				List<AlertException> exceptions = entry.getValue();

				for (AlertException exception : exceptions) {
					String metricName = exception.getName();
					AlertEntity entity = new AlertEntity();

					entity.setDate(new Date()).setContent(exception.toString()).setLevel(exception.getType());
					entity.setMetric(metricName).setType(getName()).setGroup(domain);
					alertManager.addAlert(entity);
				}
				LOGGER.info("Exception alerts queued, domain={}, alertCount={}.", domain, exceptions.size());
			} catch (Exception e) {
				LOGGER.error("Unable to handle exception alerts, domain={}.", entry.getKey(), e);
				Cat.logError(e);
			}
		}
	}

	protected TopReport queryTopReport(Date start) {
		String domain = Constants.CAT;
		String date = String.valueOf(start.getTime());
		ModelRequest request = new ModelRequest(domain, start.getTime()).setProperty("date", date);

		if (topModelService.isEligable(request)) {
			ModelResponse<TopReport> response = topModelService.invoke(request);
			TopReport report = response.getModel();

			report.accept(new TopExceptionExclude(exceptionRuleConfigManager));
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("AlertException", TimeHelper.getMinuteStr());

			try {
				TopMetric topMetric = buildTopMetric(new Date(current - TimeHelper.ONE_MINUTE - current%TimeHelper.ONE_MINUTE));
				Collection<List<Item>> itemLists = topMetric.getError().getResult().values();
				List<Item> itemList = new ArrayList<Item>();

				if (!itemLists.isEmpty()) {
					itemList = itemLists.iterator().next();
				}
				List<Item> items = new ArrayList<Item>();

				for (Item item : itemList) {
					if (!Constants.FRONT_END.equals(item.getDomain())) {
						items.add(item);
					}
				}
				LOGGER.info("Exception alert cycle started, itemCount={}, filteredItemCount={}.", itemList.size(),
						items.size());
				handleExceptions(items);

				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				LOGGER.error("Exception alert cycle failed.", e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				LOGGER.warn("Exception alert task interrupted.");
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}

	public void setAlertBuilder(AlertExceptionBuilder alertBuilder) {
		alertExceptionBuilder = alertBuilder;
	}

	public void setExceptionConfigManager(ExceptionRuleConfigManager exceptionConfigManager) {
		exceptionRuleConfigManager = exceptionConfigManager;
	}

	public void setSendManager(AlertManager sendManager) {
		alertManager = sendManager;
	}

	public void setTopService(ModelService<TopReport> topService) {
		topModelService = topService;
	}
}
