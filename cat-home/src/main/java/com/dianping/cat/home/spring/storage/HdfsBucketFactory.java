package com.dianping.cat.home.spring.storage;

import java.io.IOException;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketFactory;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.hdfs.HdfsBucket;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;

import com.dianping.cat.config.server.ServerConfigManager;

@Component("hdfsBucketFactory")
public class HdfsBucketFactory implements BucketFactory {
	@Resource(name = "hdfsMessagePathBuilder")
	private PathBuilder pathBuilder;

	@Resource(name = "hdfsSystemManager")
	private HdfsSystemManager hdfsSystemManager;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Override
	public Bucket createBucket(String domain, String ip, int hour, boolean writeMode) throws IOException {
		HdfsBucket bucket = new HdfsBucket();

		bucket.setPathBuilder(pathBuilder);
		bucket.setFileSystemManager(hdfsSystemManager);
		bucket.setServerConfigManager(serverConfigManager);
		bucket.initialize(domain, ip, hour, writeMode);
		return bucket;
	}
}
