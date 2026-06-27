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
package com.dianping.cat.report.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.dianping.cat.report.service.ModelPeriod;

@Component
public class RemoteServersManager {

	private volatile Map<String, Set<String>> currentServers = new ConcurrentHashMap<String, Set<String>>();

	private volatile Map<String, Set<String>> lastServers = new ConcurrentHashMap<String, Set<String>>();

	public Set<String> queryServers(String domain, long time) {
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT) {
			return currentServers.get(domain);
		} else if (period == ModelPeriod.LAST) {
			return lastServers.get(domain);
		} else {
			return null;
		}
	}

	public void setCurrentServers(Map<String, Set<String>> currentServers) {
		this.currentServers = currentServers;
	}

	public void setLastServers(Map<String, Set<String>> lastServers) {
		this.lastServers = lastServers;
	}

}
