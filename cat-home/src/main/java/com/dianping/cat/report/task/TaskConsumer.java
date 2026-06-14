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

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.support.Threads;

public abstract class TaskConsumer implements Threads.Task {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskConsumer.class);

	public static final int STATUS_TODO = 1;

	public static final int STATUS_DOING = 2;

	public static final int STATUS_DONE = 3;

	public static final int STATUS_FAIL = 4;

	private static final int MAX_TODO_RETRY_TIMES = 1;

	private long m_nanos = 2L * 1000 * 1000 * 1000;

	private volatile boolean m_running = true;

	private volatile boolean m_stopped = false;

	public boolean checkTime() {
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);

		if (minute >= 10) {
			return true;
		} else {
			return false;
		}
	}

	protected abstract Task findDoingTask(String consumerIp);

	protected abstract Task findTodoTask();

	protected String getLoaclIp() {
		return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	protected long getSleepTime() {
		return m_nanos;
	}

	public boolean isStopped() {
		return m_stopped;
	}

	protected abstract boolean processTask(Task doing);

	@Override
	public void run() {
		String localIp = getLoaclIp();
		LOGGER.info("Task consumer started, name={}, localIp={}.", getName(), localIp);
		while (m_running) {
			try {
				if (checkTime()) {
					Task task = findDoingTask(localIp);
					if (task == null) {
						task = findTodoTask();
					}
					boolean again = false;
					if (task != null) {
						try {
							task.setConsumer(localIp);
							if (task.getStatus() == TaskConsumer.STATUS_DOING || updateTodoToDoing(task)) {
								LOGGER.info("Task claimed for processing, name={}, reportName={}, domain={}, type={}, period={}, taskId={}.",
										getName(), task.getReportName(), task.getReportDomain(), task.getTaskType(),
										task.getReportPeriod(), task.getId());
								int retryTimes = 0;
								while (!processTask(task)) {
									retryTimes++;
									if (retryTimes < MAX_TODO_RETRY_TIMES) {
										LOGGER.warn("Task processing failed, retrying, name={}, reportName={}, domain={}, type={}, period={}, taskId={}, retryTimes={}.",
												getName(), task.getReportName(), task.getReportDomain(), task.getTaskType(),
												task.getReportPeriod(), task.getId(), retryTimes);
										taskRetryDuration();
									} else {
										LOGGER.error("Task processing failed after retries, name={}, reportName={}, domain={}, type={}, period={}, taskId={}, retryTimes={}.",
												getName(), task.getReportName(), task.getReportDomain(), task.getTaskType(),
												task.getReportPeriod(), task.getId(), retryTimes);
										updateDoingToFailure(task);
										again = true;
										break;
									}
								}
								if (!again) {
									LOGGER.info("Task processing succeeded, name={}, reportName={}, domain={}, type={}, period={}, taskId={}.",
											getName(), task.getReportName(), task.getReportDomain(), task.getTaskType(),
											task.getReportPeriod(), task.getId());
									updateDoingToDone(task);
								}
							}
						} catch (Throwable e) {
							LOGGER.error("Unexpected error while processing task, task={}.", task, e);
							Cat.logError(task.toString(), e);
						}
					} else {
						taskNotFoundDuration();
					}
				} else {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException e) {
						LOGGER.warn("Task consumer sleep interrupted before active window, name={}.", getName(), e);
					}
				}
			} catch (Throwable e) {
				LOGGER.error("Task consumer loop failed, name={}.", getName(), e);
				Cat.logError(e);
			}
		}
		m_stopped = true;
		LOGGER.info("Task consumer stopped, name={}, localIp={}.", getName(), localIp);
	}

	public void stop() {
		m_running = false;
	}

	protected abstract void taskNotFoundDuration();

	protected abstract void taskRetryDuration();

	protected abstract boolean updateDoingToDone(Task doing);

	protected abstract boolean updateDoingToFailure(Task todo);

	protected abstract boolean updateTodoToDoing(Task todo);
}
