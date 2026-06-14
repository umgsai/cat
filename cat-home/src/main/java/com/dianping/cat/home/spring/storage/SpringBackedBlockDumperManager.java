package com.dianping.cat.home.spring.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.statistic.ServerStatisticManager;

public class SpringBackedBlockDumperManager implements BlockDumperManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBackedBlockDumperManager.class);

	private final Map<Integer, BlockDumper> m_dumpers = new LinkedHashMap<Integer, BlockDumper>();

	private BucketManager m_bucketManager;

	private ServerConfigManager m_configManager;

	private ServerStatisticManager m_statisticManager;

	@Override
	public synchronized void close(int hour) {
		BlockDumper dumper = m_dumpers.remove(hour);

		if (dumper != null) {
			try {
				dumper.awaitTermination();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public BlockDumper findOrCreate(int hour) {
		BlockDumper dumper = m_dumpers.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_dumpers.get(hour);

				if (dumper == null) {
					dumper = newDumper();
					dumper.initialize(hour);
					m_dumpers.put(hour, dumper);

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					LOGGER.info("Created block dumper {}.", sdf.format(new Date(TimeUnit.HOURS.toMillis(hour))));
				}
			}
		}
		return dumper;
	}

	private BlockDumper newDumper() {
		SpringBackedBlockDumper dumper = new SpringBackedBlockDumper();

		dumper.setBucketManager(m_bucketManager);
		dumper.setConfigManager(m_configManager);
		dumper.setStatisticManager(m_statisticManager);
		return dumper;
	}

	public void setBucketManager(BucketManager bucketManager) {
		m_bucketManager = bucketManager;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		m_configManager = configManager;
	}

	public void setStatisticManager(ServerStatisticManager statisticManager) {
		m_statisticManager = statisticManager;
	}
}
