package com.dianping.cat.home.spring.storage;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperFactory;
import org.unidal.cat.message.storage.MessageProcessorFactory;
import org.unidal.cat.message.storage.internals.DefaultMessageDumper;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.statistic.ServerStatisticManager;

@Component("legacyMessageDumperFactory")
public class LegacyMessageDumperFactory implements MessageDumperFactory {
	@Resource(name = "blockDumperManager")
	private BlockDumperManager blockDumperManager;

	@Resource(name = "local")
	private BucketManager localBucketManager;

	@Resource(name = "legacyMessageProcessorFactory")
	private MessageProcessorFactory messageProcessorFactory;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager serverStatisticManager;

	@Override
	public MessageDumper createMessageDumper(int hour) {
		DefaultMessageDumper dumper = new DefaultMessageDumper();

		dumper.setBlockDumperManager(blockDumperManager);
		dumper.setBucketManager(localBucketManager);
		dumper.setConfigManager(serverConfigManager);
		dumper.setMessageProcessorFactory(messageProcessorFactory);
		dumper.setStatisticManager(serverStatisticManager);
		dumper.initialize(hour);
		return dumper;
	}
}
