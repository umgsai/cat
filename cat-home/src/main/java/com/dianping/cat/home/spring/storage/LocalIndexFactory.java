package com.dianping.cat.home.spring.storage;

import java.io.IOException;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.Index;
import org.unidal.cat.message.storage.IndexFactory;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.cat.message.storage.internals.ByteBufCache;
import org.unidal.cat.message.storage.local.LocalIndex;

@Component("localIndexFactory")
public class LocalIndexFactory implements IndexFactory {
	@Resource(name = "localMessagePathBuilder")
	private PathBuilder pathBuilder;

	@Resource(name = "byteBufCache")
	private ByteBufCache byteBufCache;

	@Resource(name = "localTokenMappingManager")
	private TokenMappingManager tokenMappingManager;

	@Override
	public Index createIndex(String domain, String ip, int hour) throws IOException {
		LocalIndex index = new LocalIndex();

		index.setPathBuilder(pathBuilder);
		index.setBufCache(byteBufCache);
		index.setTokenMappingManager(tokenMappingManager);
		index.initialize(domain, ip, hour);
		return index;
	}
}
