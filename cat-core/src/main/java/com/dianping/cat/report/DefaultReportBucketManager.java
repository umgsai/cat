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
package com.dianping.cat.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.spring.CatSpringContext;

public class DefaultReportBucketManager implements ReportBucketManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultReportBucketManager.class);

	private ServerConfigManager m_configManager;

	private ReportBucketFactory m_bucketFactory;

	private Set<ReportBucket> m_factoryBuckets = Collections.synchronizedSet(
	      Collections.newSetFromMap(new IdentityHashMap<ReportBucket, Boolean>()));

	private File m_reportBaseDir;

	private volatile boolean m_initialized;

	@Override
	public void clearOldReports() {
		initialize();
		refreshSpringBeans();

		Transaction t = Cat.newTransaction("System", "DeleteReport");
		try {
			final List<String> toRemovePaths = new ArrayList<String>();
			final Set<String> validPaths = queryValidPath(m_configManager.getLocalReportStroageTime());

			for (String path : listRelativeFiles(m_reportBaseDir)) {
				if (shouldDeleteReport(path, validPaths)) {
					toRemovePaths.add(path);
				}
			}
			for (String path : toRemovePaths) {
				File file = new File(m_reportBaseDir, path);

				file.delete();
				Cat.logEvent("System", "DeleteReport", Event.SUCCESS, file.getAbsolutePath());
			}
			removeEmptyDir(m_reportBaseDir);
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	@Override
	public void closeBucket(ReportBucket bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			LOGGER.warn("Unable to close report bucket, bucket={}.", bucket, e);
		} finally {
			if (m_factoryBuckets.remove(bucket)) {
				LOGGER.debug("Closed Spring-created report bucket, bucket={}.", bucket);
			} else {
				LOGGER.debug("Closed externally-created report bucket, bucket={}.", bucket);
			}
		}
	}

	@Override
	public ReportBucket getReportBucket(long timestamp, String name, int index) throws IOException {
		initialize();

		Date date = new Date(timestamp);

		if (m_bucketFactory == null) {
			throw new IllegalStateException("ReportBucketFactory is required for report storage.");
		}
		ReportBucket bucket = m_bucketFactory.createReportBucket(name, date, index);

		m_factoryBuckets.add(bucket);
		return bucket;
	}

	public synchronized void initialize() {
		if (!m_initialized) {
			refreshSpringBeans();
			m_reportBaseDir = new File(Cat.getCatHome(), "bucket/report");
			LOGGER.info("Initialized report bucket manager, baseDir={}, springBucketFactoryConfigured={}.",
			      m_reportBaseDir.getAbsolutePath(), m_bucketFactory != null);
			m_initialized = true;
		}
	}

	private Set<String> queryValidPath(int day) {
		Set<String> strs = new HashSet<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		long currentTimeMillis = System.currentTimeMillis();

		for (int i = 0; i < day; i++) {
			Date date = new Date(currentTimeMillis - i * 24 * 60 * 60 * 1000L);

			strs.add(sdf.format(date));
		}
		return strs;
	}

	private List<String> listRelativeDirectories(File baseFile) {
		List<String> paths = new ArrayList<String>();

		if (baseFile == null || !baseFile.exists()) {
			return paths;
		}

		try (Stream<Path> stream = Files.walk(baseFile.toPath())) {
			stream.filter(path -> !path.equals(baseFile.toPath()))
			      .filter(Files::isDirectory)
			      .map(path -> relativePath(baseFile, path))
			      .forEach(paths::add);
		} catch (IOException e) {
			Cat.logError(e);
		}
		return paths;
	}

	private List<String> listRelativeFiles(File baseFile) {
		List<String> paths = new ArrayList<String>();

		if (baseFile == null || !baseFile.exists()) {
			return paths;
		}

		try (Stream<Path> stream = Files.walk(baseFile.toPath())) {
			stream.filter(Files::isRegularFile)
			      .map(path -> relativePath(baseFile, path))
			      .forEach(paths::add);
		} catch (IOException e) {
			Cat.logError(e);
		}
		return paths;
	}

	private String relativePath(File baseFile, Path path) {
		return baseFile.toPath().relativize(path).toString().replace(File.separatorChar, '/');
	}

	private void removeEmptyDir(File baseFile) {
		// the path has two depth
		for (int i = 0; i < 2; i++) {
			for (String path : listRelativeDirectories(baseFile)) {
				try {
					File file = new File(baseFile, path);

					file.delete();
				} catch (Exception e) {
				}
			}
		}
	}

	private void refreshSpringBeans() {
		ServerConfigManager configManager = CatSpringContext.getBeanIfAvailable(ServerConfigManager.class);

		if (configManager != null) {
			m_configManager = configManager;
		}
	}

	private boolean shouldDeleteReport(String path, Set<String> validPaths) {
		for (String str : validPaths) {
			if (path.contains(str)) {
				return false;
			}
		}
		return true;
	}

	public void setBucketFactory(ReportBucketFactory bucketFactory) {
		m_bucketFactory = bucketFactory;
	}

	public void setConfigManager(ServerConfigManager configManager) {
		m_configManager = configManager;
	}

}
