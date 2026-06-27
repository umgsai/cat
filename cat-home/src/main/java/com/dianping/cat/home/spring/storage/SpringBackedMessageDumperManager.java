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
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperManager;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.statistic.ServerStatisticManager;

@Primary
@Component("messageDumperManager")
public class SpringBackedMessageDumperManager implements MessageDumperManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBackedMessageDumperManager.class);

	private final Map<Integer, MessageDumper> dumpers = new LinkedHashMap<Integer, MessageDumper>();

	@Resource(name = "blockDumperManager")
	private BlockDumperManager blockDumperManager;

	@Resource(name = "local")
	private BucketManager bucketManager;

	@Resource(name = "messageFinderManager")
	private MessageFinderManager finderManager;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager configManager;

	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager statisticManager;

	@Override
	public synchronized void close(int hour) {
		MessageDumper dumper = dumpers.remove(hour);

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
		return dumpers.get(hour);
	}

	@Override
	public MessageDumper findOrCreate(int hour) {
		MessageDumper dumper = dumpers.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = dumpers.get(hour);

				if (dumper == null) {
					dumper = newDumper();
					dumper.initialize(hour);
					dumpers.put(hour, dumper);

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					LOGGER.info("Created message dumper {}.", sdf.format(new Date(TimeUnit.HOURS.toMillis(hour))));
				}
			}
		}
		return dumper;
	}

	private MessageDumper newDumper() {
		SpringBackedMessageDumper dumper = new SpringBackedMessageDumper();

		dumper.setBlockDumperManager(blockDumperManager);
		dumper.setBucketManager(bucketManager);
		dumper.setConfigManager(configManager);
		dumper.setFinderManager(finderManager);
		dumper.setStatisticManager(statisticManager);
		return dumper;
	}

	public void setBlockDumperManager(BlockDumperManager blockDumperManager) {
		this.blockDumperManager = blockDumperManager;
	}

	public void setBucketManager(BucketManager bucketManager) {
		this.bucketManager = bucketManager;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		this.configManager = configManager;
	}

	public void setFinderManager(MessageFinderManager finderManager) {
		this.finderManager = finderManager;
	}

	public void setStatisticManager(ServerStatisticManager statisticManager) {
		this.statisticManager = statisticManager;
	}
}
