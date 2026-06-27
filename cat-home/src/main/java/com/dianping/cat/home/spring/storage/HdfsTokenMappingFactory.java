package com.dianping.cat.home.spring.storage;

import java.io.IOException;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.TokenMapping;
import org.unidal.cat.message.storage.TokenMappingFactory;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMapping;

@Component("hdfsTokenMappingFactory")
public class HdfsTokenMappingFactory implements TokenMappingFactory {
	@Resource(name = "hdfsMessagePathBuilder")
	private PathBuilder pathBuilder;

	@Resource(name = "hdfsSystemManager")
	private HdfsSystemManager hdfsSystemManager;

	@Override
	public TokenMapping createTokenMapping(int hour, String ip) throws IOException {
		HdfsTokenMapping mapping = new HdfsTokenMapping();

		mapping.setPathBuilder(pathBuilder);
		mapping.setFileSystemManager(hdfsSystemManager);
		mapping.open(hour, ip);
		return mapping;
	}
}
