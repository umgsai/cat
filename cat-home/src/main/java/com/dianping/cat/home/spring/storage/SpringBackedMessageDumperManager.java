package com.dianping.cat.home.spring.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperManager;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.statistic.ServerStatisticManager;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageFinderManager;

public class SpringBackedMessageDumperManager implements MessageDumperManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBackedMessageDumperManager.class);

	private final Map<Integer, MessageDumper> m_dumpers = new LinkedHashMap<Integer, MessageDumper>();

	private BlockDumperManager m_blockDumperManager;

	private BucketManager m_bucketManager;

	private MessageFinderManager m_finderManager;

	private ServerConfigManager m_configManager;

	private ServerStatisticManager m_statisticManager;

	@Override
	public synchronized void close(int hour) {
		MessageDumper dumper = m_dumpers.remove(hour);

		if (dumper != null) {
			try {
				dumper.awaitTermination(hour);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public MessageDumper find(int hour) {
		return m_dumpers.get(hour);
	}

	@Override
	public MessageDumper findOrCreate(int hour) {
		MessageDumper dumper = m_dumpers.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_dumpers.get(hour);

				if (dumper == null) {
					dumper = newDumper();
					dumper.initialize(hour);
					m_dumpers.put(hour, dumper);

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					LOGGER.info("Created message dumper {}.", sdf.format(new Date(TimeUnit.HOURS.toMillis(hour))));
				}
			}
		}
		return dumper;
	}

	private MessageDumper newDumper() {
		SpringBackedMessageDumper dumper = new SpringBackedMessageDumper();

		dumper.setBlockDumperManager(m_blockDumperManager);
		dumper.setBucketManager(m_bucketManager);
		dumper.setConfigManager(m_configManager);
		dumper.setFinderManager(m_finderManager);
		dumper.setStatisticManager(m_statisticManager);
		return dumper;
	}

	public void setBlockDumperManager(BlockDumperManager blockDumperManager) {
		m_blockDumperManager = blockDumperManager;
	}

	public void setBucketManager(BucketManager bucketManager) {
		m_bucketManager = bucketManager;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		m_configManager = configManager;
	}

	public void setFinderManager(MessageFinderManager finderManager) {
		m_finderManager = finderManager;
	}

	public void setStatisticManager(ServerStatisticManager statisticManager) {
		m_statisticManager = statisticManager;
	}
}
