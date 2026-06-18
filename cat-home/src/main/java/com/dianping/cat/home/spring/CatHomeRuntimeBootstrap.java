package com.dianping.cat.home.spring;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.cat.message.storage.clean.LogviewProcessor;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.server.ServersUpdaterManager;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.reload.ReportReloadTask;
import com.dianping.cat.support.Threads;

public class CatHomeRuntimeBootstrap {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeRuntimeBootstrap.class);

	private final AtomicBoolean m_started = new AtomicBoolean();

	private AlarmManager m_alarmManager;

	private DefaultTaskConsumer m_taskConsumer;

	private LogviewProcessor m_logviewProcessor;

	private MessageConsumer m_messageConsumer;

	private ReportReloadTask m_reportReloadTask;

	private ServerConfigManager m_serverConfigManager;

	private ServersUpdaterManager m_serversUpdaterManager;

	private TcpSocketReceiver m_tcpSocketReceiver;

	public void shutdown() {
		if (!m_started.get()) {
			return;
		}
		try {
			m_messageConsumer.doCheckpoint();
		} catch (RuntimeException e) {
			LOGGER.warn("Unable to checkpoint message consumer during shutdown.", e);
		}
		m_tcpSocketReceiver.destory();
		LOGGER.info("CAT home runtime bootstrap stopped.");
	}

	public void start() {
		if (!m_started.compareAndSet(false, true)) {
			return;
		}

		if (m_serversUpdaterManager == null) {
			throw new IllegalStateException("ServersUpdaterManager is required for CAT home runtime bootstrap.");
		}
		LOGGER.info("Resolved ServersUpdaterManager for CAT home runtime bootstrap.");

		m_tcpSocketReceiver.init();
		Threads.forGroup("Cat").start(m_logviewProcessor);
		Threads.forGroup("Cat").start(m_reportReloadTask);
		LOGGER.info("isJobMachine: {}", m_serverConfigManager.isJobMachine());
		if (m_serverConfigManager.isJobMachine()) {
			Threads.forGroup("Cat").start(m_taskConsumer);
		}
		LOGGER.info("isAlertMachine: {}", m_serverConfigManager.isAlertMachine());
		if (m_serverConfigManager.isAlertMachine()) {
			m_alarmManager.startAlarm();
		}
		LOGGER.info("CAT home runtime bootstrap started.");
	}

	public void setAlarmManager(AlarmManager alarmManager) {
		m_alarmManager = alarmManager;
	}

	public void setLogviewProcessor(LogviewProcessor logviewProcessor) {
		m_logviewProcessor = logviewProcessor;
	}

	public void setMessageConsumer(MessageConsumer messageConsumer) {
		m_messageConsumer = messageConsumer;
	}

	public void setReportReloadTask(ReportReloadTask reportReloadTask) {
		m_reportReloadTask = reportReloadTask;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		m_serverConfigManager = serverConfigManager;
	}

	public void setServersUpdaterManager(ServersUpdaterManager serversUpdaterManager) {
		m_serversUpdaterManager = serversUpdaterManager;
	}

	public void setTaskConsumer(DefaultTaskConsumer taskConsumer) {
		m_taskConsumer = taskConsumer;
	}

	public void setTcpSocketReceiver(TcpSocketReceiver tcpSocketReceiver) {
		m_tcpSocketReceiver = tcpSocketReceiver;
	}
}
