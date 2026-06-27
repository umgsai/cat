package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.MessageDumperFactory;
import org.unidal.cat.message.storage.internals.DefaultMessageDumperManager;

@Component("legacyMessageDumperManager")
public class SpringLegacyMessageDumperManager extends DefaultMessageDumperManager {
	@Resource(name = "legacyMessageDumperFactory")
	private MessageDumperFactory messageDumperFactory;

	@PostConstruct
	public void configure() {
		setMessageDumperFactory(messageDumperFactory);
		initialize();
	}
}
