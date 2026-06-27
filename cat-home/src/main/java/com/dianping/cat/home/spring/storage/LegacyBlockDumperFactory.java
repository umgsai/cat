package com.dianping.cat.home.spring.storage;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperFactory;
import org.unidal.cat.message.storage.BlockWriterFactory;
import org.unidal.cat.message.storage.internals.DefaultBlockDumper;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.statistic.ServerStatisticManager;

@Component("legacyBlockDumperFactory")
public class LegacyBlockDumperFactory implements BlockDumperFactory {
	@Resource(name = "legacyBlockWriterFactory")
	private BlockWriterFactory blockWriterFactory;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager serverStatisticManager;

	@Override
	public BlockDumper createBlockDumper(int hour) {
		DefaultBlockDumper dumper = new DefaultBlockDumper();

		dumper.setBlockWriterFactory(blockWriterFactory);
		dumper.setConfigManager(serverConfigManager);
		dumper.setStatisticManager(serverStatisticManager);
		dumper.initialize(hour);
		return dumper;
	}
}
