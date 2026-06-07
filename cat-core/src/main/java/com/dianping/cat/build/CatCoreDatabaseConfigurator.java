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

import com.dianping.cat.core.config.repository.ConfigRepository;

final class CatCoreDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
	private static final String CONFIG_DAO_ROLE = "com.dianping.cat.core.config.ConfigDao";

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineSimpleTableProviderComponents(all, "cat", com.dianping.cat.core.config._INDEX.getEntityClasses());
		defineDaoComponents(all, com.dianping.cat.core.config._INDEX.getDaoClasses());
		removeConfigDaoComponent(all);
		all.add(C(ConfigRepository.class).req(DataSourceManager.class));

		defineSimpleTableProviderComponents(all, "cat", com.dianping.cat.core.dal._INDEX.getEntityClasses());
		defineDaoComponents(all, com.dianping.cat.core.dal._INDEX.getDaoClasses());

		return all;
	}

	private void removeConfigDaoComponent(List<Component> components) {
		components.removeIf(component -> CONFIG_DAO_ROLE.equals(component.getModel().getRole()));
	}
}
