package org.unidal.cat.message.storage;

public interface MessageDumperFactory {

	public MessageDumper createMessageDumper(int hour);
}
