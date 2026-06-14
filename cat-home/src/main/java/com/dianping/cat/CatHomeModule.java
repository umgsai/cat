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
package com.dianping.cat;

import com.dianping.cat.support.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.hadoop.CatHadoopModule;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.reload.ReportReloadTask;
import com.dianping.cat.spring.CatSpringContext;

public class CatHomeModule extends AbstractModule {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeModule.class);

	public static final String ID = "cat-home";

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		ServerConfigManager serverConfigManager = CatSpringContext.getBeanIfAvailable(ServerConfigManager.class);

		if (serverConfigManager == null) {
			serverConfigManager = ctx.lookup(ServerConfigManager.class);
			LOGGER.info("Resolved ServerConfigManager from Plexus for CatHomeModule.");
		} else {
			LOGGER.info("Resolved ServerConfigManager from Spring for CatHomeModule.");
		}
		ReportReloadTask reportReloadTask = CatSpringContext.getBeanIfAvailable(ReportReloadTask.class);

		if (reportReloadTask == null) {
			throw new IllegalStateException("ReportReloadTask must be configured by Spring for CatHomeModule.");
		}
		LOGGER.info("Resolved ReportReloadTask from Spring for CatHomeModule.");

		Threads.forGroup("Cat").start(reportReloadTask);

		lookup(ctx, MessageConsumer.class);

		if (serverConfigManager.isJobMachine()) {
			DefaultTaskConsumer taskConsumer = CatSpringContext.getBeanIfAvailable(DefaultTaskConsumer.class);

			if (taskConsumer == null) {
				throw new IllegalStateException("DefaultTaskConsumer must be configured by Spring for CatHomeModule.");
			}
			LOGGER.info("Resolved DefaultTaskConsumer from Spring for CatHomeModule.");
			Threads.forGroup("Cat").start(taskConsumer);
		}

		AlarmManager alarmManager = CatSpringContext.getBeanIfAvailable(AlarmManager.class);

		if (alarmManager == null) {
			alarmManager = ctx.lookup(AlarmManager.class);
			LOGGER.info("Resolved AlarmManager from Plexus for CatHomeModule.");
		} else {
			LOGGER.info("Resolved AlarmManager from Spring for CatHomeModule.");
		}

		if (serverConfigManager.isAlertMachine()) {
			alarmManager.startAlarm();
		}

		final MessageConsumer consumer = lookup(ctx, MessageConsumer.class);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				consumer.doCheckpoint();
			}
		});
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatConsumerModule.ID, CatHadoopModule.ID);
	}

	@Override
	protected void setup(ModuleContext ctx) throws Exception {
		final TcpSocketReceiver messageReceiver = lookup(ctx, TcpSocketReceiver.class);

		messageReceiver.init();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				messageReceiver.destory();
			}
		});
	}

	private <T> T lookup(ModuleContext ctx, Class<T> type) {
		T bean = CatSpringContext.getBeanIfAvailable(type);

		if (bean != null) {
			LOGGER.info("Resolved {} from Spring for CatHomeModule.", type.getSimpleName());
			return bean;
		}

		T component = ctx.lookup(type);

		LOGGER.info("Resolved {} from Plexus for CatHomeModule.", type.getSimpleName());
		return component;
	}
}
