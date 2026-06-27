package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketFactory;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.PathBuilder;

@Component("hdfsMessageBucketManager")
public class SpringHdfsMessageBucketManager extends HdfsMessageBucketManager {
	@Resource(name = "hdfsLogviewFileSystemManager")
	private FileSystemManager fileSystemManager;

	@Resource(name = "pathBuilder")
	private PathBuilder pathBuilder;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "hdfsMessageBucketFactory")
	private HdfsMessageBucketFactory bucketFactory;

	@PostConstruct
	public void configure() {
		setFileSystemManager(fileSystemManager);
		setPathBuilder(pathBuilder);
		setServerConfigManager(serverConfigManager);
		setBucketFactory(bucketFactory);
		initialize();
	}
}
