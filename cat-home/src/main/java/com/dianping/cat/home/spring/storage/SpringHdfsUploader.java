package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.clean.HdfsUploader;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;

import com.dianping.cat.config.server.ServerConfigManager;

@Component("hdfsUploader")
public class SpringHdfsUploader extends HdfsUploader {
	@Resource(name = "hdfsSystemManager")
	private HdfsSystemManager hdfsSystemManager;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@PostConstruct
	public void configure() {
		setFileSystemManager(hdfsSystemManager);
		setServerConfigManager(serverConfigManager);
		initialize();
	}
}
