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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.mybatis.HourlyReportContentRepository;
import com.dianping.cat.mybatis.HourlyReportRepository;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import static com.dianping.cat.Constants.HOUR;

/**
	* Hourly report manager by domain of one report type(such as Transaction, Event, Problem, Heartbeat etc.) produced in one machine
	* for a couple of hours.
	*/
public class DefaultReportManager<T> implements ReportManager<T> {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DefaultReportManager.class);

	private ReportDelegate<T> reportDelegate;

	private ReportBucketManager bucketManager;

	private HourlyReportRepository reportDao;

	private HourlyReportContentRepository reportContentDao;

	private DomainValidator validator;

	private String name;

	private Map<Long, Map<String, T>> reports = new ConcurrentHashMap<Long, Map<String, T>>();

	public void cleanup(long time) {
		List<Long> startTimes = new ArrayList<Long>(reports.keySet());

		for (long startTime : startTimes) {
			if (startTime <= time) {
				synchronized (reports) {
					reports.remove(startTime);
				}
			}
		}
	}

	public void destory() {
	}

	@Override
	public Set<String> getDomains(long startTime) {
		Map<String, T> hourlyReports = reports.get(startTime);

		if (hourlyReports == null) {
			return new HashSet<String>();
		} else {
			Set<String> domains = hourlyReports.keySet();
			Set<String> result = new HashSet<String>();

			for (String domain : domains) {
				if (validator.validate(domain)) {
					result.add(domain);
				}
			}
			return result;
		}
	}

	@Override
	public T getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
		Map<String, T> hourlyReports = reports.get(startTime);

		if (hourlyReports == null && createIfNotExist) {
			synchronized (reports) {
				hourlyReports = reports.get(startTime);

				if (hourlyReports == null) {
					hourlyReports = new ConcurrentHashMap<String, T>();
					reports.put(startTime, hourlyReports);
				}
			}
		}

		if (hourlyReports == null) {
			hourlyReports = new LinkedHashMap<String, T>();
		}

		T report = hourlyReports.get(domain);

		if (report == null && createIfNotExist) {
			synchronized (hourlyReports) {
				report = reportDelegate.makeReport(domain, startTime, HOUR);
				hourlyReports.put(domain, report);
			}
		}

		if (report == null) {
			report = reportDelegate.makeReport(domain, startTime, HOUR);
		}

		return report;
	}

	@Override
	public Map<String, T> getHourlyReports(long startTime) {
		Map<String, T> hourlyReports = reports.get(startTime);

		if (hourlyReports == null) {
			return Collections.emptyMap();
		} else {
			return hourlyReports;
		}
	}

	@Override
	public void initialize() {
	}

	@Override
	public Map<String, T> loadHourlyReports(long startTime, StoragePolicy policy, int index) {
		Transaction t = Cat.newTransaction("Restore", name);
		Map<String, T> hourlyReports = reports.get(startTime);
		Cat.logEvent("Restore", name + ":" + index);
		ReportBucket bucket = null;

		if (hourlyReports == null) {
			hourlyReports = new ConcurrentHashMap<String, T>();
			reports.put(startTime, hourlyReports);
		}

		try {
			bucket = bucketManager.getReportBucket(startTime, name, index);

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				T report = reportDelegate.parseXml(xml);

				hourlyReports.put(id, report);
			}

			reportDelegate.afterLoad(hourlyReports);
			t.setStatus(Message.SUCCESS);
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.logError(e);
			LOGGER.error("Error when loading {} reports of {}.", name, new Date(startTime), e);
		} finally {
			t.complete();

			if (bucket != null) {
				bucketManager.closeBucket(bucket);
			}
		}
		return hourlyReports;
	}

	@Override
	public Map<String, T> loadLocalReports(long startTime, int index) {
		Transaction t = Cat.newTransaction("ReloadLocalTask", name);
		Cat.logEvent("ReloadLocal", name + ":" + index + ":" + new Date(startTime));
		ReportBucket bucket = null;
		Map<String, T> hourlyReports = new ConcurrentHashMap<String, T>();

		try {
			bucket = bucketManager.getReportBucket(startTime, name, index);

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				T report = reportDelegate.parseXml(xml);

				hourlyReports.put(id, report);
			}

			t.setStatus(Message.SUCCESS);
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.logError(e);
			LOGGER.error("Error when loading local {} reports of {}, index={}.", name, new Date(startTime), index, e);
		} finally {
			t.complete();

			if (bucket != null) {
				bucketManager.closeBucket(bucket);
			}
		}
		return hourlyReports;
	}

	public void setBucketManager(ReportBucketManager bucketManager) {
		this.bucketManager = bucketManager;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReportContentDao(HourlyReportContentRepository reportContentDao) {
		this.reportContentDao = reportContentDao;
	}

	public void setReportDao(HourlyReportRepository reportDao) {
		this.reportDao = reportDao;
	}

	public void setReportDelegate(ReportDelegate<T> reportDelegate) {
		this.reportDelegate = reportDelegate;
	}

	public void setValidator(DomainValidator validator) {
		this.validator = validator;
	}

	private void storeDatabase(long startTime, Map<String, T> reports) {
		Date period = new Date(startTime);
		Date creationDate = new Date();
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		for (T report : reports.values()) {
			try {
				String domain = reportDelegate.getDomain(report);
				HourlyReport r = reportDao.createLocal();

				r.setName(name);
				r.setDomain(domain);
				r.setPeriod(period);
				r.setIp(ip);
				r.setType(1);
				r.setCreationDate(creationDate);

				reportDao.insert(r);

				long id = r.getId();
				byte[] binaryContent = reportDelegate.buildBinary(report);
				HourlyReportContent content = reportContentDao.createLocal();

				content.setReportId(id);
				content.setContent(binaryContent);
				content.setPeriod(period);
				content.setCreationDate(creationDate);
				reportContentDao.insert(content);
				reportDelegate.createHourlyTask(report);
			} catch (Throwable e) {
				Cat.logError(e);
				LOGGER.error("Error when storing {} report to database, startTime={}.", name, period, e);
			}
		}
	}

	private void storeFile(Map<String, T> reports, ReportBucket bucket) {
		for (T report : reports.values()) {
			try {
				String domain = reportDelegate.getDomain(report);
				String xml = reportDelegate.buildXml(report);

				bucket.storeById(domain, xml);
			} catch (Exception e) {
				Cat.logError(e);
				LOGGER.error("Error when storing {} report to local file.", name, e);
			}
		}
	}

	@Override
	public void storeHourlyReports(long startTime, StoragePolicy policy, int index) {
		Transaction t = Cat.newTransaction("Checkpoint", name);
		Map<String, T> hourlyReports = reports.get(startTime);
		ReportBucket bucket = null;

		try {
			t.addData("reports", hourlyReports == null ? 0 : hourlyReports.size());

			if (hourlyReports != null) {
				Set<String> errorDomains = new HashSet<String>();

				for (String domain : hourlyReports.keySet()) {
					if (!validator.validate(domain)) {
						errorDomains.add(domain);
					}
				}
				for (String domain : errorDomains) {
					hourlyReports.remove(domain);
				}
				if (!errorDomains.isEmpty()) {
					LOGGER.info("error domain:{}", errorDomains);
				}

				reportDelegate.beforeSave(hourlyReports);

				if (policy.forFile()) {
					bucket = bucketManager.getReportBucket(startTime, name, index);

					try {
						storeFile(hourlyReports, bucket);
					} finally {
						bucketManager.closeBucket(bucket);
					}
				}

				if (policy.forDatabase()) {
					storeDatabase(startTime, hourlyReports);
				}
			}
			t.setStatus(Message.SUCCESS);
		} catch (Throwable e) {
			Cat.logError(e);
			t.setStatus(e);
			LOGGER.error("Error when storing {} reports of {}.", name, new Date(startTime), e);
		} finally {
			cleanup(startTime);
			t.complete();

			if (bucket != null) {
				bucketManager.closeBucket(bucket);
			}
		}
	}

	public static enum StoragePolicy {
		FILE,

		FILE_AND_DB;

		public boolean forDatabase() {
			return this == FILE_AND_DB;
		}

		public boolean forFile() {
			return this == FILE_AND_DB || this == FILE;
		}
	}

}
