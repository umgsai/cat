package com.dianping.cat.home.spring.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.cat.message.storage.MessageProcessor;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.spi.BufReleaseHelper;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.tree.MessageId;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.support.Threads;

public class SpringBackedMessageDumper implements MessageDumper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBackedMessageDumper.class);

	private BlockDumperManager m_blockDumperManager;

	private BucketManager m_bucketManager;

	private ServerConfigManager m_configManager;

	private MessageFinderManager m_finderManager;

	private ServerStatisticManager m_statisticManager;

	private final List<BlockingQueue<MessageTree>> m_queues = new ArrayList<BlockingQueue<MessageTree>>();

	private final List<MessageProcessor> m_processors = new ArrayList<MessageProcessor>();

	private int m_processThreads;

	@Override
	public void awaitTermination(int hour) throws InterruptedException {
		closeMessageProcessor();
		m_blockDumperManager.close(hour);
		m_bucketManager.closeBuckets(hour);
	}

	private void closeMessageProcessor() throws InterruptedException {
		while (true) {
			boolean allEmpty = true;

			for (BlockingQueue<MessageTree> queue : m_queues) {
				if (!queue.isEmpty()) {
					allEmpty = false;
					break;
				}
			}

			if (allEmpty) {
				break;
			}
			Thread.sleep(1);
		}

		for (MessageProcessor processor : m_processors) {
			processor.shutdown();
		}
	}

	@Override
	public void initialize(int hour) {
		m_processThreads = m_configManager.getMessageProcessorThreads();

		for (int i = 0; i < m_processThreads; i++) {
			BlockingQueue<MessageTree> queue = new ArrayBlockingQueue<MessageTree>(10000);
			MessageProcessor processor = newProcessor();

			m_queues.add(queue);
			m_processors.add(processor);

			processor.initialize(hour, i, queue);
			Threads.forGroup("Cat").start(processor);
		}
	}

	private int getIndex(String key) {
		return Math.abs(key.hashCode()) % m_processThreads;
	}

	private MessageProcessor newProcessor() {
		SpringBackedMessageProcessor processor = new SpringBackedMessageProcessor();

		processor.setBlockDumperManager(m_blockDumperManager);
		processor.setFinderManager(m_finderManager);
		return processor;
	}

	@Override
	public void process(MessageTree tree) {
		MessageId id = tree.getFormatMessageId();
		String domain = id.getDomain();
		int index = getIndex(id.getIpAddressInHex());
		int size = tree.getBuffer().readableBytes();
		BlockingQueue<MessageTree> queue = m_queues.get(index);
		boolean success = queue.offer(tree);

		if (!success) {
			m_statisticManager.addMessageDumpLoss(1);
			LOGGER.warn("Message tree queue is full, index={}.", index);
			BufReleaseHelper.release(tree.getBuffer());
		} else {
			m_statisticManager.addMessageSize(domain, size);
		}
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
