package com.dianping.cat.analysis;

import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.lookup.ContainerLoader;

public class ContainerMessageAnalyzerFactory implements MessageAnalyzerFactory {

	@Override
	public MessageAnalyzer createAnalyzer(String name) {
		try {
			return getContainer().lookup(MessageAnalyzer.class, name);
		} catch (ComponentLookupException e) {
			throw new IllegalStateException("Unable to lookup message analyzer: " + name, e);
		}
	}

	@Override
	public Map<String, MessageAnalyzer> getAnalyzerMap() {
		try {
			return getContainer().lookupMap(MessageAnalyzer.class);
		} catch (ComponentLookupException e) {
			throw new IllegalStateException("Unable to lookup message analyzer map.", e);
		}
	}

	private PlexusContainer getContainer() {
		PlexusContainer container = ContainerLoader.getDefaultContainer();

		if (container == null) {
			throw new IllegalStateException("Plexus container is not available for message analyzer factory.");
		}
		return container;
	}
}
