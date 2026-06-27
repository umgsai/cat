package com.dianping.cat.home.spring;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.clean.LogviewProcessor;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.server.ServersUpdaterManager;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.reload.ReportReloadTask;
import com.dianping.cat.support.Threads;

@Component("catHomeRuntimeBootstrap")
public class CatHomeRuntimeBootstrap {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeRuntimeBootstrap.class);

	private final AtomicBoolean started = new AtomicBoolean();

	private final AtomicBoolean stopped = new AtomicBoolean();

	private Thread shutdownHook;

	@Resource(name = "alarmManager")
	private AlarmManager alarmManager;

	@Resource(name = "defaultTaskConsumer")
	private DefaultTaskConsumer taskConsumer;

	@Resource(name = "logviewProcessor")
	private LogviewProcessor logviewProcessor;

	@Resource(name = "messageConsumer")
	private MessageConsumer messageConsumer;

	@Resource(name = "reportReloadTask")
	private ReportReloadTask reportReloadTask;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "serversUpdaterManager")
	private ServersUpdaterManager serversUpdaterManager;

	@Resource(name = "tcpSocketReceiver")
	private TcpSocketReceiver tcpSocketReceiver;

	@PreDestroy
	public void shutdown() {
		if (!started.get() || !stopped.compareAndSet(false, true)) {
			return;
		}

		try {
			messageConsumer.doCheckpoint();
		} catch (RuntimeException e) {
			LOGGER.warn("Unable to checkpoint message consumer during shutdown.", e);
		}
		tcpSocketReceiver.destory();
		removeShutdownHook();
		LOGGER.info("CAT home runtime bootstrap stopped.");
	}

	@PostConstruct
	public void start() {
		if (!started.compareAndSet(false, true)) {
			return;
		}

		if (serversUpdaterManager == null) {
			throw new IllegalStateException("ServersUpdaterManager is required for CAT home runtime bootstrap.");
		}
		LOGGER.info("Resolved ServersUpdaterManager for CAT home runtime bootstrap.");

		registerShutdownHook();
		tcpSocketReceiver.init();
		Threads.forGroup("Cat").start(logviewProcessor);
		Threads.forGroup("Cat").start(reportReloadTask);
		LOGGER.info("isJobMachine: {}", serverConfigManager.isJobMachine());
		if (serverConfigManager.isJobMachine()) {
			Threads.forGroup("Cat").start(taskConsumer);
		}
		LOGGER.info("isAlertMachine: {}", serverConfigManager.isAlertMachine());
		if (serverConfigManager.isAlertMachine()) {
			alarmManager.startAlarm();
		}
		LOGGER.info("CAT home runtime bootstrap started.");
	}

	private void registerShutdownHook() {
		shutdownHook = new Thread(this::shutdown);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	private void removeShutdownHook() {
		Thread hook = shutdownHook;

		if (hook == null || hook == Thread.currentThread()) {
			return;
		}

		try {
			Runtime.getRuntime().removeShutdownHook(hook);
		} catch (IllegalStateException e) {
			// JVM is already shutting down, so the hook no longer needs removal.
		}
	}

	public void setAlarmManager(AlarmManager alarmManager) {
		this.alarmManager = alarmManager;
	}

	public void setLogviewProcessor(LogviewProcessor logviewProcessor) {
		this.logviewProcessor = logviewProcessor;
	}

	public void setMessageConsumer(MessageConsumer messageConsumer) {
		this.messageConsumer = messageConsumer;
	}

	public void setReportReloadTask(ReportReloadTask reportReloadTask) {
		this.reportReloadTask = reportReloadTask;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		this.serverConfigManager = serverConfigManager;
	}

	public void setServersUpdaterManager(ServersUpdaterManager serversUpdaterManager) {
		this.serversUpdaterManager = serversUpdaterManager;
	}

	public void setTaskConsumer(DefaultTaskConsumer taskConsumer) {
		this.taskConsumer = taskConsumer;
	}

	public void setTcpSocketReceiver(TcpSocketReceiver tcpSocketReceiver) {
		this.tcpSocketReceiver = tcpSocketReceiver;
	}
}
