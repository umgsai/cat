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
package com.dianping.cat.alarm.spi.decorator;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;

@Component("ruleFTLDecorator")
public class RuleFTLDecorator {
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleFTLDecorator.class);

	public volatile Configuration configuration;

	public String generateConfigsHtml(String templateValue) {
		Map<Object, Object> dataMap = new HashMap<Object, Object>();
		StringWriter sw = new StringWriter(5000);

		dataMap.put("configs", templateValue);
		try {
			Template configsTemplate = getConfiguration().getTemplate("rule_configs.ftl");
			configsTemplate.process(dataMap, sw);
		} catch (Exception e) {
			LOGGER.error("Unable to render alert rule config html, template=rule_configs.ftl.", e);
			Cat.logError(e);
		}
		return sw.toString();
	}

	private Configuration getConfiguration() {
		if (configuration == null) {
			initialize();
		}
		return configuration;
	}

	@PostConstruct
	public void initialize() {
		if (configuration != null) {
			return;
		}

		Configuration configuration = new Configuration();

		configuration.setDefaultEncoding("UTF-8");
		try {
			configuration.setClassForTemplateLoading(this.getClass(), "/freemaker");
			this.configuration = configuration;
		} catch (Exception e) {
			LOGGER.error("Unable to initialize alert rule FTL decorator template loading.", e);
			Cat.logError(e);
		}
	}
}
