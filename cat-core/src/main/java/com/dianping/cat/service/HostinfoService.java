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
package com.dianping.cat.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import com.dianping.cat.support.Threads;
import com.dianping.cat.support.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.mybatis.HostInfoRepository;
import com.dianping.cat.helper.TimeHelper;

public class HostinfoService {
	private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(HostinfoService.class);

	public static final String UNKNOWN_PROJECT = "UnknownProject";

	private HostInfoRepository m_hostinfoDao;

	private ServerConfigManager m_manager;

	private Map<String, String> m_ipDomains = new ConcurrentHashMap<String, String>();

	private Map<String, Hostinfo> m_hostinfos = new ConcurrentHashMap<String, Hostinfo>();

	private volatile boolean m_initialized;

	public Hostinfo createLocal() {
		return m_hostinfoDao.createLocal();
	}

	public List<Hostinfo> findAll() {
		ensureInitialized();

		return new ArrayList<Hostinfo>(m_hostinfos.values());
	}

	public Hostinfo findByIp(String ip) {
		ensureInitialized();

		Hostinfo hostinfo = m_hostinfos.get(ip);

		if (hostinfo != null) {
			return hostinfo;
		} else {
			try {
				hostinfo = m_hostinfoDao.findByIp(ip);

				if (hostinfo != null) {
					m_hostinfos.put(ip, hostinfo);
					return hostinfo;
				} else {
					return null;
				}
			} catch (EmptyResultDataAccessException e) {
				SLF4J_LOGGER.warn("Hostinfo is missing by ip={}.", ip, e);
			} catch (Exception e) {
				SLF4J_LOGGER.error("Unable to find hostinfo by ip={}.", ip, e);
				Cat.logError(e);
			}
			return null;
		}
	}

	private void ensureInitialized() {
		if (!m_initialized) {
			initialize();
		}
	}

	public synchronized void initialize() {
		if (m_initialized) {
			return;
		}

		if (m_hostinfoDao == null) {
			throw new IllegalStateException("HostinfoRepository is required for HostinfoService.");
		}
		if (m_manager == null) {
			throw new IllegalStateException("ServerConfigManager is required for HostinfoService.");
		}

		Threads.forGroup("Cat").start(new RefreshHost());
		m_initialized = true;
		SLF4J_LOGGER.info("HostinfoService started refresh task.");
	}

	private boolean insert(Hostinfo hostinfo) {
		int result = m_hostinfoDao.insert(hostinfo);

		if (result == 1) {
			m_hostinfos.put(hostinfo.getIp(), hostinfo);
			return true;
		} else {
			return false;
		}
	}

	public boolean insert(String domain, String ip) {
		ensureInitialized();

		try {
			Hostinfo info = createLocal();

			info.setDomain(domain);
			info.setIp(ip);
			boolean inserted = insert(info);

			if (inserted) {
				SLF4J_LOGGER.info("Inserted hostinfo, domain={}, ip={}.", domain, ip);
				return true;
			}
			SLF4J_LOGGER.warn("Hostinfo insert affected no rows, domain={}, ip={}.", domain, ip);
		} catch (RuntimeException e) {
			SLF4J_LOGGER.error("Unable to insert hostinfo, domain={}, ip={}.", domain, ip, e);
			Cat.logError(e);
		}
		return false;
	}

	public String queryDomainByIp(String ip) {
		ensureInitialized();

		String project = m_ipDomains.get(ip);

		if (project == null) {
			return UNKNOWN_PROJECT;
		}
		return project;
	}

	public String queryHostnameByIp(String ip) {
		ensureInitialized();

		try {
			if (validateIp(ip)) {
				Hostinfo info = m_hostinfos.get(ip);
				String hostname = null;

				if (info != null) {
					hostname = info.getHostname();

					if (isNotEmpty(hostname)) {
						return hostname;
					}
				}
				info = findByIp(ip);

				if (info != null) {
					m_hostinfos.put(ip, info);
					hostname = info.getHostname();
				}
				return hostname;
			} else {
				return null;
			}
		} catch (Exception e) {
			SLF4J_LOGGER.error("Unable to query hostname by ip={}.", ip, e);
			Cat.logError(e);
		}

		return null;
	}

	public List<String> queryIpsByDomain(String domain) {
		ensureInitialized();

		List<String> ips = new ArrayList<String>();
		if (domain == null) {
			return ips;
		}

		for (Hostinfo hostinfo : m_hostinfos.values()) {
			if (domain.equals(hostinfo.getDomain())) {
				String ip = hostinfo.getIp();

				if (ip != null && ip.length() > 0) {
					ips.add(ip);
				}
			}
		}

		return ips;
	}

	protected void refresh() {
		try {
			List<Hostinfo> hostinfos = m_hostinfoDao.findAllIp();
			Map<String, Hostinfo> tmpHostInfos = new ConcurrentHashMap<String, Hostinfo>();
			Map<String, String> tmpIpDomains = new ConcurrentHashMap<String, String>();

			for (Hostinfo hostinfo : hostinfos) {
				tmpHostInfos.put(hostinfo.getIp(), hostinfo);
				tmpIpDomains.put(hostinfo.getIp(), hostinfo.getDomain());
			}
			m_hostinfos = tmpHostInfos;
			m_ipDomains = tmpIpDomains;
			SLF4J_LOGGER.info("Refreshed hostinfo cache, hostCount={}.", hostinfos.size());
		} catch (RuntimeException e) {
			SLF4J_LOGGER.error("Unable to refresh hostinfo cache.", e);
			Cat.logError("initialize HostService error", e);
		}
	}

	public boolean update(long id, String domain, String ip) {
		ensureInitialized();

		Hostinfo info = createLocal();

		info.setId(id);
		info.setDomain(domain);
		info.setIp(ip);
		info.setLastModifiedDate(new Date());
		updateHostinfo(info);
		m_hostinfos.put(ip, info);
		return true;
	}

	public boolean updateHostinfo(Hostinfo hostinfo) {
		ensureInitialized();

		m_hostinfos.put(hostinfo.getIp(), hostinfo);

		try {
			m_hostinfoDao.updateByPK(hostinfo);
			SLF4J_LOGGER.info("Updated hostinfo, id={}, domain={}, ip={}.", hostinfo.getId(), hostinfo.getDomain(),
					hostinfo.getIp());
			return true;
		} catch (RuntimeException e) {
			SLF4J_LOGGER.error("Unable to update hostinfo, id={}, domain={}, ip={}.", hostinfo.getId(),
					hostinfo.getDomain(), hostinfo.getIp(), e);
			Cat.logError(e);
			return false;
		}
	}

	public void setHostinfoDao(HostInfoRepository hostinfoDao) {
		m_hostinfoDao = hostinfoDao;
	}

	public void setServerConfigManager(ServerConfigManager manager) {
		m_manager = manager;
	}

	private boolean validateIp(String str) {
		Pattern pattern = Pattern.compile(
		      "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

	private boolean isNotEmpty(String value) {
		return value != null && value.length() > 0;
	}

	public class RefreshHost implements Task {
		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (true) {
				refresh();

				try {
					Thread.sleep(TimeHelper.ONE_MINUTE);
				} catch (InterruptedException e) {
					SLF4J_LOGGER.warn("Hostinfo refresh task interrupted.", e);
					Cat.logError(e);
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}
}
