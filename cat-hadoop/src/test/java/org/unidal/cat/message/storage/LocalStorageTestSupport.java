/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unidal.cat.message.storage;

import java.io.File;
import java.io.IOException;

import org.unidal.cat.message.storage.internals.DefaultByteBufCache;
import org.unidal.cat.message.storage.internals.DefaultStorageConfiguration;
import org.unidal.cat.message.storage.local.LocalFileBuilder;
import org.unidal.cat.message.storage.local.LocalIndex;
import org.unidal.cat.message.storage.local.LocalIndexManager;
import org.unidal.cat.message.storage.local.LocalTokenMapping;
import org.unidal.cat.message.storage.local.LocalTokenMappingManager;

public class LocalStorageTestSupport {
	private final DefaultByteBufCache m_byteBufCache;

	private final DefaultStorageConfiguration m_config;

	private final LocalIndexManager m_indexManager;

	private final LocalFileBuilder m_pathBuilder;

	private final LocalTokenMappingManager m_tokenMappingManager;

	public LocalStorageTestSupport(File baseDir) {
		m_config = new DefaultStorageConfiguration();
		m_config.setBaseDataDir(baseDir);

		m_pathBuilder = new LocalFileBuilder();
		m_pathBuilder.setConfig(m_config);

		m_byteBufCache = new DefaultByteBufCache();
		m_byteBufCache.initialize();

		m_tokenMappingManager = new LocalTokenMappingManager();
		m_tokenMappingManager.setTokenMappingFactory(this::createTokenMapping);

		m_indexManager = new LocalIndexManager();
		m_indexManager.setPathBuilder(m_pathBuilder);
		m_indexManager.setIndexFactory(this::createIndex);
	}

	private Index createIndex(String domain, String ip, int hour) throws IOException {
		LocalIndex index = new LocalIndex();

		index.setPathBuilder(m_pathBuilder);
		index.setBufCache(m_byteBufCache);
		index.setTokenMappingManager(m_tokenMappingManager);
		index.initialize(domain, ip, hour);
		return index;
	}

	public TokenMapping createTokenMapping(int hour, String ip) throws IOException {
		LocalTokenMapping mapping = new LocalTokenMapping();

		mapping.setPathBuilder(m_pathBuilder);
		mapping.open(hour, ip);
		return mapping;
	}

	public IndexManager getIndexManager() {
		return m_indexManager;
	}

	public TokenMappingManager getTokenMappingManager() {
		return m_tokenMappingManager;
	}
}
