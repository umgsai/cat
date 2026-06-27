package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.cat.message.storage.local.LocalFileBuilder;

@Component("localMessagePathBuilder")
public class SpringLocalMessagePathBuilder extends LocalFileBuilder {
	@Resource(name = "storageConfiguration")
	private StorageConfiguration storageConfiguration;

	@PostConstruct
	public void configure() {
		setConfig(storageConfiguration);
	}
}
