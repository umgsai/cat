package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.storage.MessageBucketFactory;
import com.dianping.cat.statistic.ServerStatisticManager;

@Component("legacyLocalMessageBucketManager")
public class SpringLegacyLocalMessageBucketManager extends LocalMessageBucketManager {
	@Resource(name = "legacyMessageBucketFactory")
	private MessageBucketFactory bucketFactory;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager serverStatisticManager;

	@Resource(name = "pathBuilder")
	private PathBuilder pathBuilder;

	@PostConstruct
	public void configure() {
		setConfigManager(serverConfigManager);
		setPathBuilder(pathBuilder);
		setServerStateManager(serverStatisticManager);
		setBucketFactory(bucketFactory);
		initialize();
	}
}
