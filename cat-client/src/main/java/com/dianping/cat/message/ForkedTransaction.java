package com.dianping.cat.message;

import java.io.Closeable;

public interface ForkedTransaction extends Transaction, Closeable {
	public static String FORKED = "Forked";

	public static String DETACHED = "Detached";

	public static String EMBEDDED = "Embedded";

	void close();

	String getMessageId();

	String getParentMessageId();

	String getRootMessageId();

	ForkedTransaction join();

	void setMessageId(String messageId);
}