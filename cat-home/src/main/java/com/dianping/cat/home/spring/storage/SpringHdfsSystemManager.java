package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;

import com.dianping.cat.config.server.ServerConfigManager;

@Component("hdfsSystemManager")
public class SpringHdfsSystemManager extends HdfsSystemManager {
	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@PostConstruct
	public void configure() {
		setConfigManager(serverConfigManager);
		initialize();
	}
}
