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
package com.dianping.cat.report.page.alert;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;

import com.dianping.cat.Cat;
import com.dianping.cat.mybatis.alert.dao.data.AlertDO;
import com.dianping.cat.mybatis.repository.alert.AlertRepository;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.report.ReportPage;

@Component("alertHandler")
public class Handler implements PageHandler<Context> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

	@Resource
	private JspViewer jspViewer;

	@Resource
	private SenderManager senderManager;

	@Resource
	private AlertRepository alertRepository;

	private AlertDO buildAlertEntity(Payload payload) {
		AlertDO alertEntity = new AlertDO();

		alertEntity.setAlertTime(payload.getAlertTime());
		alertEntity.setCategory(payload.getCategory());
		alertEntity.setContent(payload.getContent());
		alertEntity.setDomain(payload.getDomain());
		alertEntity.setMetric(payload.getMetric());
		alertEntity.setType(payload.getLevel());
		return alertEntity;
	}

	private Map<String, AlertMinute> generateAlertMinutes(List<AlertDO> alerts) {
		DateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		Map<String, AlertMinute> alertMinutes = new LinkedHashMap<String, AlertMinute>();

		for (AlertDO alert : alerts) {
			String time = format.format(alert.getAlertTime());
			AlertMinute alertMinute = alertMinutes.get(time);

			if (alertMinute == null) {
				alertMinute = new AlertMinute(time);

				alertMinutes.put(time, alertMinute);
			}
			alertMinute.addAlert(alert);
		}

		return alertMinutes;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "alert")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "alert")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case ALERT:
			List<String> receivers = Splitter.on(',').omitEmptyStrings().splitToList(payload.getReceivers());
			if (receivers == null || receivers.size() == 0) {
				LOGGER.warn("Manual alert send request lacks receivers, channel={}, type={}, group={}.",
				      payload.getChannel(), payload.getType(), payload.getGroup());
				setAlertResult(model, 0);
			} else {
				SendMessageEntity message = new SendMessageEntity(payload.getGroup(), payload.getTitle(),	payload.getType(),
										payload.getContent(), receivers);

				try {
					boolean result = senderManager.sendAlert(AlertChannel.findByName(payload.getChannel()), message);
					if (result) {
						setAlertResult(model, 1);
					} else {
						LOGGER.warn("Manual alert send failed, channel={}, type={}, group={}, receiverCount={}.",
						      payload.getChannel(), payload.getType(), payload.getGroup(), receivers.size());
						setAlertResult(model, 2);
					}
				} catch (NullPointerException ex) {
					LOGGER.error("Manual alert send failed because channel is invalid, channel={}, type={}, group={}.",
					      payload.getChannel(), payload.getType(), payload.getGroup(), ex);
					setAlertResult(model, 3);
				}
			}
			break;
		case INSERT:
			if (StringUtils.isEmpty(payload.getDomain())) {
				setAlertResult(model, 4);
			} else {
				AlertDO alertEntity = buildAlertEntity(payload);

				try {
					int count = alertRepository.insert(alertEntity);

					if (count == 0) {
						LOGGER.warn("Manual alert insert returned zero, domain={}, category={}, metric={}.",
						      alertEntity.getDomain(), alertEntity.getCategory(), alertEntity.getMetric());
						setAlertResult(model, 5);
					} else {
						setAlertResult(model, 1);
					}
				} catch (RuntimeException e) {
					setAlertResult(model, 5);
					LOGGER.error("Unable to insert manual alert, domain={}, category={}, metric={}.",
					      alertEntity.getDomain(), alertEntity.getCategory(), alertEntity.getMetric(), e);
					Cat.logError(e);
				}
			}
			break;
		case VIEW:
			Date startTime = payload.getStartTime();
			Date endTime = payload.getEndTime();
			String domain = payload.getDomain();
			String alertTypeStr = payload.getAlertType();
			List<AlertDO> alerts;
			try {
				if (StringUtils.isEmpty(alertTypeStr)) {
					alerts = alertRepository.queryAlertsByTimeDomain(startTime, endTime, domain);
				} else {
					alerts = alertRepository.queryAlertsByTimeDomainCategories(startTime, endTime, domain,
					      payload.getAlertTypeArray());
				}
			} catch (RuntimeException e) {
				alerts = new ArrayList<AlertDO>();
				LOGGER.error("Unable to query alerts, startTime={}, endTime={}, domain={}, alertTypes={}.", startTime,
				      endTime, domain, alertTypeStr, e);
				Cat.logError(e);
			}
			model.setAlertMinutes(generateAlertMinutes(alerts));
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.ALERT);

		if (!ctx.isProcessStopped()) {
			jspViewer.view(ctx, model);
		}
	}

	private void setAlertResult(Model model, int status) {
		switch (status) {
		case 0:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"lack receivers\"}");
			break;
		case 1:
			model.setAlertResult("{\"status\":200}");
			break;
		case 2:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"send failed, please retry again\"}");
			break;
		case 3:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"send failed, please check your channel argument\"}");
			break;
		case 4:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"lack domain\"}");
			break;
		case 5:
			model.setAlertResult("{\"status\":500}");
			break;
		}
	}

	public class AlertDomain {

		private String m_name;

		private Map<String, List<AlertDO>> m_alertsByCategory = new HashMap<String, List<AlertDO>>();

		public AlertDomain(String name) {
			m_name = name;
		}

		public void addAlert(AlertDO alert) {
			String category = alert.getCategory();
			List<AlertDO> alerts = m_alertsByCategory.get(category);

			if (alerts == null) {
				alerts = new ArrayList<AlertDO>();

				m_alertsByCategory.put(category, alerts);
			}
			alerts.add(alert);
		}

		public Map<String, List<AlertDO>> getAlertCategories() {
			return m_alertsByCategory;
		}

		public int getCount() {
			int count = 0;

			for (List<AlertDO> alerts : m_alertsByCategory.values()) {
				count += alerts.size();
			}
			return count;
		}

		public String getName() {
			return m_name;
		}

	}

	public class AlertMinute {

		private String m_time;

		private Map<String, AlertDomain> m_domains = new HashMap<String, AlertDomain>();

		public AlertMinute(String time) {
			m_time = time;
		}

		public void addAlert(AlertDO alert) {
			String domain = alert.getDomain();
			AlertDomain alertDomain = m_domains.get(domain);

			if (alertDomain == null) {
				alertDomain = new AlertDomain(domain);

				m_domains.put(domain, alertDomain);
			}

			alertDomain.addAlert(alert);
		}

		public List<AlertDomain> getAlertDomains() {
			List<AlertDomain> alertDomains = new ArrayList<Handler.AlertDomain>(m_domains.values());

			Collections.sort(alertDomains, new Comparator<AlertDomain>() {
				@Override
				public int compare(AlertDomain domain1, AlertDomain domain2) {
					return domain2.getCount() - domain1.getCount();
				}
			});
			return alertDomains;
		}

		public String getTime() {
			return m_time;
		}

	}

}
