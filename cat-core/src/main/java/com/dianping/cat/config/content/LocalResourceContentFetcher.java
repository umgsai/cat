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
package com.dianping.cat.config.content;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;

@Component("contentFetcher")
public class LocalResourceContentFetcher implements ContentFetcher {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LocalResourceContentFetcher.class);

	private final String PATH = "/config/";

	@Override
	public String getConfigContent(String configName) {
		String path = PATH + configName + ".xml";
		String content = "";

		try (InputStream in = getClass().getResourceAsStream(path)) {
			content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			LOGGER.warn("can't find local default config {}", configName, e);
			Cat.logError(configName + " can't find", e);
		}
		return content;
	}
}
