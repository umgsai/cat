package com.dianping.cat.home.spring.storage;

import java.util.concurrent.BlockingQueue;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.cat.message.storage.BlockWriterFactory;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.internals.DefaultBlockWriter;

import com.dianping.cat.statistic.ServerStatisticManager;

@Component("legacyBlockWriterFactory")
public class LegacyBlockWriterFactory implements BlockWriterFactory {
	@Resource(name = "local")
	private BucketManager localBucketManager;

	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager serverStatisticManager;

	@Override
	public BlockWriter createBlockWriter(int hour, int index, BlockingQueue<Block> queue) {
		DefaultBlockWriter writer = new DefaultBlockWriter();

		writer.setBucketManager(localBucketManager);
		writer.setStatisticManager(serverStatisticManager);
		writer.initialize(hour, index, queue);
		return writer;
	}
}
