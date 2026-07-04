package com.dianping.cat.message;

public interface Log {
	/**
	 * The time stamp the message was created.
	 * 
	 * @return message creation time stamp in milliseconds
	 */
	long getTimestamp();

	String getSeverity();

	String getMessage();
}
