package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.IndexFactory;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.local.LocalIndexManager;

@Component("localIndexManager")
public class SpringLocalIndexManager extends LocalIndexManager {
	@Resource(name = "localIndexFactory")
	private IndexFactory indexFactory;

	@Resource(name = "localMessagePathBuilder")
	private PathBuilder pathBuilder;

	@PostConstruct
	public void configure() {
		setPathBuilder(pathBuilder);
		setIndexFactory(indexFactory);
	}
}
