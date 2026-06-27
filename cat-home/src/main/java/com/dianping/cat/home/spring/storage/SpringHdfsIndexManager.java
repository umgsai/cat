package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.IndexFactory;
import org.unidal.cat.message.storage.hdfs.HdfsIndexManager;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import org.unidal.cat.message.storage.hdfs.MessageConsumerFinder;

import com.dianping.cat.config.server.ServerConfigManager;

@Component("hdfsIndexManager")
public class SpringHdfsIndexManager extends HdfsIndexManager {
	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "hdfsSystemManager")
	private HdfsSystemManager hdfsSystemManager;

	@Resource(name = "hdfsMessageConsumerFinder")
	private MessageConsumerFinder messageConsumerFinder;

	@Resource(name = "hdfsIndexFactory")
	private IndexFactory indexFactory;

	@PostConstruct
	public void configure() {
		setConfigManager(serverConfigManager);
		setFileSystemManager(hdfsSystemManager);
		setConsumerFinder(messageConsumerFinder);
		setIndexFactory(indexFactory);
		initialize();
	}
}
