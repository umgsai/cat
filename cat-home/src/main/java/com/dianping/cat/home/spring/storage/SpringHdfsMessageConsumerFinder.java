package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.hdfs.HdfsMessageConsumerFinder;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;

@Component("hdfsMessageConsumerFinder")
public class SpringHdfsMessageConsumerFinder extends HdfsMessageConsumerFinder {
	@Resource(name = "hdfsSystemManager")
	private HdfsSystemManager hdfsSystemManager;

	@PostConstruct
	public void configure() {
		setFileSystemManager(hdfsSystemManager);
	}
}
