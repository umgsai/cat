package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.clean.HdfsUploader;
import org.unidal.cat.message.storage.clean.LogviewProcessor;

import com.dianping.cat.config.server.ServerConfigManager;

@Component("logviewProcessor")
public class SpringLogviewProcessor extends LogviewProcessor {
	@Resource(name = "hdfsUploader")
	private HdfsUploader hdfsUploader;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@PostConstruct
	public void configure() {
		setHdfsUploader(hdfsUploader);
		setConfigManager(serverConfigManager);
		initialize();
	}
}
