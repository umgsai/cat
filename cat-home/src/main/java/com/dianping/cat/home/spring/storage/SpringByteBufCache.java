package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.internals.DefaultByteBufCache;

@Component("byteBufCache")
public class SpringByteBufCache extends DefaultByteBufCache {

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}
}
