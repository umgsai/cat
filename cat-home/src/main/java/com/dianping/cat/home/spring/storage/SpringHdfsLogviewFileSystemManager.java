package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;

@Component("hdfsLogviewFileSystemManager")
public class SpringHdfsLogviewFileSystemManager extends FileSystemManager {
	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@PostConstruct
	public void configure() {
		setConfigManager(serverConfigManager);
		initialize();
	}
}
