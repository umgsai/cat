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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import com.dianping.cat.core.dal.jdbc.DalNotFoundException;
import com.dianping.cat.support.Threads;
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
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

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

	public ExecutorService m_threadPool;

	protected ConfigRepository m_configDao;

	protected ContentFetcher m_fetcher;

	private int m_configId;

	private long m_modifyTime;

	private volatile ServerConfig m_config;

	private volatile Server m_server;

	private Set<String> m_forcedStatisticTypePrefixes = new HashSet<>();

	private volatile boolean m_initialized;

	private volatile boolean m_initializing;

	public void setConfigDao(ConfigRepository configDao) {
		m_configDao = configDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		m_fetcher = fetcher;
	}

	public ServerConfig getConfig() {
		initialize();

		return m_config;
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
		if (m_server != null) {
			HarfsConfig harfsConfig = m_server.getStorage().findHarfs(id);

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
		if (m_server != null) {
			HarfsConfig hdfsConfig = m_server.getStorage().findHarfs(id);

			return toLong(hdfsConfig == null ? null : hdfsConfig.getMaxSize(), DEFAULT_HDFS_FILE_MAX_SIZE);
		} else {
			return DEFAULT_HDFS_FILE_MAX_SIZE;
		}
	}

	public String getHarfsServerUri(String id) {
		if (m_server != null) {
			HarfsConfig hdfsConfig = m_server.getStorage().findHarfs(id);

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
		if (m_server != null) {
			HdfsConfig hdfsConfig = m_server.getStorage().findHdfs(id);

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
		if (m_server != null) {
			StorageConfig storage = m_server.getStorage();

			return new File(storage.getLocalBaseDir(), id).getPath();
		} else if (id == null) {
			return "target/bucket";
		} else {
			return "target/bucket/" + id;
		}
	}

	public int getHdfsMaxStorageTime() {
		if (m_server != null) {
			StorageConfig storage = m_server.getStorage();

			return storage.getMaxHdfsStorageTime();
		} else {
			return 15;
		}
	}

	public Map<String, String> getHdfsProperties() {
		if (m_server != null) {
			Map<String, String> properties = new HashMap<String, String>();

			for (Property p : m_server.getStorage().getProperties().values()) {
				properties.put(p.getName(), p.getValue());
			}

			return properties;
		} else {
			return Collections.emptyMap();
		}
	}

	public String getHdfsServerUri(String id) {
		if (m_server != null) {
			HdfsConfig hdfsConfig = m_server.getStorage().findHdfs(id);

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
		if (m_server != null) {
			StorageConfig storage = m_server.getStorage();

			return storage.getUploadThread();
		} else {
			return 5;
		}
	}

	public int getHdfsUploadThreadsCount() {
		if (m_server != null) {
			StorageConfig storage = m_server.getStorage();

			return storage.getUploadThread();
		} else {
			return 5;
		}
	}

	public int getLocalReportStroageTime() {
		if (m_server != null) {
			StorageConfig storage = m_server.getStorage();

			return storage.getLocalReportStorageTime();
		} else {
			return 7;
		}
	}

	public int getLogViewStroageTime() {
		if (m_server != null) {
			StorageConfig storage = m_server.getStorage();

			return storage.getLocalLogivewStorageTime();
		} else {
			return 30;
		}
	}

	public Map<String, Domain> getLongConfigDomains() {
		if (m_server != null) {
			LongConfig longConfig = m_server.getConsumer().getLongConfig();

			if (longConfig != null) {
				return longConfig.getDomains();
			}
		}
		return Collections.emptyMap();
	}

	public int getLongUrlDefaultThreshold() {
		if (m_server != null) {
			LongConfig longConfig = m_server.getConsumer().getLongConfig();

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
		return m_threadPool;
	}

	public int getModelServiceThreads() {
		return Integer.parseInt(getProperty("model-service-thread", "20"));
	}

	public String getProperty(String name, String defaultValue) {
		if (!m_initialized && !m_initializing) {
			initialize();
		}

		if (m_server != null) {
			Property property = m_server.findProperty(name);

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

		return m_config;
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

	public synchronized void initialize() {
		if (m_initialized) {
			return;
		}
		m_initializing = true;

		try {
			try {
				Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
				String content = config.getContent();

				m_configId = config.getId();
				m_modifyTime = config.getModifyDate().getTime();
				m_config = DefaultSaxParser.parse(content);
				SLF4J_LOGGER.info("Loaded server config from repository, configId={}, modifyTime={}.", m_configId,
						m_modifyTime);
			} catch (DalNotFoundException e) {
				SLF4J_LOGGER.warn("Server config is missing in repository, loading default content from fetcher.", e);

				try {
					String content = m_fetcher.getConfigContent(CONFIG_NAME);
					Config config = m_configDao.createLocal();

					config.setName(CONFIG_NAME);
					config.setContent(content);
					m_configDao.insert(config);
					m_configId = config.getId();
					m_config = DefaultSaxParser.parse(content);
					SLF4J_LOGGER.info("Initialized server config from default content, configId={}.", m_configId);
				} catch (Exception ex) {
					SLF4J_LOGGER.error("Unable to initialize server config from default content.", ex);
					Cat.logError(ex);
				}
			} catch (Exception e) {
				SLF4J_LOGGER.error("Unable to load server config from repository.", e);
				Cat.logError(e);
			}

			if (m_config == null) {
				try {
					File localServerFile = new File(Cat.getCatHome(), "server.xml");

					SLF4J_LOGGER.info("init cat server with cat server xml {}", localServerFile);
					initialize(localServerFile);
				} catch (Exception e) {
					SLF4J_LOGGER.error("Unable to initialize server config from local server.xml.", e);
					Cat.logError(e);
				}
			}

			if (m_config == null) {
				m_config = new ServerConfig();
				SLF4J_LOGGER.warn("Server config is empty after initialization, using a new empty config.");
			}

			m_config.accept(new ServerConfigValidator());

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
			m_initialized = true;
		} finally {
			m_initializing = false;
		}
	}

	public void initialize(File configFile) throws Exception {
		m_initializing = true;

		try {
			if (configFile != null && configFile.canRead()) {
				SLF4J_LOGGER.info("Loading configuration file({}) ...", configFile.getCanonicalPath());

				String xml = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
				m_config = DefaultSaxParser.parse(xml);
				SLF4J_LOGGER.info("Loaded server config from local file, path={}.", configFile.getCanonicalPath());
			} else {
				if (configFile != null) {
					SLF4J_LOGGER.warn("Server config local file is not readable, path={}.", configFile.getCanonicalPath());
				}

				m_config = new ServerConfig();
			}
			m_config.accept(new ServerConfigValidator());
			refreshServer();
			prepare();
			m_initialized = true;
		} finally {
			m_initializing = false;
		}
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);

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
		if (m_server != null) {
			return m_server.getStorage().isHarMode();
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

		if (m_server != null) {
			SLF4J_LOGGER.info("{}", m_server);

			if (isLocalMode()) {
				m_threadPool = Threads.forPool().getFixedThreadPool("Cat-ModelService", 5);
			} else {
				m_threadPool = Threads.forPool().getFixedThreadPool("Cat-ModelService", getModelServiceThreads());
			}
		}
	}

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				ServerConfig serverConfig = DefaultSaxParser.parse(config.getContent());
				serverConfig.accept(new ServerConfigValidator());

				m_config = serverConfig;
				m_modifyTime = modifyTime;

				refreshServer();
				SLF4J_LOGGER.info("Refreshed server config, configId={}, modifyTime={}.", m_configId, m_modifyTime);
			}
		}
	}

	private void refreshServer() throws SAXException, IOException {
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		ServerConfig config = DefaultSaxParser.parse(m_config.toString());
		Server defaultServer = config.findServer(DEFAULT).setId(ip);
		Server server = config.findServer(ip);

		if (server != null && defaultServer != null) {
			ServerConfigVisitor visitor = new ServerConfigVisitor(server);

			visitor.visitServer(defaultServer);
		}
		m_server = defaultServer;

		String forcedStatisticTypePrefixStr = getProperty("forced-statistic-type-prefixes", "Cellar.,Squirrel.");
		List<String> forcedStatisticTypePrefixes = Splitter.on(',').omitEmptyStrings()
				.splitToList(forcedStatisticTypePrefixStr);
		m_forcedStatisticTypePrefixes = new HashSet<>(forcedStatisticTypePrefixes);
		SLF4J_LOGGER.info("Refreshed server runtime config, localIp={}, forcedStatisticTypePrefixes={}.", ip,
				m_forcedStatisticTypePrefixes);
	}

	public boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_config.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			refreshServer();
			SLF4J_LOGGER.info("Stored server config, configId={}.", m_configId);
		} catch (Exception e) {
			SLF4J_LOGGER.error("Unable to store server config, configId={}.", m_configId, e);
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
		return m_forcedStatisticTypePrefixes;
	}

}
