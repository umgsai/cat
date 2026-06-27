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
package com.dianping.cat.alarm.spi;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.decorator.DecoratorManager;
import com.dianping.cat.alarm.spi.receiver.ContactorManager;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.alarm.spi.spliter.SpliterManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.dianping.cat.support.Threads;
import com.dianping.cat.support.Threads.Task;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component("spiAlertManager")
public class AlertManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertManager.class);

	private static final int MILLIS1MINUTE = 60 * 1000;

	private volatile boolean initialized;

	@Resource
	protected SpliterManager spliterManager;

	@Resource
	protected SenderManager senderManager;

	@Resource
	protected AlertService alertService;

	@Resource
	private AlertPolicyManager alertPolicyManager;

	@Resource
	private DecoratorManager decoratorManager;

	@Resource
	private ContactorManager contactorManager;

	@Resource
	private ServerConfigManager serverConfigManager;

	private BlockingQueue<AlertEntity> alerts = new LinkedBlockingDeque<AlertEntity>(10000);

	private Map<String, AlertEntity> unrecoveredAlerts = new ConcurrentHashMap<String, AlertEntity>(1000);

	private Map<String, AlertEntity> sentAlerts = new ConcurrentHashMap<String, AlertEntity>(1000);

	private ConcurrentHashMap<AlertEntity, Long> alertMap = new ConcurrentHashMap<AlertEntity, Long>();

	public boolean addAlert(AlertEntity entity) {
		ensureInitialized();

		alertMap.put(entity, entity.getDate().getTime());

		String group = entity.getGroup();
		Cat.logEvent("Alert:" + entity.getType().getName(), group, Event.SUCCESS, entity.toString());

		if (serverConfigManager.isAlertMachine()) {
			boolean offered = alerts.offer(entity);

			if (!offered) {
				LOGGER.warn("Alert queue is full, alert is dropped, type={}, group={}, metric={}, key={}.",
				      entity.getType().getName(), group, entity.getMetric(), entity.getKey());
			}
			return offered;
		} else {
			LOGGER.info("Current machine is not configured as alert machine, skip queueing alert, type={}, group={}, key={}.",
			      entity.getType().getName(), group, entity.getKey());
			return true;
		}
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	@PostConstruct
	public synchronized void initialize() {
		if (initialized) {
			return;
		}

		LOGGER.info("Initializing alert manager executors.");
		Threads.forGroup("Cat").start(new SendExecutor());
		Threads.forGroup("Cat").start(new RecoveryAnnouncer());
		initialized = true;
	}

	public boolean isSuspend(String alertKey, int suspendMinute) {
		AlertEntity sentAlert = sentAlerts.get(alertKey);

		if (sentAlert != null) {
			long duration = System.currentTimeMillis() - sentAlert.getDate().getTime();

			if (duration / MILLIS1MINUTE < suspendMinute) {
				LOGGER.info("Alert is suspended, key={}, suspendMinute={}, elapsedMinute={}.", alertKey, suspendMinute,
				      duration / MILLIS1MINUTE);
				Cat.logEvent("SuspendAlert", alertKey, Event.SUCCESS, null);
				return true;
			}
		}
		return false;
	}

	public void setAlertService(AlertService alertService) {
		this.alertService = alertService;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		serverConfigManager = configManager;
	}

	public void setContactorManager(ContactorManager contactorManager) {
		this.contactorManager = contactorManager;
	}

	public void setDecoratorManager(DecoratorManager decoratorManager) {
		this.decoratorManager = decoratorManager;
	}

	public void setPolicyManager(AlertPolicyManager policyManager) {
		alertPolicyManager = policyManager;
	}

	public void setSenderManager(SenderManager senderManager) {
		this.senderManager = senderManager;
	}

	public void setSplitterManager(SpliterManager splitterManager) {
		spliterManager = splitterManager;
	}

	public List<AlertEntity> queryLastestAlarmKey(int minute) {
		List<AlertEntity> keys = new ArrayList<AlertEntity>();
		long currentTimeMillis = System.currentTimeMillis();

		for (Entry<AlertEntity, Long> entry : alertMap.entrySet()) {
			Long value = entry.getValue();

			if (currentTimeMillis - value < TimeHelper.ONE_MINUTE * minute) {
				keys.add(entry.getKey());
			}
		}

		return keys;
	}

	//List去重
	private void removeDuplicate(List<String> list) {
		LinkedHashSet<String> set = new LinkedHashSet<String>(list.size());
		set.addAll(list);
		list.clear();
		list.addAll(set);
	}

	private boolean send(AlertEntity alert) {
		boolean result = false;
		String type = alert.getType().getName();
		String group = alert.getGroup();
		String level = alert.getLevel().getLevel();
		String alertKey = alert.getKey();
		List<AlertChannel> channels = alertPolicyManager.queryChannels(type, group, level);
		int suspendMinute = alertPolicyManager.querySuspendMinute(type, group, level);

		unrecoveredAlerts.put(alertKey, alert);

		Pair<String, String> pair = decoratorManager.generateTitleAndContent(alert);
		String title = pair.getKey();

		if (suspendMinute > 0) {
			if (isSuspend(alertKey, suspendMinute)) {
				return true;
			} else {
				sentAlerts.put(alertKey, alert);
			}
		}

		SendMessageEntity message = null;

		for (AlertChannel channel : channels) {
			String contactGroup = alert.getContactGroup();
			List<String> receivers = contactorManager.queryReceivers(contactGroup, channel, type);
			//去重
			removeDuplicate(receivers);

			if (receivers.size() > 0) {
				String rawContent = pair.getValue();

				if (suspendMinute > 0) {
					rawContent = rawContent + "<br/>[告警间隔时间]" + suspendMinute + "分钟";
				}
				String content = spliterManager.process(rawContent, channel);
				message = new SendMessageEntity(group, title, type, content, receivers);

				if (senderManager.sendAlert(channel, message)) {
					result = true;
					LOGGER.info("Sent alert message, type={}, group={}, level={}, channel={}, receivers={}.", type,
					      group, level, channel, receivers.size());
				} else {
					LOGGER.warn("Alert sender returned false, type={}, group={}, level={}, channel={}, receivers={}.",
					      type, group, level, channel, receivers.size());
				}
			} else {
				LOGGER.warn("No alert receiver found, type={}, group={}, contactGroup={}, channel={}.", type, group,
				      contactGroup, channel);
				Cat.logEvent("NoneReceiver:" + channel, type + ":" + contactGroup, Event.SUCCESS, null);
			}
		}

		String dbContent = Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(pair.getValue()).replaceAll("");

		if (message == null) {
			message = new SendMessageEntity(group, title, type, "", null);
		}
		message.setContent(dbContent);
		alertService.insert(alert, message);
		return result;
	}

	private boolean sendRecoveryMessage(AlertEntity alert, String currentMinute) {
		AlertType alterType = alert.getType();
		String type = alterType.getName();
		String group = alert.getGroup();
		String level = alert.getLevel().getLevel();
		List<AlertChannel> channels = alertPolicyManager.queryChannels(type, group, level);

		for (AlertChannel channel : channels) {
			String title = "[告警恢复] [告警类型 " + alterType.getTitle() + "][" + group + " " + alert.getMetric() + "]";
			String content = "[告警已恢复][恢复时间]" + currentMinute;
			List<String> receivers = contactorManager.queryReceivers(alert.getContactGroup(), channel, type);
			//去重
			removeDuplicate(receivers);

			if (receivers.size() > 0) {
				SendMessageEntity message = new SendMessageEntity(group, title, type, content, receivers);

				if (senderManager.sendAlert(channel, message)) {
					LOGGER.info("Sent alert recovery message, type={}, group={}, level={}, channel={}, receivers={}.",
					      type, group, level, channel, receivers.size());
					return true;
				} else {
					LOGGER.warn("Alert recovery sender returned false, type={}, group={}, level={}, channel={}, receivers={}.",
					      type, group, level, channel, receivers.size());
				}
			} else {
				LOGGER.warn("No alert recovery receiver found, type={}, group={}, contactGroup={}, channel={}.", type,
				      group, alert.getContactGroup(), channel);
			}
		}

		return false;
	}

	private class RecoveryAnnouncer implements Task {

		private DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		private int queryRecoverMinute(AlertEntity alert) {
			String type = alert.getType().getName();
			String group = alert.getGroup();
			String level = alert.getLevel().getLevel();

			return alertPolicyManager.queryRecoverMinute(type, group, level);
		}

		@Override
		public void run() {
			while (true) {
				long current = System.currentTimeMillis();
				String currentStr = sdf.format(new Date(current));
				List<String> recoveredItems = new ArrayList<String>();

				for (Entry<String, AlertEntity> entry : unrecoveredAlerts.entrySet()) {
					try {
						String key = entry.getKey();
						AlertEntity alert = entry.getValue();
						int recoverMinute = queryRecoverMinute(alert);
						long alertTime = alert.getDate().getTime();
						int alreadyMinutes = (int) ((current - alertTime) / MILLIS1MINUTE);

						if (alreadyMinutes >= recoverMinute) {
							recoveredItems.add(key);
							sendRecoveryMessage(alert, currentStr);
						}
					} catch (Exception e) {
						LOGGER.error("Unable to announce alert recovery, key={}.", entry.getKey(), e);
						Cat.logError(e);
					}
				}

				for (String key : recoveredItems) {
					unrecoveredAlerts.remove(key);
				}

				long duration = System.currentTimeMillis() - current;
				if (duration < MILLIS1MINUTE) {
					long lackMills = MILLIS1MINUTE - duration;

					try {
						TimeUnit.MILLISECONDS.sleep(lackMills);
					} catch (InterruptedException e) {
						LOGGER.warn("Alert recovery announcer interrupted.");
						Thread.currentThread().interrupt();
						Cat.logError(e);
						break;
					}
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

	private class SendExecutor implements Task {
		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (true) {
				try {
					AlertEntity alert = alerts.poll(5, TimeUnit.MILLISECONDS);

					if (alert != null) {
						send(alert);
					}
				} catch (Exception e) {
					LOGGER.error("Unable to process alert from queue.", e);
					Cat.logError(e);
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
