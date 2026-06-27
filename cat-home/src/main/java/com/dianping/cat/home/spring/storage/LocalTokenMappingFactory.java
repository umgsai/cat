package com.dianping.cat.home.spring.storage;

import java.io.IOException;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.TokenMapping;
import org.unidal.cat.message.storage.TokenMappingFactory;
import org.unidal.cat.message.storage.local.LocalTokenMapping;

@Component("localTokenMappingFactory")
public class LocalTokenMappingFactory implements TokenMappingFactory {
	@Resource(name = "localMessagePathBuilder")
	private PathBuilder pathBuilder;

	@Override
	public TokenMapping createTokenMapping(int hour, String ip) throws IOException {
		LocalTokenMapping mapping = new LocalTokenMapping();

		mapping.setPathBuilder(pathBuilder);
		mapping.open(hour, ip);
		return mapping;
	}
}
