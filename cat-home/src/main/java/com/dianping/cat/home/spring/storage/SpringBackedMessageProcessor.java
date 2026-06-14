package com.dianping.cat.home.spring.storage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.MessageFinder;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.cat.message.storage.internals.DefaultBlock;

import com.dianping.cat.Cat;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.tree.MessageId;

public class SpringBackedMessageProcessor implements MessageProcessor, MessageFinder {
	private BlockDumperManager m_blockDumperManager;

	private MessageFinderManager m_finderManager;

	private BlockDumper m_dumper;

	private int m_index;

	private BlockingQueue<MessageTree> m_queue;

	private final ConcurrentHashMap<String, Block> m_blocks = new ConcurrentHashMap<String, Block>();

	private int m_hour;

	private AtomicBoolean m_enabled;

	private CountDownLatch m_latch;

	@Override
	public ByteBuf find(MessageId id) {
		Block block = m_blocks.get(id.getDomain());

		return block == null ? null : block.find(id);
	}

	@Override
	public String getName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return getClass().getSimpleName() + " " + sdf.format(new Date(TimeUnit.HOURS.toMillis(m_hour))) + "-" + m_index;
	}

	@Override
	public void initialize(int hour, int index, BlockingQueue<MessageTree> queue) {
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
		m_dumper = m_blockDumperManager.findOrCreate(hour);
		m_hour = hour;
		m_latch = new CountDownLatch(1);
		m_finderManager.register(hour, this);
	}

	private void processMessage(MessageTree tree) {
		MessageId id = tree.getFormatMessageId();
		String domain = id.getDomain();
		int hour = id.getHour();
		Block block = m_blocks.get(domain);

		if (block == null) {
			block = new DefaultBlock(domain, hour);
			m_blocks.put(domain, block);
		}

		ByteBuf buffer = tree.getBuffer();

		try {
			if (block.isFull()) {
				block.finish();
				m_dumper.dump(block);

				block = new DefaultBlock(domain, hour);
				m_blocks.put(domain, block);
			}
			block.pack(id, buffer);
		} catch (Exception e) {
			Cat.logError(e);
		} finally {
			ReferenceCountUtil.release(buffer);
		}
	}

	private MessageTree pollMessage() throws InterruptedException {
		return m_queue.poll(5, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try {
			while (m_enabled.get() || !m_queue.isEmpty()) {
				MessageTree tree = pollMessage();

				if (tree != null) {
					processMessage(tree);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		for (Block block : m_blocks.values()) {
			try {
				block.finish();
				m_dumper.dump(block);
			} catch (IOException e) {
				Cat.logError(e);
			}
		}
		m_blocks.clear();
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
	}

	public void setBlockDumperManager(BlockDumperManager blockDumperManager) {
		m_blockDumperManager = blockDumperManager;
	}

	public void setFinderManager(MessageFinderManager finderManager) {
		m_finderManager = finderManager;
	}
}
