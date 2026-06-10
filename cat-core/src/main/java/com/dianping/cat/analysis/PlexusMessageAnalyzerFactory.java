package com.dianping.cat.analysis;

import java.util.Map;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = MessageAnalyzerFactory.class)
public class PlexusMessageAnalyzerFactory extends ContainerHolder implements MessageAnalyzerFactory {

	@Override
	public MessageAnalyzer createAnalyzer(String name) {
		return lookup(MessageAnalyzer.class, name);
	}

	@Override
	public Map<String, MessageAnalyzer> getAnalyzerMap() {
		return lookupMap(MessageAnalyzer.class);
	}
}
