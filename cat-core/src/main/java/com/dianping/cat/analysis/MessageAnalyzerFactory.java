package com.dianping.cat.analysis;

import java.util.Map;

public interface MessageAnalyzerFactory {

	public MessageAnalyzer createAnalyzer(String name);

	public Map<String, MessageAnalyzer> getAnalyzerMap();
}
