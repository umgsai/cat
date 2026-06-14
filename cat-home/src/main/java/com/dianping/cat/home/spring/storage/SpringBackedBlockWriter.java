package com.dianping.cat.home.spring.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.statistic.ServerStatisticManager;

public class SpringBackedBlockWriter implements BlockWriter {
	private BucketManager m_bucketManager;

	private ServerStatisticManager m_statisticManager;

	private int m_index;

	private BlockingQueue<Block> m_queue;

	private long m_hour;

	private AtomicBoolean m_enabled;

	private CountDownLatch m_latch;

	@Override
	public String getName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return getClass().getSimpleName() + " " + sdf.format(new Date(TimeUnit.HOURS.toMillis(m_hour))) + "-" + m_index;
	}

	@Override
	public void initialize(int hour, int index, BlockingQueue<Block> queue) {
		m_hour = hour;
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
		m_latch = new CountDownLatch(1);
	}

	private void processBlock(String ip, Block block) {
		try {
			Bucket bucket = m_bucketManager.getBucket(block.getDomain(), ip, block.getHour(), true);

			bucket.puts(block.getData(), block.getOffsets());
		} catch (Exception e) {
			Cat.logError(ip, e);
		} catch (Error e) {
			Cat.logError(ip, e);
		} finally {
			block.clear();
		}
	}

	@Override
	public void run() {
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		try {
			while (m_enabled.get() || !m_queue.isEmpty()) {
				Block block = m_queue.poll(5, TimeUnit.MILLISECONDS);

				if (block != null) {
					long time = System.currentTimeMillis();
					processBlock(ip, block);
					m_statisticManager.addBlockTime(System.currentTimeMillis() - time);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		m_latch.countDown();
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);

		try {
			m_latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		while (true) {
			Block block = m_queue.poll();

			if (block == null) {
				break;
			}
			processBlock(NetworkInterfaceManager.INSTANCE.getLocalHostAddress(), block);
		}
	}

	public void setBucketManager(BucketManager bucketManager) {
		m_bucketManager = bucketManager;
	}

	public void setStatisticManager(ServerStatisticManager statisticManager) {
		m_statisticManager = statisticManager;
	}
}
