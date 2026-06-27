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
package com.dianping.cat.report.alert.summary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.alert.summary.build.SummaryBuilder;

@Component
public class AlertSummaryExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertSummaryExecutor.class);

	public static final long SUMMARY_DURATION = 5 * TimeHelper.ONE_MINUTE;

	public static final long ALTERATION_DURATION = 30 * TimeHelper.ONE_MINUTE;

	@Resource(name = "AlertSummaryContentGenerator")
	private SummaryBuilder relatedBuilder;

	@Resource(name = "FailureDecorator")
	private SummaryBuilder failureBuilder;

	@Resource(name = "AlterationSummaryContentGenerator")
	private SummaryBuilder alterationBuilder;

	@Resource
	private SenderManager senderManager;

	private List<String> builderReceivers(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitter.on(',').omitEmptyStrings().splitToList(str));
		}

		return result;
	}

	private String buildMailTitle(String domain, Date date) {
		StringBuilder builder = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		builder.append("[统一告警] [项目 ").append(domain).append("]");
		builder.append("[时间 ").append(dateFormat.format(date)).append("]");
		return builder.toString();
	}

	public String execute(String domain, Date date) {
		Transaction t = Cat.newTransaction("Summary", domain);

		date = normalizeDate(date);
		try {
			LOGGER.info("Generating alert summary, domain={}, date={}.", domain, date);
			StringBuilder builder = new StringBuilder();

			appendSummary(builder, relatedBuilder, domain, date);
			appendSummary(builder, failureBuilder, domain, date);
			appendSummary(builder, alterationBuilder, domain, date);

			t.setStatus(Transaction.SUCCESS);
			return builder.toString();
		} catch (Exception e) {
			t.setStatus(e);
			LOGGER.error("Unable to generate alert summary, domain={}, date={}.", domain, date, e);
			Cat.logError("generate alert summary fail:" + domain + " " + date, e);
		} finally {
			t.complete();
		}
		return null;
	}

	private void appendSummary(StringBuilder builder, SummaryBuilder summaryBuilder, String domain, Date date) {
		if (summaryBuilder == null) {
			LOGGER.warn("Alert summary builder is not configured, domain={}, date={}.", domain, date);
			return;
		}
		builder.append(summaryBuilder.generateHtml(domain, date));
	}

	public String execute(String domain, Date date, String receiverStr) {
		String content = execute(domain, date);

		if (content == null || "".equals(content)) {
			return null;
		} else {
			String title = buildMailTitle(domain, date);
			List<String> receivers = builderReceivers(receiverStr);
			SendMessageEntity message = new SendMessageEntity(domain, title, "alertSummary", content, receivers);

			if (receivers.size() > 0) {
				LOGGER.info("Sending alert summary mail, domain={}, date={}, receiverCount={}.", domain, date,
						receivers.size());

				if (senderManager == null) {
					LOGGER.warn("Alert summary sender manager is not configured, skip summary mail, domain={}, date={}.",
					      domain, date);
				} else {
					senderManager.sendAlert(AlertChannel.MAIL, message);
				}
			}
		}

		return content;
	}

	private Date normalizeDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public void setAlterationBuilder(SummaryBuilder alterationBuilder) {
		this.alterationBuilder = alterationBuilder;
	}

	public void setFailureBuilder(SummaryBuilder failureBuilder) {
		this.failureBuilder = failureBuilder;
	}

	public void setRelatedBuilder(SummaryBuilder relatedBuilder) {
		this.relatedBuilder = relatedBuilder;
	}

	public void setSendManager(SenderManager sendManager) {
		senderManager = sendManager;
	}

}
