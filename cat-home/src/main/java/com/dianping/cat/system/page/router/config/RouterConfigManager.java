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
package com.dianping.cat.system.page.router.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.mybatis.DailyReportContentRepository;
import com.dianping.cat.mybatis.DailyReportRepository;
import com.dianping.cat.mybatis.data.DailyReportContentDO;
import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.router.entity.*;
import com.dianping.cat.home.router.transform.DefaultNativeParser;
import com.dianping.cat.home.router.transform.DefaultSaxParser;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@Component
public class RouterConfigManager {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(RouterConfigManager.class);

	public static final String DEFAULT = "default";

	private static final String CONFIG_NAME = "routerConfig";

	@Resource
	private ConfigRepository configRepository;

	@Resource
	private ContentFetcher contentFetcher;

	@Resource
	private DailyReportRepository dailyReportRepository;

	@Resource
	private DailyReportContentRepository dailyReportContentRepository;

	private long configId;

	private volatile RouterConfig routerConfig;

	private long modifyTime;

	private Map<String, List<SubnetInfo>> subNetInfos = new HashMap<String, List<SubnetInfo>>();

	private Map<String, String> ipToGroupInfo = new HashMap<String, String>();

	private Map<Long, Pair<RouterConfig, Long>> routerConfigs = new LinkedHashMap<Long, Pair<RouterConfig, Long>>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<Long, Pair<RouterConfig, Long>> eldest) {
			return size() > 100;
		}
	};

	private volatile boolean initialized;

	private void addServerList(List<Server> servers, Server server) {
		for (Server s : servers) {
			if (s.getId().equals(server.getId())) {
				return;
			}
		}
		servers.add(server);
	}

	public RouterConfig getRouterConfig() {
		ensureInitialized();
		return routerConfig;
	}

	public Map<Long, Pair<RouterConfig, Long>> getRouterConfigs() {
		ensureInitialized();
		return routerConfigs;
	}

	public void setConfigDao(ConfigRepository configDao) {
		configRepository = configDao;
	}

	public void setDailyReportContentDao(DailyReportContentRepository dailyReportContentDao) {
		dailyReportContentRepository = dailyReportContentDao;
	}

	public void setDailyReportDao(DailyReportRepository dailyReportDao) {
		dailyReportRepository = dailyReportDao;
	}

	public void setFetcher(ContentFetcher fetcher) {
		contentFetcher = fetcher;
	}

	private void ensureInitialized() {
		if (!initialized) {
			initialize();
		}
	}

	@PostConstruct
	public synchronized void initialize() {
		if (initialized) {
			return;
		}

		try {
			SLF4J_LOGGER.info("Initializing router config manager, configName={}.", CONFIG_NAME);
			Config config = configRepository.findByName(CONFIG_NAME);
			String content = config.getContent();

			configId = config.getId();
			routerConfig = DefaultSaxParser.parse(content);
			modifyTime = config.getModifyDate().getTime();
		} catch (EmptyResultDataAccessException e) {
			SLF4J_LOGGER.warn("Router config not found in repository, loading default content, configName={}.",
			      CONFIG_NAME);
			try {
				String content = contentFetcher.getConfigContent(CONFIG_NAME);
				Config config = configRepository.createLocal();
				Date now = new Date();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				config.setModifyDate(now);
				configRepository.insert(config);

				configId = config.getId();
				routerConfig = DefaultSaxParser.parse(content);
				modifyTime = now.getTime();
			} catch (Exception ex) {
				SLF4J_LOGGER.error("Unable to create default router config, configName={}.", CONFIG_NAME, ex);
				Cat.logError(ex);
			}
		} catch (Exception e) {
			SLF4J_LOGGER.error("Unable to initialize router config, configName={}.", CONFIG_NAME, e);
			Cat.logError(e);
		}
		if (routerConfig == null) {
			SLF4J_LOGGER.warn("Router config is empty after initialization, using an empty config.");
			routerConfig = new RouterConfig();
		}

		refreshNetInfo();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return CONFIG_NAME;
			}

			@Override
			public void handle() throws Exception {
				refreshConfigInfo();
				refreshReportInfo();
			}
		});
		initialized = true;
	}

	public boolean insert(String xml) {
		ensureInitialized();

		try {
			RouterConfig routerConfig = DefaultSaxParser.parse(xml);

			if (validate(routerConfig)) {
				this.routerConfig = routerConfig;
				boolean result = storeConfig();

				if (result) {
					refreshNetInfo();
				}
				SLF4J_LOGGER.info("Inserted router config, result={}, xmlLength={}.", result, xml == null ? 0 : xml.length());
				return result;
			} else {
				SLF4J_LOGGER.warn("Router config validation failed, xmlLength={}.", xml == null ? 0 : xml.length());
				return false;
			}
		} catch (Exception e) {
			SLF4J_LOGGER.error("Unable to insert router config, xmlLength={}.", xml == null ? 0 : xml.length(), e);
			Cat.logError(e);
			return false;
		}
	}

	public boolean notCustomizedDomains(String group, Domain domainConfig) {
		ensureInitialized();

		return domainConfig == null || domainConfig.findGroup(group) == null
		      || domainConfig.findGroup(group).getServers().isEmpty();
	}

	public boolean notCustomizedDomains(String group, String domain) {
		ensureInitialized();

		Domain domainConfig = routerConfig.findDomain(domain);

		return notCustomizedDomains(group, domainConfig);
	}

	public Server queryBackUpServer() {
		ensureInitialized();

		return new Server().setId(routerConfig.getBackupServer()).setPort(routerConfig.getBackupServerPort());
	}

	public Map<String, Server> queryEnableServers() {
		ensureInitialized();

		return queryEnableServers(routerConfig);
	}

	private Map<String, Server> queryEnableServers(RouterConfig routerConfig) {
		Map<String, DefaultServer> servers = routerConfig.getDefaultServers();
		Map<String, Server> results = new HashMap<String, Server>();

		for (Entry<String, DefaultServer> entry : servers.entrySet()) {
			DefaultServer server = entry.getValue();

			if (server.isEnable()) {
				Server s = new Server().setId(server.getId()).setPort(server.getPort()).setWeight(server.getWeight());
				results.put(entry.getKey(), s);
			}
		}
		return results;
	}

	private String queryGroupBySubnet(String ip) {
		for (Entry<String, List<SubnetInfo>> entry : subNetInfos.entrySet()) {
			List<SubnetInfo> subnetInfos = entry.getValue();
			String group = entry.getKey();

			for (SubnetInfo info : subnetInfos) {
				try {
					if (info.isInRange(ip)) {
						return group;
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return null;
	}

	public DefaultServer queryServerByIp(String ip) {
		ensureInitialized();

		DefaultServer server = routerConfig.getDefaultServers().get(ip);

		if (server != null) {
			return server;
		}
		return null;
	}

	public String queryServerGroupByIp(String ip) {
		ensureInitialized();

		String group = ipToGroupInfo.get(ip);

		if (group == null) {
			group = queryGroupBySubnet(ip);

			if (group == null) {
				group = DEFAULT;
			}

			ipToGroupInfo.put(ip, group);
		}
		return group;
	}

	public List<Server> queryServersByDomain(String group, String domain) {
		ensureInitialized();

		Domain domainConfig = routerConfig.findDomain(domain);
		List<Server> result = new ArrayList<Server>();
		boolean noExist = notCustomizedDomains(group, domainConfig);

		if (noExist) {
			List<Server> servers = new ArrayList<Server>();
			Map<String, Server> enables = queryEnableServers();
			ServerGroup serverGroup = routerConfig.getServerGroups().get(group);

			if (serverGroup != null) {
				for (GroupServer s : serverGroup.getGroupServers().values()) {
					servers.add(enables.get(s.getId()));
				}
			}

			if (servers.isEmpty()) {
				servers = new ArrayList<Server>(enables.values());
			}

			if (!servers.isEmpty()) {
				int length = servers.size();
				int hashCode = domain.hashCode();

				for (int i = 0; i < 2; i++) {
					int index = Math.abs((hashCode + i)) % length;

					addServerList(result, servers.get(index));
				}
			}

			addServerList(result, queryBackUpServer());
		} else {
			result.addAll(domainConfig.findGroup(group).getServers());
		}
		return result;
	}

	private void refreshConfigInfo() throws SAXException, IOException {
		Config config = configRepository.findByName(CONFIG_NAME);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > this.modifyTime) {
				String content = config.getContent();

				routerConfig = DefaultSaxParser.parse(content);
				this.modifyTime = modifyTime;
				refreshNetInfo();
				SLF4J_LOGGER.info("Refreshed router config, configName={}, modifyTime={}.", CONFIG_NAME, modifyTime);
			}
		}
	}

	private void refreshNetInfo() {
		Map<String, List<SubnetInfo>> subNetInfos = new HashMap<String, List<SubnetInfo>>();

		for (Entry<String, NetworkPolicy> netPolicy : routerConfig.getNetworkPolicies().entrySet()) {
			ArrayList<SubnetInfo> infos = new ArrayList<SubnetInfo>();

			if (!DEFAULT.equals(netPolicy.getKey())) {
				for (Entry<String, Network> network : netPolicy.getValue().getNetworks().entrySet()) {
					try {
						SubnetUtils subnetUtils = new SubnetUtils(network.getValue().getId());
						SubnetInfo netInfo = subnetUtils.getInfo();

						infos.add(netInfo);
					} catch (Exception e) {
						SLF4J_LOGGER.warn("Unable to parse router network subnet, policy={}, network={}.",
						      netPolicy.getKey(), network.getValue().getId(), e);
						Cat.logError(e);
					}
				}
				subNetInfos.put(netPolicy.getKey(), infos);
			}
		}

		this.subNetInfos = subNetInfos;
		ipToGroupInfo = new HashMap<String, String>();
	}

	private void refreshReportInfo() throws Exception {
		Date period = TimeHelper.getCurrentDay(-1);
		long time = period.getTime();

		try {
			DailyReportDO report = dailyReportRepository.findByDomainNamePeriod(Constants.CAT, RouterConfigBuilder.ID,
			      period);
			long modifyTime = report.getCreateTime().getTime();
			Pair<RouterConfig, Long> pair = routerConfigs.get(time);

			if (pair == null || modifyTime > pair.getValue()) {
				try {
					DailyReportContentDO reportContent = dailyReportContentRepository.findByPK(report.getId());
					RouterConfig routerConfig = DefaultNativeParser.parse(reportContent.getContent());

					routerConfigs.put(time, Pair.of(routerConfig, modifyTime));
					Cat.logEvent("ReloadConfig", "router");
				} catch (EmptyResultDataAccessException ignored) {
					SLF4J_LOGGER.warn("Router report content not found while refreshing report cache, reportId={}.",
					      report.getId());
				}
			}
		} catch (EmptyResultDataAccessException ignored) {
			SLF4J_LOGGER.warn("Router daily report not found while refreshing report cache, period={}.", period);
		}
	}

	public boolean shouldBlock(String ip) {
		ensureInitialized();

		String group = queryServerGroupByIp(ip);
		NetworkPolicy networkPolicy = routerConfig.findNetworkPolicy(group);

		if (networkPolicy != null) {
			return networkPolicy.isBlock();
		} else {
			return false;
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = configRepository.createLocal();

				config.setId(configId);
				config.setKeyId(configId);
				config.setName(CONFIG_NAME);
				config.setContent(routerConfig.toString());
				configRepository.updateByPK(config);
			} catch (Exception e) {
				SLF4J_LOGGER.error("Unable to store router config, configName={}, configId={}.", CONFIG_NAME,
				      configId, e);
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public boolean validate(final RouterConfig routerConfig) {
		Set<String> servers = routerConfig.getDefaultServers().keySet();

		for (ServerGroup serverGroup : routerConfig.getServerGroups().values()) {
			for (GroupServer server : serverGroup.getGroupServers().values()) {
				if (!servers.contains(server.getId())) {
					SLF4J_LOGGER.warn("Router config validation failed, groupServer has no default server, server={}.",
					      server);
					Cat.logError(new RuntimeException("Error router config in group server, has no server ip: " + server));
					return false;
				}
			}
		}

		if (queryEnableServers(routerConfig).isEmpty()) {
			SLF4J_LOGGER.warn("Router config validation failed, enabled servers are empty.");
			Cat.logError(new RuntimeException("Error router config, enable servers not exist."));
			return false;
		}

		// if (routerConfig.findNetworkPolicy(DEFAULT) == null) {
		// Cat.logError(new RuntimeException("Error router config, default network policy doesn't exist."));
		// return false;
		// }

		return true;
	}
}
