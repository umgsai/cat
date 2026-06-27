package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.BlockDumperFactory;
import org.unidal.cat.message.storage.internals.DefaultBlockDumperManager;

@Component("legacyBlockDumperManager")
public class SpringLegacyBlockDumperManager extends DefaultBlockDumperManager {
	@Resource(name = "legacyBlockDumperFactory")
	private BlockDumperFactory blockDumperFactory;

	@PostConstruct
	public void configure() {
		setBlockDumperFactory(blockDumperFactory);
	}
}
