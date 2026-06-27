package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.TokenMappingFactory;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMappingManager;

@Component("hdfsTokenMappingManager")
public class SpringHdfsTokenMappingManager extends HdfsTokenMappingManager {
	@Resource(name = "hdfsTokenMappingFactory")
	private TokenMappingFactory tokenMappingFactory;

	@PostConstruct
	public void configure() {
		setTokenMappingFactory(tokenMappingFactory);
	}
}
