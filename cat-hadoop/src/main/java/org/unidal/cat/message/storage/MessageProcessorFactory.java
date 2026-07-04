package org.unidal.cat.message.storage;

import java.util.concurrent.BlockingQueue;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageProcessorFactory {

	MessageProcessor createMessageProcessor(int hour, int index, BlockingQueue<MessageTree> queue);
}
