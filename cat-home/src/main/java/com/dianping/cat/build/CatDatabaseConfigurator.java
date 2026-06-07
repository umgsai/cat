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
package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.core.mybatis.repository.alert.summary.AlertSummaryRepository;
import com.dianping.cat.core.mybatis.repository.alteration.AlterationRepository;
import com.dianping.cat.core.mybatis.repository.baseline.BaselineRepository;
import com.dianping.cat.core.mybatis.repository.config.modification.ConfigModificationRepository;
import com.dianping.cat.core.mybatis.repository.metric.graph.MetricGraphRepository;
import com.dianping.cat.core.mybatis.repository.metric.screen.MetricScreenRepository;
import com.dianping.cat.core.mybatis.repository.overload.OverloadRepository;
import com.dianping.cat.core.mybatis.repository.topologygraph.TopologyGraphRepository;

final class CatDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
	private static final String[] REPLACED_DAO_ROLES = { "com.dianping.cat.home.dal.report.AlertSummaryDao",
			"com.dianping.cat.home.dal.report.AlterationDao", "com.dianping.cat.home.dal.report.BaselineDao",
			"com.dianping.cat.home.dal.report.ConfigModificationDao",
			"com.dianping.cat.home.dal.report.MetricGraphDao", "com.dianping.cat.home.dal.report.MetricScreenDao",
			"com.dianping.cat.home.dal.report.OverloadDao", "com.dianping.cat.home.dal.report.TopologyGraphDao" };

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		// all.add(defineJdbcDataSourceComponent("cat", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/cat", "root", "***", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

		defineSimpleTableProviderComponents(all, "cat", com.dianping.cat.home.dal.report._INDEX.getEntityClasses());
		defineDaoComponents(all, com.dianping.cat.home.dal.report._INDEX.getDaoClasses());
		removeReplacedDaoComponents(all);
		addRepositoryComponents(all);

		return all;
	}

	private void addRepositoryComponents(List<Component> components) {
		components.add(C(AlertSummaryRepository.class).req(DataSourceManager.class));
		components.add(C(AlterationRepository.class).req(DataSourceManager.class));
		components.add(C(BaselineRepository.class).req(DataSourceManager.class));
		components.add(C(ConfigModificationRepository.class).req(DataSourceManager.class));
		components.add(C(MetricGraphRepository.class).req(DataSourceManager.class));
		components.add(C(MetricScreenRepository.class).req(DataSourceManager.class));
		components.add(C(OverloadRepository.class).req(DataSourceManager.class));
		components.add(C(TopologyGraphRepository.class).req(DataSourceManager.class));
	}

	private boolean isReplacedDaoRole(String role) {
		for (String replacedDaoRole : REPLACED_DAO_ROLES) {
			if (replacedDaoRole.equals(role)) {
				return true;
			}
		}
		return false;
	}

	private void removeReplacedDaoComponents(List<Component> components) {
		components.removeIf(component -> isReplacedDaoRole(component.getModel().getRole()));
	}
}
