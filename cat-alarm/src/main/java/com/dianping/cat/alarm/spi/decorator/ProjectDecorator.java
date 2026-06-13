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

import org.unidal.lookup.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.spring.CatSpringContext;

public abstract class ProjectDecorator extends Decorator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDecorator.class);

	protected ProjectService m_projectService;

	public void setProjectService(ProjectService projectService) {
		m_projectService = projectService;
	}

	private ProjectService getProjectService() {
		if (m_projectService == null) {
			ProjectService projectService = CatSpringContext.getBeanIfAvailable(ProjectService.class);

			if (projectService != null) {
				m_projectService = projectService;
				LOGGER.info("ProjectDecorator refreshed Spring ProjectService dependency.");
			}
		}
		return m_projectService;
	}

	public String buildContactInfo(String domainName) {
		try {
			ProjectService projectService = getProjectService();
			Project project = projectService == null ? null : projectService.findByDomain(domainName);

			if (project != null) {
				String owners = project.getOwner();
				String phones = project.getPhone();
				StringBuilder builder = new StringBuilder();

				if (!StringUtils.isEmpty(owners)) {
					builder.append("[业务负责人: ").append(owners).append(" ]");
				}
				if (!StringUtils.isEmpty(phones)) {
					builder.append("[负责人手机号码: ").append(phones).append(" ]");
				}

				return builder.toString();
			}
		} catch (Exception ex) {
			LOGGER.error("Unable to build project contact info, domain={}.", domainName, ex);
			Cat.logError("build project contact info error for domain: " + domainName, ex);
		}

		return "";
	}
}
