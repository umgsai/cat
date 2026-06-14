package org.unidal.cat.message.storage;

public interface BlockDumperFactory {

	public BlockDumper createBlockDumper(int hour);
}
