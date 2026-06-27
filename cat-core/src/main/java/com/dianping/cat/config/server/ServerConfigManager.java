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
package com.dianping.cat.config.server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.google.common.base.Splitter;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HarfsConfig;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;
import com.dianping.cat.configuration.server.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.support.Threads;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Component
public class ServerConfigManager {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(ServerConfigManager.class);

	public static final String DUMP_DIR = "dump";

	public final static String REMOTE_SERVERS = "remote-servers";

	public final static String LOCAL_MODE = "local-mode";

	public final static String JOB_MACHINE = "job-machine";

	public final static String SEND_MACHINE = "send-machine";

	public final static String ALARM_MACHINE = "alarm-machine";

	public final static String HDFS_ENABLED = "hdfs-enabled";

	public final static String ROUTER_ADJUST_ENABLED = "router-adjust-enabled";

	public static final String DEFAULT = "default";

	private static final String CONFIG_NAME = "server-config";

	private static final long DEFAULT_HDFS_FILE_MAX_SIZE = 128 * 1024 * 1024L; // 128M

	public ExecutorService threadPool;

	@Resource(name = "configRepository")
	protected ConfigRepository configRepository;

	@Resource(name = "contentFetcher")
	protected ContentFetcher contentFetcher;

	private long configId;

	private long modifyTime;

	private volatile ServerConfig serverConfig;

	private volatile Server currentServer;

	private Set<String> forcedStatisticTypePrefixes = new HashSet<>();

	private volatile boolean initialized;

	private volatile boolean initializing;

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	public ServerConfig getConfig() {
		initialize();

		return serverConfig;
	}

	public String getConsoleDefaultDomain() {
		return Constants.CAT;
	}

	public List<Pair<String, Integer>> getConsoleEndpoints() {
		String remoteServers = getProperty(REMOTE_SERVERS, "");
		List<String> endpoints = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(remoteServers);
		List<Pair<String, Integer>> pairs = new ArrayList<Pair<String, Integer>>(endpoints.size());

		for (String endpoint : endpoints) {
			int pos = endpoint.indexOf(':');
			String host = (pos > 0 ? endpoint.substring(0, pos) : endpoint);
			int port = (pos > 0 ? Integer.parseInt(endpoint.substring(pos + 1)) : 2281);

			pairs.add(Pair.of(host, port));
		}

		return pairs;
	}

	public String getConsoleRemoteServers() {
		String remoteServers = getProperty(REMOTE_SERVERS, "");

		if (remoteServers != null && remoteServers.length() > 0) {
			return remoteServers;
		} else {
			return "";
		}
	}

	public boolean getEnableOfRealtimeAnalyzer(String name) {
		return Boolean.parseBoolean(getProperty(name + "-analyzer-enable", "true"));
	}

	public String getHarfsBaseDir(String id) {
		if (currentServer != null) {
			HarfsConfig harfsConfig = currentServer.getStorage().findHarfs(id);

			if (harfsConfig != null) {
				String baseDir = harfsConfig.getBaseDir();

				if (baseDir != null && baseDir.trim().length() > 0) {
					return baseDir;
				}
			}
		}
		return null;
	}

	public long getHarfsFileMaxSize(String id) {
		if (currentServer != null) {
			HarfsConfig hdfsConfig = currentServer.getStorage().findHarfs(id);

			return toLong(hdfsConfig == null ? null : hdfsConfig.getMaxSize(), DEFAULT_HDFS_FILE_MAX_SIZE);
		} else {
			return DEFAULT_HDFS_FILE_MAX_SIZE;
		}
	}

	public String getHarfsServerUri(String id) {
		if (currentServer != null) {
			HarfsConfig hdfsConfig = currentServer.getStorage().findHarfs(id);

			if (hdfsConfig != null) {
				String serverUri = hdfsConfig.getServerUri();

				if (serverUri != null && serverUri.trim().length() > 0) {
					return serverUri;
				}
			}
		}

		return null;
	}

	public String getHdfsBaseDir(String id) {
		if (currentServer != null) {
			HdfsConfig hdfsConfig = currentServer.getStorage().findHdfs(id);

			if (hdfsConfig != null) {
				String baseDir = hdfsConfig.getBaseDir();

				if (baseDir != null && baseDir.trim().length() > 0) {
					return baseDir;
				}
			}
		}
		return null;
	}

	public String getHdfsLocalBaseDir(String id) {
		if (currentServer != null) {
			StorageConfig storage = currentServer.getStorage();

			return new File(storage.getLocalBaseDir(), id).getPath();
		} else if (id == null) {
			return "target/bucket";
		} else {
			return "target/bucket/" + id;
		}
	}

	public int getHdfsMaxStorageTime() {
		if (currentServer != null) {
			StorageConfig storage = currentServer.getStorage();

			return storage.getMaxHdfsStorageTime();
		} else {
			return 15;
		}
	}

	public Map<String, String> getHdfsProperties() {
		if (currentServer != null) {
			Map<String, String> properties = new HashMap<String, String>();

			for (Property p : currentServer.getStorage().getProperties().values()) {
				properties.put(p.getName(), p.getValue());
			}

			return properties;
		} else {
			return Collections.emptyMap();
		}
	}

	public String getHdfsServerUri(String id) {
		if (currentServer != null) {
			HdfsConfig hdfsConfig = currentServer.getStorage().findHdfs(id);

			if (hdfsConfig != null) {
				String serverUri = hdfsConfig.getServerUri();

				if (serverUri != null && serverUri.trim().length() > 0) {
					return serverUri;
				}
			}
		}

		return null;
	}

	public int getHdfsUploadThreadCount() {
		if (currentServer != null) {
			StorageConfig storage = currentServer.getStorage();

			return storage.getUploadThread();
		} else {
			return 5;
		}
	}

	public int getHdfsUploadThreadsCount() {
		if (currentServer != null) {
			StorageConfig storage = currentServer.getStorage();

			return storage.getUploadThread();
		} else {
			return 5;
		}
	}

	public int getLocalReportStroageTime() {
		if (currentServer != null) {
			StorageConfig storage = currentServer.getStorage();

			return storage.getLocalReportStorageTime();
		} else {
			return 7;
		}
	}

	public int getLogViewStroageTime() {
		if (currentServer != null) {
			StorageConfig storage = currentServer.getStorage();

			return storage.getLocalLogivewStorageTime();
		} else {
			return 30;
		}
	}

	public Map<String, Domain> getLongConfigDomains() {
		if (currentServer != null) {
			LongConfig longConfig = currentServer.getConsumer().getLongConfig();

			if (longConfig != null) {
				return longConfig.getDomains();
			}
		}
		return Collections.emptyMap();
	}

	public int getLongUrlDefaultThreshold() {
		if (currentServer != null) {
			LongConfig longConfig = currentServer.getConsumer().getLongConfig();

			if (longConfig != null) {
				return longConfig.getDefaultSqlThreshold();
			}
		}
		return 1000; // 1 second
	}

	public int getMessageDumpThreads() {
		return Integer.parseInt(getProperty("message-dumper-thread", "3"));
	}

	public int getMessageProcessorThreads() {
		return Integer.parseInt(getProperty("message-processor-thread", "8"));
	}

	public ExecutorService getModelServiceExecutorService() {
		return threadPool;
	}

	public int getModelServiceThreads() {
		return Integer.parseInt(getProperty("model-service-thread", "20"));
	}

	public String getProperty(String name, String defaultValue) {
		if (!initialized && !initializing) {
			initialize();
		}

		if (currentServer != null) {
			Property property = currentServer.findProperty(name);

			if (property != null) {
				return property.getValue();
			}
		}
		return defaultValue;
	}

	public int getMaxTypeThreshold() {
		return Integer.parseInt(getProperty("max-type-threshold", "100"));
	}

	public int getTypeNameLengthLimit() {
		return Integer.parseInt(getProperty("type-name-length-limit", "256"));
	}

	public int getTpValueExpireMinute() {
		return Integer.parseInt(getProperty("tp-value-expire-minute", "1"));
	}

	public ServerConfig getServerConfig() {
		initialize();

		return serverConfig;
	}

	public int getBlockDumpThread() {
		return Integer.parseInt(getProperty("block-dump-thread", "5"));
	}

	public String getStorageCompressType() {
		return getProperty("storage-compress-type", "snappy");
	}

	public int getStorageDeflateLevel() {
		return Integer.parseInt(getProperty("storage-deflate-level", "5"));
	}

	public int getStorageMaxBlockSize() {
		return Integer.parseInt(getProperty("storage-max-block-size", "131072"));
	}

	public boolean getStroargeNioEnable() {
		return Boolean.parseBoolean(getProperty("storage-nio-enable", "true"));
	}

	public int getThreadsOfRealtimeAnalyzer(String name) {
		return Integer.parseInt(getProperty(name + "-analyzer-threads", "2"));
	}

	@PostConstruct
	public synchronized void initialize() {
		if (initialized) {
			return;
		}
		initializing = true;

		try {
			try {
				Config dbConfig = configRepository.findByName(CONFIG_NAME);
				String content = dbConfig.getContent();

				configId = dbConfig.getId();
				modifyTime = dbConfig.getModifyDate().getTime();
				serverConfig = DefaultSaxParser.parse(content);
				SLF4J_LOGGER.info("Loaded server config from repository, configId={}, modifyTime={}.", configId,
						modifyTime);
			} catch (EmptyResultDataAccessException e) {
				SLF4J_LOGGER.warn("Server config is missing in repository, loading default content from fetcher.", e);

				try {
					String content = contentFetcher.getConfigContent(CONFIG_NAME);
					Config dbConfig = configRepository.createLocal();

					dbConfig.setName(CONFIG_NAME);
					dbConfig.setContent(content);
					configRepository.insert(dbConfig);
					configId = dbConfig.getId();
					serverConfig = DefaultSaxParser.parse(content);
					SLF4J_LOGGER.info("Initialized server config from default content, configId={}.", configId);
				} catch (Exception ex) {
					SLF4J_LOGGER.error("Unable to initialize server config from default content.", ex);
					Cat.logError(ex);
				}
			} catch (Exception e) {
				SLF4J_LOGGER.error("Unable to load server config from repository.", e);
				Cat.logError(e);
			}

			if (serverConfig == null) {
				try {
					File localServerFile = new File(Cat.getCatHome(), "server.xml");

					SLF4J_LOGGER.info("init cat server with cat server xml {}", localServerFile);
					initialize(localServerFile);
				} catch (Exception e) {
					SLF4J_LOGGER.error("Unable to initialize server config from local server.xml.", e);
					Cat.logError(e);
				}
			}

			if (serverConfig == null) {
				serverConfig = new ServerConfig();
				SLF4J_LOGGER.warn("Server config is empty after initialization, using a new empty config.");
			}

			serverConfig.accept(new ServerConfigValidator());

			try {
				refreshServer();
			} catch (Exception e) {
				SLF4J_LOGGER.error("Unable to refresh local server config view.", e);
				Cat.logError(e);
			}

			prepare();

			TimerSyncTask.getInstance().register(new SyncHandler() {

				@Override
				public String getName() {
					return CONFIG_NAME;
				}

				@Override
				public void handle() throws Exception {
					refreshConfig();
				}
			});
			initialized = true;
		} finally {
			initializing = false;
		}
	}

	public void initialize(File configFile) throws Exception {
		initializing = true;

		try {
			if (configFile != null && configFile.canRead()) {
				SLF4J_LOGGER.info("Loading configuration file({}) ...", configFile.getCanonicalPath());

				String xml = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
				serverConfig = DefaultSaxParser.parse(xml);
				SLF4J_LOGGER.info("Loaded server config from local file, path={}.", configFile.getCanonicalPath());
			} else {
				if (configFile != null) {
					SLF4J_LOGGER.warn("Server config local file is not readable, path={}.", configFile.getCanonicalPath());
				}

				serverConfig = new ServerConfig();
			}
			serverConfig.accept(new ServerConfigValidator());
			refreshServer();
			prepare();
			initialized = true;
		} finally {
			initializing = false;
		}
	}

	public boolean insert(String xml) {
		try {
			serverConfig = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			SLF4J_LOGGER.error("Unable to parse server config xml for insert. xmlLength={}.",
					xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public boolean isUseNewStorage() {
		return Boolean.parseBoolean(getProperty("use-new-storage", "true"));
	}

	public boolean isAlertMachine() {
		return Boolean.parseBoolean(getProperty(ALARM_MACHINE, "false"));
	}

	public boolean isHarMode() {
		if (currentServer != null) {
			return currentServer.getStorage().isHarMode();
		} else {
			return false;
		}
	}

	public boolean isHdfsOn() {
		return Boolean.parseBoolean(getProperty(HDFS_ENABLED, "false"));
	}

	public boolean isJobMachine() {
		return Boolean.parseBoolean(getProperty(JOB_MACHINE, "false"));
	}

	public boolean isLocalMode() {
		return Boolean.parseBoolean(getProperty(LOCAL_MODE, "false"));
	}

	public boolean isRouterAdjustEnabled() {
		return Boolean.parseBoolean(getProperty(ROUTER_ADJUST_ENABLED, "false"));
	}

	public boolean isRemoteServersFixed() {
		return Boolean.parseBoolean(getProperty("remote-servers-fixed", "false"));
	}

	public boolean isRpcClient(String type) {
		return "PigeonCall".equals(type) || "Call".equals(type);
	}

	public boolean isRpcServer(String type) {
		return "PigeonService".equals(type) || "Service".equals(type);
	}

	public boolean isSendMachine() {
		return Boolean.parseBoolean(getProperty(SEND_MACHINE, "false"));
	}

	private void prepare() {
		if (isLocalMode()) {
			SLF4J_LOGGER.warn("CAT server is running in LOCAL mode! No HDFS or MySQL will be accessed!");
		}
		SLF4J_LOGGER.info("CAT server is running with hdfs,{}", isHdfsOn());
		SLF4J_LOGGER.info("CAT server is running with alert,{}", isAlertMachine());
		SLF4J_LOGGER.info("CAT server is running with job,{}", isJobMachine());

		if (currentServer != null) {
			SLF4J_LOGGER.info("{}", currentServer);

			if (isLocalMode()) {
				threadPool = Threads.forPool().getFixedThreadPool("Cat-ModelService", 5);
			} else {
				threadPool = Threads.forPool().getFixedThreadPool("Cat-ModelService", getModelServiceThreads());
			}
		}
	}

	private void refreshConfig() throws Exception {
		Config dbConfig = configRepository.findByName(CONFIG_NAME);
		long remoteModifyTime = dbConfig.getModifyDate().getTime();

		synchronized (this) {
			if (remoteModifyTime > modifyTime) {
				ServerConfig latestServerConfig = DefaultSaxParser.parse(dbConfig.getContent());
				latestServerConfig.accept(new ServerConfigValidator());

				serverConfig = latestServerConfig;
				modifyTime = remoteModifyTime;

				refreshServer();
				SLF4J_LOGGER.info("Refreshed server config, configId={}, modifyTime={}.", configId, modifyTime);
			}
		}
	}

	private void refreshServer() throws SAXException, IOException {
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		ServerConfig runtimeConfig = DefaultSaxParser.parse(serverConfig.toString());
		Server defaultServer = runtimeConfig.findServer(DEFAULT).setId(ip);
		Server matchedServer = runtimeConfig.findServer(ip);

		if (matchedServer != null && defaultServer != null) {
			ServerConfigVisitor visitor = new ServerConfigVisitor(matchedServer);

			visitor.visitServer(defaultServer);
		}
		currentServer = defaultServer;

		String forcedStatisticTypePrefixStr = getProperty("forced-statistic-type-prefixes", "Cellar.,Squirrel.");
		List<String> configuredPrefixes = Splitter.on(',').omitEmptyStrings()
				.splitToList(forcedStatisticTypePrefixStr);
		forcedStatisticTypePrefixes = new HashSet<>(configuredPrefixes);
		SLF4J_LOGGER.info("Refreshed server runtime config, localIp={}, forcedStatisticTypePrefixes={}.", ip,
				forcedStatisticTypePrefixes);
	}

	public boolean storeConfig() {
		try {
			Config dbConfig = configRepository.createLocal();

			dbConfig.setId(configId);
			dbConfig.setKeyId(configId);
			dbConfig.setName(CONFIG_NAME);
			dbConfig.setContent(serverConfig.toString());
			configRepository.updateByPK(dbConfig);
			refreshServer();
			SLF4J_LOGGER.info("Stored server config, configId={}.", configId);
		} catch (Exception e) {
			SLF4J_LOGGER.error("Unable to store server config, configId={}.", configId, e);
			Cat.logError(e);
			return false;
		}
		return true;
	}

	private long toLong(String str, long defaultValue) {
		long value = 0;
		int len = str == null ? 0 : str.length();

		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);

			if (Character.isDigit(ch)) {
				value = value * 10L + (ch - '0');
			} else if (ch == 'm' || ch == 'M') {
				value *= 1024 * 1024L;
			} else if (ch == 'k' || ch == 'K') {
				value *= 1024L;
			}
		}

		if (value > 0) {
			return value;
		} else {
			return defaultValue;
		}
	}

	public boolean validateIp(String str) {
		Pattern pattern = Pattern.compile(
		      "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

	public Set<String> getForcedStatisticTypePrefixes() {
		return forcedStatisticTypePrefixes;
	}

}
