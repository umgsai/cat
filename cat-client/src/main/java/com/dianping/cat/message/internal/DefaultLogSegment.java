package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.Log;
import com.dianping.cat.message.LogSegment;

public class DefaultLogSegment implements LogSegment {
	private final List<Log> m_logs = new ArrayList<>();

	@Override
	public List<Log> getLogs() {
		return m_logs;
	}

	@Override
	public String getDomain() {
		return null;
	}

	@Override
	public String getHostName() {
		return null;
	}

	@Override
	public String getIpAddress() {
		return null;
	}
}
