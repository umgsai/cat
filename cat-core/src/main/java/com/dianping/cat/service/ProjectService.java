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

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.mybatis.ProjectRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProjectService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

	public static final String DEFAULT = "Default";

	@Resource
	private ProjectRepository projectRepository;

	@Resource
	private ServerConfigManager serverConfigManager;

	private ConcurrentHashMap<String, String> domains = new ConcurrentHashMap<String, String>();

	private ConcurrentHashMap<String, Project> domainToProjects = new ConcurrentHashMap<String, Project>();

	private ConcurrentHashMap<String, Project> cmdbToProjects = new ConcurrentHashMap<String, Project>();

	private volatile boolean initialized;

	public boolean contains(String domain) {
		ensureInitialized();

		return domains.containsKey(domain);
	}

	public Project create() {
		return projectRepository.createLocal();
	}

	public boolean delete(Project project) {
		ensureInitialized();

		long id = project.getId();
		String domainName = null;

		for (Entry<String, Project> entry : domainToProjects.entrySet()) {
			Project pro = entry.getValue();

			if (pro.getId() == id) {
				domainName = pro.getDomain();
				break;
			}
		}

		try {
			projectRepository.deleteByPK(project);

			if (domainName != null) {
				domainToProjects.remove(domainName);
				domains.remove(domainName);
			}

			String cmdbDomain = project.getCmdbDomain();

			if (cmdbDomain != null) {
				cmdbToProjects.remove(cmdbDomain);
			}

			return true;
		} catch (Exception e) {
			LOGGER.error("Unable to delete project, id={}, domain={}.", id, domainName, e);
			Cat.logError("delete project error ", e);
			return false;
		}
	}

	public List<Project> findAll() {
		ensureInitialized();

		return new ArrayList<Project>(domainToProjects.values());
	}

	public Set<String> findAllDomains() {
		ensureInitialized();

		return domains.keySet();
	}

	public Project findByDomain(String domainName) {
		ensureInitialized();

		Project project = domainToProjects.get(domainName);

		if (project != null) {
			return project;
		} else {
			try {
				Project pro = projectRepository.findByDomain(domainName);

				domainToProjects.put(pro.getDomain(), pro);
				return project;
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Project is missing or unavailable by domain={}.", domainName, e);
			} catch (Exception e) {
				LOGGER.error("Unable to find project by domain={}.", domainName, e);
				Cat.logError(e);
			}
			return null;
		}
	}

	public Map<String, Department> findDepartments(Collection<String> domains) {
		ensureInitialized();

		Map<String, Department> departments = new TreeMap<String, Department>();

		for (String domain : domains) {
			Project project = findProject(domain);
			String department = DEFAULT;
			String projectLine = DEFAULT;

			if (project != null) {
				String bu = project.getBu();
				String productLine = project.getCmdbProductline();

				department = bu == null ? DEFAULT : bu;
				projectLine = productLine == null ? DEFAULT : productLine;
			}
			Department temp = departments.get(department);

			if (temp == null) {
				temp = new Department();
				departments.put(department, temp);
			}
			temp.findOrCreatProjectLine(projectLine).addDomain(domain);
		}

		return departments;
	}

	public Project findProject(String domain) {
		ensureInitialized();

		Project project = domainToProjects.get(domain);

		if (project == null) {
			project = cmdbToProjects.get(domain);
		}
		return project;
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

		if (!serverConfigManager.isLocalMode()) {
			LOGGER.info("Initializing ProjectService in remote mode.");
			refresh();
		} else {
			LOGGER.info("Initializing ProjectService in local mode; skip database refresh.");
		}
		initialized = true;
	}

	public void setProjectDao(ProjectRepository projectDao) {
		projectRepository = projectDao;
	}

	public void setServerConfigManager(ServerConfigManager manager) {
		serverConfigManager = manager;
	}

	public boolean insert(Project project) {
		ensureInitialized();

		domainToProjects.put(project.getDomain(), project);

		try {
			int result = projectRepository.insert(project);

			if (result == 1) {
				LOGGER.info("Inserted project, domain={}, id={}.", project.getDomain(), project.getId());
				return true;
			} else {
				LOGGER.warn("Project insert returned unexpected row count, domain={}, result={}.", project.getDomain(),
						result);
				return false;
			}
		} catch (RuntimeException e) {
			LOGGER.error("Unable to insert project, domain={}.", project.getDomain(), e);
			Cat.logError(e);
			return false;
		}
	}

	public boolean insert(String domain) {
		ensureInitialized();

		Project project = create();

		project.setDomain(domain);
		project.setCmdbProductline(DEFAULT);
		project.setBu(DEFAULT);

		try {
			insert(project);
			domains.put(domain, domain);

			return true;
		} catch (Exception ex) {
			LOGGER.error("Unable to insert default project, domain={}.", domain, ex);
			Cat.logError(ex);
		}
		return false;
	}

	protected void refresh() {
		try {
			List<Project> projects = projectRepository.findAll();
			ConcurrentHashMap<String, Project> tmpDomainProjects = new ConcurrentHashMap<String, Project>();
			ConcurrentHashMap<String, Project> tmpCmdbProjects = new ConcurrentHashMap<String, Project>();
			ConcurrentHashMap<String, String> tmpDomains = new ConcurrentHashMap<String, String>();

			for (Project project : projects) {
				String domain = project.getDomain();

				tmpDomains.put(domain, domain);
				tmpDomainProjects.put(domain, project);

				String cmdb = project.getCmdbDomain();

				if (cmdb != null) {
					tmpCmdbProjects.put(cmdb, project);
				}
			}
			domains = tmpDomains;
			domainToProjects = tmpDomainProjects;
			cmdbToProjects = tmpCmdbProjects;
			LOGGER.info("Refreshed projects, projectCount={}, cmdbDomainCount={}.", projects.size(),
					tmpCmdbProjects.size());
		} catch (RuntimeException e) {
			LOGGER.error("Unable to refresh ProjectService projects.", e);
			Cat.logError("initialize ProjectService error", e);
		}
	}

	public boolean update(Project project) {
		ensureInitialized();

		domainToProjects.put(project.getDomain(), project);

		try {
			projectRepository.updateByPK(project);
			LOGGER.info("Updated project, domain={}, id={}.", project.getDomain(), project.getId());
			return true;
		} catch (RuntimeException e) {
			LOGGER.error("Unable to update project, domain={}, id={}.", project.getDomain(), project.getId(), e);
			Cat.logError(e);
			return false;
		}
	}

	public static class Department {

		private Map<String, ProjectLine> m_projectLines = new TreeMap<String, ProjectLine>();

		public ProjectLine findOrCreatProjectLine(String projectLine) {
			ProjectLine line = m_projectLines.get(String.valueOf(projectLine));

			if (line == null) {
				line = new ProjectLine();

				m_projectLines.put(projectLine, line);
			}
			return line;
		}

		public Map<String, ProjectLine> getProjectLines() {
			return m_projectLines;
		}
	}

	public static class ProjectLine {
		private List<String> m_lineDomains = new ArrayList<String>();

		public void addDomain(String name) {
			m_lineDomains.add(name);
		}

		public List<String> getLineDomains() {
			return m_lineDomains;
		}
	}

}
