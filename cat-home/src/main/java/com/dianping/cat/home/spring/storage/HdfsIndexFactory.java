package com.dianping.cat.home.spring.storage;

import java.io.IOException;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.Index;
import org.unidal.cat.message.storage.IndexFactory;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.cat.message.storage.hdfs.HdfsIndex;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;

import com.dianping.cat.config.server.ServerConfigManager;

@Component("hdfsIndexFactory")
public class HdfsIndexFactory implements IndexFactory {
	@Resource(name = "hdfsMessagePathBuilder")
	private PathBuilder pathBuilder;

	@Resource(name = "hdfsSystemManager")
	private HdfsSystemManager hdfsSystemManager;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "hdfsTokenMappingManager")
	private TokenMappingManager tokenMappingManager;

	@Override
	public Index createIndex(String domain, String ip, int hour) throws IOException {
		HdfsIndex index = new HdfsIndex();

		index.setPathBuilder(pathBuilder);
		index.setFileSystemManager(hdfsSystemManager);
		index.setServerConfigManager(serverConfigManager);
		index.setTokenMappingManager(tokenMappingManager);
		index.initialize(domain, ip, hour);
		return index;
	}
}
