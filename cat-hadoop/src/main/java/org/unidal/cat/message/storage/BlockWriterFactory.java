package org.unidal.cat.message.storage;

import java.util.concurrent.BlockingQueue;

public interface BlockWriterFactory {

	BlockWriter createBlockWriter(int hour, int index, BlockingQueue<Block> queue);
}
