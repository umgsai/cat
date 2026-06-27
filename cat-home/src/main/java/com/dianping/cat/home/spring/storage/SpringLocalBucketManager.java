package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.BucketFactory;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.local.LocalBucketManager;

@Component("local")
public class SpringLocalBucketManager extends LocalBucketManager {
	@Resource(name = "localMessageBucketFactory")
	private BucketFactory bucketFactory;

	@Resource(name = "localMessagePathBuilder")
	private PathBuilder pathBuilder;

	@PostConstruct
	public void configure() {
		setPathBuilder(pathBuilder);
		setBucketFactory(bucketFactory);
	}
}
