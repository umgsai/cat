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
package com.dianping.cat.report.page.dependency.graph;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.mybatis.data.ConfigDO;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.home.dependency.config.transform.DefaultSaxParser;

@Component
public class TopologyGraphConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(TopologyGraphConfigManager.class);

	private static final String AVG_STR = Chinese.RESPONSE_TIME;

	private static final String ERROR_STR = Chinese.EXCEPTION_COUNT;

	private static final String TOTAL_STR = Chinese.TOTAL_COUNT;

	private static final String MILLISECOND = "(ms)";

	private static final int OK = GraphConstrant.OK;

	private static final int WARN = GraphConstrant.WARN;

	private static final int ERROR = GraphConstrant.ERROR;

	private static final String CONFIG_NAME = "topologyConfig";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	private TopologyGraphConfig config;

	private DecimalFormat decimalFormat = new DecimalFormat("0.0");

	private long configId;

	private String fileName;

	private Set<String> pigeonCalls = new HashSet<String>(Arrays.asList("Call", "PigeonCall", "PigeonClient"));

	private Set<String> pigeonServices = new HashSet<String>(Arrays.asList("Service", "PigeonService", "PigeonServer"));

	private String buildDes(String... args) {
		StringBuilder sb = new StringBuilder();
		int len = args.length;

		for (int i = 0; i < len; i++) {
			sb.append(args[i]).append(GraphConstrant.DELIMITER);
		}

		return sb.toString();
	}

	public Pair<Integer, String> buildEdgeState(String domain, Dependency dependency) {
		ensureInitialized();

		String type = formatType(dependency.getType());
		String from = domain;
		String to = dependency.getTarget();
		EdgeConfig config = queryEdgeConfig(type, from, to);
		long error = dependency.getErrorCount();
		StringBuilder sb = new StringBuilder();
		int errorCode = OK;

		if (config != null) {
			double avg = dependency.getAvg();
			long totalCount = dependency.getTotalCount();
			int minCount = config.getMinCountThreshold();

			sb.append(buildDes(type, TOTAL_STR, String.valueOf(dependency.getTotalCount()))).append(GraphConstrant.ENTER);

			if (avg >= config.getErrorResponseTime() && totalCount > minCount) {
				errorCode = ERROR;
				sb.append(buildErrorDes(type, AVG_STR, decimalFormat.format(avg), MILLISECOND)).append(GraphConstrant.ENTER);
			} else if (avg >= config.getWarningResponseTime() && totalCount > minCount) {
				errorCode = WARN;
				sb.append(buildWarningDes(type, AVG_STR, decimalFormat.format(avg), MILLISECOND)).append(GraphConstrant.ENTER);
			} else {
				sb.append(buildDes(type, AVG_STR, decimalFormat.format(avg), MILLISECOND)).append(GraphConstrant.ENTER);
			}
			if (error >= config.getErrorThreshold() && totalCount > minCount) {
				errorCode = ERROR;
				sb.append(buildErrorDes(type, ERROR_STR, String.valueOf(error))).append(GraphConstrant.ENTER);
			} else if (error >= config.getWarningThreshold() && totalCount > minCount) {
				errorCode = WARN;
				sb.append(buildWarningDes(type, ERROR_STR, String.valueOf(error))).append(GraphConstrant.ENTER);
			} else if (error > 0) {
				sb.append(buildDes(type, ERROR_STR, String.valueOf(error))).append(GraphConstrant.ENTER);
			}
		}
		return Pair.of(errorCode, sb.toString());
	}

	private String buildErrorDes(String... args) {
		StringBuilder sb = new StringBuilder("<span style='color:red'>");
		String content = buildDes(args);

		sb.append(content).append("</span>");
		return sb.toString();
	}

	public Pair<Integer, String> buildNodeState(String domain, Index index) {
		ensureInitialized();

		String type = index.getName();
		String realType = formatType(type);
		DomainConfig config = queryNodeConfig(realType, domain);
		int errorCode = OK;
		StringBuilder sb = new StringBuilder();

		if (config != null) {
			double avg = index.getAvg();
			long error = index.getErrorCount();
			long totalCount = index.getTotalCount();
			int minCount = config.getMinCountThreshold();

			sb.append(type).append(GraphConstrant.DELIMITER);

			if (index.getTotalCount() > 0 && !type.equalsIgnoreCase("Exception")) {
				sb.append(buildDes(TOTAL_STR, String.valueOf(index.getTotalCount())));
			}
			if (avg >= config.getErrorResponseTime() && totalCount > minCount) {
				errorCode = ERROR;
				sb.append(buildErrorDes(AVG_STR, decimalFormat.format(avg), MILLISECOND));
			} else if (avg >= config.getWarningResponseTime() && totalCount > minCount) {
				errorCode = WARN;
				sb.append(buildWarningDes(AVG_STR, decimalFormat.format(avg), MILLISECOND));
			} else {
				if (!type.equalsIgnoreCase("Exception")) {
					sb.append(buildDes(AVG_STR, decimalFormat.format(avg), MILLISECOND));
				}
			}
			if (error >= config.getErrorThreshold() && totalCount > minCount) {
				errorCode = ERROR;
				sb.append(buildErrorDes(ERROR_STR, String.valueOf(error)));
			} else if (error >= config.getWarningThreshold() && totalCount > minCount) {
				errorCode = WARN;
				sb.append(buildWarningDes(ERROR_STR, String.valueOf(error)));
			} else if (error > 0) {
				sb.append(buildDes(ERROR_STR, String.valueOf(error)));
			}
			sb.append(GraphConstrant.ENTER);
		}
		return Pair.of(errorCode, sb.toString());
	}

	private String buildWarningDes(String... args) {
		StringBuilder sb = new StringBuilder("<span style='color:#bfa22f'>");
		String content = buildDes(args);

		sb.append(content).append("</span>");
		return sb.toString();
	}

	private EdgeConfig convertNodeConfig(DomainConfig config) {
		EdgeConfig edgeConfig = new EdgeConfig();

		edgeConfig.setMinCountThreshold(config.getMinCountThreshold());
		edgeConfig.setWarningResponseTime(config.getWarningResponseTime());
		edgeConfig.setErrorResponseTime(config.getErrorResponseTime());
		edgeConfig.setWarningThreshold(config.getWarningThreshold());
		edgeConfig.setErrorThreshold(config.getErrorThreshold());
		return edgeConfig;
	}

	public boolean deleteDomainConfig(String type, String domain) {
		ensureInitialized();

		NodeConfig types = config.getNodeConfigs().get(type);
		types.removeDomainConfig(domain);
		return storeConfig();
	}

	public boolean deleteEdgeConfig(String type, String from, String to) {
		ensureInitialized();

		String key = type + ':' + from + ':' + to;
		config.removeEdgeConfig(key);
		return storeConfig();
	}

	private String formatType(String type) {
		String realType = type;
		if (type.startsWith("Cache.")) {
			realType = "Cache";
		} else if (pigeonCalls.contains(type)) {
			realType = "PigeonCall";
		} else if (pigeonServices.contains(type)) {
			realType = "PigeonService";
		}
		return realType;
	}

	public synchronized TopologyGraphConfig getConfig() {
		ensureInitialized();

		return config;
	}

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	@PostConstruct
	public void initialize() {
		if (fileName != null) {
			try {
				String content = FileUtils.readFileToString(new File(fileName), StandardCharsets.UTF_8);
				config = DefaultSaxParser.parse(content);
			} catch (Exception e) {
				LOGGER.error("Unable to initialize topology graph config from file, fileName={}.", fileName, e);
				Cat.logError(e);
			}
		} else {
			try {
				ConfigDO configDO = configRepository.findByName(CONFIG_NAME);
				String content = configDO.getContent();

				configId = configDO.getId();
				config = DefaultSaxParser.parse(content);
			} catch (EmptyResultDataAccessException e) {
				try {
					String content = contentFetcher.getConfigContent(CONFIG_NAME);
					ConfigDO configDO = configRepository.createLocal();

					configDO.setName(CONFIG_NAME);
					configDO.setContent(content);
					configRepository.insert(configDO);

					configId = configDO.getId();
					config = DefaultSaxParser.parse(content);
				} catch (Exception ex) {
					LOGGER.error("Unable to create default topology graph config, configName={}.", CONFIG_NAME, ex);
					Cat.logError(ex);
				}
			} catch (Exception e) {
				LOGGER.error("Unable to initialize topology graph config, configName={}.", CONFIG_NAME, e);
				Cat.logError(e);
			}
			if (config == null) {
				config = new TopologyGraphConfig();
			}
		}
	}

	public boolean insertDomainConfig(String type, DomainConfig domainConfig) {
		ensureInitialized();

		config.findOrCreateNodeConfig(type).addDomainConfig(domainConfig);
		return storeConfig();
	}

	public boolean insertDomainDefaultConfig(String type, DomainConfig domainConfig) {
		ensureInitialized();

		NodeConfig node = config.findOrCreateNodeConfig(type);

		node.setDefaultMinCountThreshold(domainConfig.getMinCountThreshold());
		node.setDefaultErrorResponseTime(domainConfig.getErrorResponseTime());
		node.setDefaultErrorThreshold(domainConfig.getErrorThreshold());
		node.setDefaultWarningResponseTime(domainConfig.getWarningResponseTime());
		node.setDefaultWarningThreshold(domainConfig.getWarningThreshold());
		return storeConfig();
	}

	public boolean insertEdgeConfig(EdgeConfig edgeConfig) {
		ensureInitialized();

		edgeConfig.setKey(edgeConfig.getType() + ":" + edgeConfig.getFrom() + ":" + edgeConfig.getTo());
		config.addEdgeConfig(edgeConfig);
		return storeConfig();
	}

	public EdgeConfig queryEdgeConfig(String type, String from, String to) {
		ensureInitialized();

		EdgeConfig edgeConfig = config.findEdgeConfig(type + ":" + from + ":" + to);

		if (edgeConfig == null) {
			DomainConfig domainConfig = null;
			if ("PigeonCall".equalsIgnoreCase(type)) {
				domainConfig = queryNodeConfig("PigeonService", to);
			} else if ("PigeonServer".equalsIgnoreCase(type)) {
				domainConfig = queryNodeConfig("PigeonService", from);
			} else {
				domainConfig = queryNodeConfig(type, to);
			}
			if (domainConfig != null) {
				edgeConfig = convertNodeConfig(domainConfig);
			}
		}
		return edgeConfig;
	}

	public DomainConfig queryNodeConfig(String type, String domain) {
		ensureInitialized();

		NodeConfig typesConfig = config.findNodeConfig(type);

		if (typesConfig != null) {
			DomainConfig config = typesConfig.findDomainConfig(domain);
			if (config == null) {
				config = new DomainConfig();

				config.setId(domain);
				config.setMinCountThreshold(typesConfig.getDefaultMinCountThreshold());
				config.setErrorResponseTime(typesConfig.getDefaultErrorResponseTime());
				config.setErrorThreshold(typesConfig.getDefaultErrorThreshold());
				config.setWarningResponseTime(typesConfig.getDefaultWarningResponseTime());
				config.setWarningThreshold(typesConfig.getDefaultWarningThreshold());
			}
			return config;
		}
		return null;
	}

	public void setFileName(String file) {
		fileName = file;
	}

	private void ensureInitialized() {
		if (config == null) {
			synchronized (this) {
				if (config == null) {
					initialize();
				}
			}
		}
	}

	private boolean storeConfig() {
		ensureInitialized();

		if (fileName != null) {
			try {
				FileUtils.writeStringToFile(new File(fileName), config.toString(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				LOGGER.error("Unable to store topology graph config to file, fileName={}.", fileName, e);
				Cat.logError(e);
				return false;
			}
		} else {
			try {
				ConfigDO configDO = configRepository.createLocal();
				configDO.setId(configId);
				configDO.setName(CONFIG_NAME);
				configDO.setContent(config.toString());
				configRepository.updateByPK(configDO);
			} catch (Exception e) {
				LOGGER.error("Unable to store topology graph config, configName={}, configId={}.", CONFIG_NAME,
				      configId, e);
				Cat.logError(e);
				return false;
			}
		}

		return true;
	}
}
