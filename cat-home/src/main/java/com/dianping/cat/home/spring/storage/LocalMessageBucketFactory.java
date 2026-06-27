package com.dianping.cat.home.spring.storage;

import java.io.IOException;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketFactory;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.internals.ByteBufCache;
import org.unidal.cat.message.storage.local.LocalBucket;

import com.dianping.cat.config.server.ServerConfigManager;

@Component("localMessageBucketFactory")
public class LocalMessageBucketFactory implements BucketFactory {
	@Resource(name = "localMessagePathBuilder")
	private PathBuilder pathBuilder;

	@Resource(name = "byteBufCache")
	private ByteBufCache byteBufCache;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Override
	public Bucket createBucket(String domain, String ip, int hour, boolean writeMode) throws IOException {
		LocalBucket bucket = new LocalBucket();

		bucket.setPathBuilder(pathBuilder);
		bucket.setBufCache(byteBufCache);
		bucket.setConfig(serverConfigManager);
		return bucket;
	}
}
