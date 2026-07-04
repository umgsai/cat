package com.dianping.cat.analysis;

import java.util.Map;

public interface MessageAnalyzerFactory {

	MessageAnalyzer createAnalyzer(String name);

	Map<String, MessageAnalyzer> getAnalyzerMap();
}
