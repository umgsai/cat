package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.internals.DefaultStorageConfiguration;

@Component("storageConfiguration")
public class SpringStorageConfiguration extends DefaultStorageConfiguration {

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}
}
