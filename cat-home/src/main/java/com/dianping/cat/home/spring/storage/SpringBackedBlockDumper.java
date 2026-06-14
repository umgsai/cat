package com.dianping.cat.home.spring.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.cat.message.storage.BucketManager;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.support.Threads;

public class SpringBackedBlockDumper implements BlockDumper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBackedBlockDumper.class);

	private BucketManager m_bucketManager;

	private ServerConfigManager m_configManager;

	private ServerStatisticManager m_statisticManager;

	private final List<BlockingQueue<Block>> m_queues = new ArrayList<BlockingQueue<Block>>();

	private final List<BlockWriter> m_writers = new ArrayList<BlockWriter>();

	@Override
	public void awaitTermination() throws InterruptedException {
		int index = 0;

		while (index < 100) {
			boolean allEmpty = true;

			for (BlockingQueue<Block> queue : m_queues) {
				if (!queue.isEmpty()) {
					allEmpty = false;
					break;
				}
			}

			if (allEmpty) {
				break;
			}
			Thread.sleep(100);
			index++;
		}

		for (BlockWriter writer : m_writers) {
			writer.shutdown();
		}
	}

	@Override
	public void dump(Block block) throws IOException {
		int hash = Math.abs(block.getDomain().hashCode());
		int index = hash % m_writers.size();
		BlockingQueue<Block> queue = m_queues.get(index);
		boolean success = queue.offer(block);

		if (!success) {
			m_statisticManager.addBlockLoss(1);
			LOGGER.warn("Block dump queue is full, index={}.", index);
			Cat.logError(new IllegalStateException("Block dump queue is full, index=" + index));
		} else {
			m_statisticManager.addBlockTotal(1);
		}
	}

	@Override
	public void initialize(int hour) {
		int threads = m_configManager.getMessageDumpThreads();

		for (int i = 0; i < threads; i++) {
			BlockingQueue<Block> queue = new ArrayBlockingQueue<Block>(10000);
			BlockWriter writer = newWriter();

			m_queues.add(queue);
			m_writers.add(writer);

			writer.initialize(hour, i, queue);
			Threads.forGroup("Cat").start(writer);
		}
	}

	private BlockWriter newWriter() {
		SpringBackedBlockWriter writer = new SpringBackedBlockWriter();

		writer.setBucketManager(m_bucketManager);
		writer.setStatisticManager(m_statisticManager);
		return writer;
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
