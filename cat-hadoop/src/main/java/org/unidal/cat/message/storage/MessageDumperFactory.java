package org.unidal.cat.message.storage;

public interface MessageDumperFactory {

	MessageDumper createMessageDumper(int hour);
}
