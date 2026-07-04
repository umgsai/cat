package org.unidal.cat.message.storage;

public interface BlockDumperFactory {

	BlockDumper createBlockDumper(int hour);
}
