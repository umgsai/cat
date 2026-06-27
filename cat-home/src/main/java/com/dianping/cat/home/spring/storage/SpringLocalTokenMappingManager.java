package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.TokenMappingFactory;
import org.unidal.cat.message.storage.local.LocalTokenMappingManager;

@Component("localTokenMappingManager")
public class SpringLocalTokenMappingManager extends LocalTokenMappingManager {
	@Resource(name = "localTokenMappingFactory")
	private TokenMappingFactory tokenMappingFactory;

	@PostConstruct
	public void configure() {
		setTokenMappingFactory(tokenMappingFactory);
	}
}
