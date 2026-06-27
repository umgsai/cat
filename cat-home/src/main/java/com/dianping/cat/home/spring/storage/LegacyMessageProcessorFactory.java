package com.dianping.cat.home.spring.storage;

import java.util.concurrent.BlockingQueue;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.cat.message.storage.MessageProcessorFactory;
import org.unidal.cat.message.storage.internals.DefaultMessageProcessor;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.spi.MessageTree;

@Component("legacyMessageProcessorFactory")
public class LegacyMessageProcessorFactory implements MessageProcessorFactory {
	@Resource(name = "blockDumperManager")
	private BlockDumperManager blockDumperManager;

	@Resource(name = "messageFinderManager")
	private MessageFinderManager messageFinderManager;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Override
	public MessageProcessor createMessageProcessor(int hour, int index, BlockingQueue<MessageTree> queue) {
		DefaultMessageProcessor processor = new DefaultMessageProcessor();

		processor.setBlockDumperManager(blockDumperManager);
		processor.setFinderManager(messageFinderManager);
		processor.setConfigManager(serverConfigManager);
		processor.initialize(hour, index, queue);
		return processor;
	}
}
