package com.dianping.cat.home.spring.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.statistic.ServerStatisticManager;

@Primary
@Component("blockDumperManager")
public class SpringBackedBlockDumperManager implements BlockDumperManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBackedBlockDumperManager.class);

	private final Map<Integer, BlockDumper> dumpers = new LinkedHashMap<Integer, BlockDumper>();

	@Resource(name = "local")
	private BucketManager bucketManager;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager configManager;

	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager statisticManager;

	@Override
	public synchronized void close(int hour) {
		BlockDumper dumper = dumpers.remove(hour);

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
		BlockDumper dumper = dumpers.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = dumpers.get(hour);

				if (dumper == null) {
					dumper = newDumper();
					dumper.initialize(hour);
					dumpers.put(hour, dumper);

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					LOGGER.info("Created block dumper {}.", sdf.format(new Date(TimeUnit.HOURS.toMillis(hour))));
				}
			}
		}
		return dumper;
	}

	private BlockDumper newDumper() {
		SpringBackedBlockDumper dumper = new SpringBackedBlockDumper();

		dumper.setBucketManager(bucketManager);
		dumper.setConfigManager(configManager);
		dumper.setStatisticManager(statisticManager);
		return dumper;
	}

	public void setBucketManager(BucketManager bucketManager) {
		this.bucketManager = bucketManager;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		this.configManager = configManager;
	}

	public void setStatisticManager(ServerStatisticManager statisticManager) {
		this.statisticManager = statisticManager;
	}
}
