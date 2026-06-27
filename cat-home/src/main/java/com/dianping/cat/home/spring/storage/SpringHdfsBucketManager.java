package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.BucketFactory;
import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import org.unidal.cat.message.storage.hdfs.MessageConsumerFinder;

import com.dianping.cat.config.server.ServerConfigManager;

@Component("hdfsBucketManager")
public class SpringHdfsBucketManager extends HdfsBucketManager {
	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "hdfsSystemManager")
	private HdfsSystemManager hdfsSystemManager;

	@Resource(name = "hdfsMessageConsumerFinder")
	private MessageConsumerFinder messageConsumerFinder;

	@Resource(name = "hdfsBucketFactory")
	private BucketFactory bucketFactory;

	@PostConstruct
	public void configure() {
		setConfigManager(serverConfigManager);
		setFileSystemManager(hdfsSystemManager);
		setConsumerFinder(messageConsumerFinder);
		setBucketFactory(bucketFactory);
		initialize();
	}
}
