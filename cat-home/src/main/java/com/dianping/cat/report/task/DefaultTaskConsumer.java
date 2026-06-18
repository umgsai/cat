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
/**
	*
	*/
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.mybatis.TaskRepository;
import com.dianping.cat.message.Transaction;

public class DefaultTaskConsumer extends TaskConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskConsumer.class);

	private ReportFacade m_reportFacade;

	private TaskRepository m_taskDao;

	@Override
	protected Task findDoingTask(String ip) {
		Task task = null;
		try {
			task = m_taskDao.findByStatusConsumer(STATUS_DOING, ip);
		} catch (RuntimeException e) {
			LOGGER.error("Unable to find doing task, consumerIp={}.", ip, e);
		}
		return task;
	}

	@Override
	protected Task findTodoTask() {
		Task task = null;
		try {
			task = m_taskDao.findByStatusConsumer(STATUS_TODO, null);
		} catch (RuntimeException e) {
			LOGGER.error("Unable to find todo task.", e);
		}
		return task;
	}

	@Override
	public String getName() {
		return "Task-Consumer";
	}

	@Override
	protected boolean processTask(Task doing) {
		boolean result = false;
		Transaction t = Cat.newTransaction("Task", doing.getReportName());

		t.addData(doing.toString());
		try {
			LOGGER.info("Processing report task, reportName={}, domain={}, type={}, period={}, taskId={}.",
					doing.getReportName(), doing.getReportDomain(), doing.getTaskType(), doing.getReportPeriod(), doing.getId());
			result = m_reportFacade.builderReport(doing);
			t.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			LOGGER.error("Unable to process report task, reportName={}, domain={}, type={}, period={}, taskId={}.",
					doing.getReportName(), doing.getReportDomain(), doing.getTaskType(), doing.getReportPeriod(), doing.getId(),
					e);
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
		return result;
	}

	@Override
	public void shutdown() {

	}

	@Override
	protected void taskNotFoundDuration() {
		try {
			Thread.sleep(2 * 60 * 1000);
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	@Override
	protected void taskRetryDuration() {
		LockSupport.parkNanos(2L * 1000 * 1000 * 1000);// sleep 10 sec
	}

	@Override
	protected boolean updateDoingToDone(Task doing) {
		doing.setStatus(STATUS_DONE);
		doing.setEndDate(new Date());

		try {
			return m_taskDao.updateDoingToDone(doing) == 1;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to mark task done, reportName={}, domain={}, type={}, period={}, taskId={}.",
					doing.getReportName(), doing.getReportDomain(), doing.getTaskType(), doing.getReportPeriod(), doing.getId(), e);
			Cat.logError(e);
		}
		return true;
	}

	@Override
	protected boolean updateDoingToFailure(Task doing) {
		doing.setStatus(STATUS_FAIL);
		doing.setEndDate(new Date());

		try {
			return m_taskDao.updateDoingToFail(doing) == 1;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to mark task failed, reportName={}, domain={}, type={}, period={}, taskId={}.",
					doing.getReportName(), doing.getReportDomain(), doing.getTaskType(), doing.getReportPeriod(), doing.getId(), e);
			Cat.logError(e);
			return false;
		}
	}

	@Override
	protected boolean updateTodoToDoing(Task todo) {
		todo.setStatus(STATUS_DOING);
		todo.setConsumer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		todo.setStartDate(new Date());

		try {
			return m_taskDao.updateTodoToDoing(todo) == 1;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to claim todo task, reportName={}, domain={}, type={}, period={}, taskId={}, consumer={}.",
					todo.getReportName(), todo.getReportDomain(), todo.getTaskType(), todo.getReportPeriod(), todo.getId(),
					todo.getConsumer(), e);
			Cat.logError(e);
			return false;
		}
	}

	public void setReportFacade(ReportFacade reportFacade) {
		m_reportFacade = reportFacade;
	}

	public void setTaskDao(TaskRepository taskDao) {
		m_taskDao = taskDao;
	}
}
