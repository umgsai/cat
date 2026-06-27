package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.hdfs.HdfsFileBuilder;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;

@Component("hdfsMessagePathBuilder")
public class SpringHdfsMessagePathBuilder extends HdfsFileBuilder {
	@Resource(name = "hdfsSystemManager")
	private HdfsSystemManager hdfsSystemManager;

	@PostConstruct
	public void configure() {
		setFileSystemManager(hdfsSystemManager);
	}
}
