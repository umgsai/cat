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
package com.dianping.cat.report.page.transaction.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.Graph;
import com.dianping.cat.consumer.transaction.model.entity.Graph2;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;

public class TransactionReportService extends AbstractReportService<TransactionReport> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionReportService.class);

	private TransactionReport convert(TransactionReport report) {
		Date start = report.getStartTime();
		Date end = report.getEndTime();

		try {
			if (start != null && end != null) {
				TpsStatistics statistics = new TpsStatistics((end.getTime() - start.getTime()) / 1000.0);

				report.accept(statistics);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to convert transaction report, domain={}, start={}, end={}.", report.getDomain(), start,
					end, e);
			Cat.logError(e);
		}

		// for old report, can be removed later.
		AllMachineRemover remover = new AllMachineRemover();
		report.accept(remover);

		GraphTrendParser graphTrendParser = new GraphTrendParser();
		report.accept(graphTrendParser);

		return report;
	}

	@Override
	public TransactionReport makeReport(String domain, Date start, Date end) {
		TransactionReport report = new TransactionReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public TransactionReport queryDailyReport(String domain, Date start, Date end) {
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = TransactionAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao
										.findByDomainNamePeriod(domain, name, new Date(startTime));
				TransactionReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (EmptyResultDataAccessException e) {
				LOGGER.warn("Transaction daily report is missing, domain={}, period={}.", domain, new Date(startTime), e);
			} catch (Exception e) {
				LOGGER.error("Unable to query transaction daily report, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		transactionReport.setStartTime(start);
		transactionReport.setEndTime(end);
		return convert(transactionReport);
	}

	private TransactionReport queryFromDailyBinary(long id, String domain) {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TransactionReport(domain);
		}
	}

	private TransactionReport queryFromHourlyBinary(int id, Date period, String domain) {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TransactionReport(domain);
		}
	}

	private TransactionReport queryFromMonthlyBinary(int id, String domain) {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TransactionReport(domain);
		}
	}

	private TransactionReport queryFromWeeklyBinary(int id, String domain) {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TransactionReport(domain);
		}
	}

	@Override
	public TransactionReport queryHourlyReport(String domain, Date start, Date end) {
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = TransactionAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao
										.findAllByDomainNamePeriod(new Date(startTime), domain, name);
			} catch (RuntimeException e) {
				LOGGER.error("Unable to query transaction hourly report list, domain={}, period={}.", domain,
						new Date(startTime), e);
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						TransactionReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);

						reportModel.accept(merger);
					} catch (EmptyResultDataAccessException e) {
						LOGGER.warn("Transaction hourly report content is missing, domain={}, reportId={}, period={}.",
								domain, report.getId(), report.getPeriod(), e);
					} catch (Exception e) {
						LOGGER.error("Unable to parse transaction hourly report, domain={}, reportId={}, period={}.",
								domain, report.getId(), report.getPeriod(), e);
						Cat.logError(e);
					}
				}
			}
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		transactionReport.setStartTime(start);
		transactionReport.setEndTime(new Date(end.getTime() - 1));

		return convert(transactionReport);
	}

	@Override
	public TransactionReport queryMonthlyReport(String domain, Date start) {
		TransactionReport transactionReport = new TransactionReport(domain);

		try {
			MonthlyReport entity = m_monthlyReportDao
									.findReportByDomainNamePeriod(start, domain, TransactionAnalyzer.ID);
			transactionReport = queryFromMonthlyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Transaction monthly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query transaction monthly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return convert(transactionReport);
	}

	@Override
	public TransactionReport queryWeeklyReport(String domain, Date start) {
		TransactionReport transactionReport = new TransactionReport(domain);

		try {
			WeeklyReport entity = m_weeklyReportDao
									.findReportByDomainNamePeriod(start, domain, TransactionAnalyzer.ID);
			transactionReport = queryFromWeeklyBinary(entity.getId(), domain);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.warn("Transaction weekly report is missing, domain={}, period={}.", domain, start, e);
		} catch (Exception e) {
			LOGGER.error("Unable to query transaction weekly report, domain={}, period={}.", domain, start, e);
			Cat.logError(e);
		}
		return convert(transactionReport);
	}

	public class TpsStatistics extends BaseVisitor {

		public double m_duration;

		public TpsStatistics(double duration) {
			m_duration = duration;
		}

		@Override
		public void visitName(TransactionName name) {
			if (m_duration > 0) {
				name.setTps(name.getTotalCount() * 1.0 / m_duration);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			if (m_duration > 0) {
				type.setTps(type.getTotalCount() * 1.0 / m_duration);
				super.visitType(type);
			}
		}
	}

	public class GraphTrendParser extends BaseVisitor {
		@Override
		public void visitType(TransactionType type) {
			Map<Integer, Graph2> graph2s = type.getGraph2s();

			if (graph2s != null && graph2s.size() > 0 && type.getGraphTrend() == null) {
				Graph2 graph2 = graph2s.entrySet().iterator().next().getValue();
				GraphTrend graphTrend = new GraphTrend();

				graphTrend.setDuration(graph2.getDuration());
				graphTrend.setAvg(graph2.getAvg());
				graphTrend.setCount(graph2.getCount());
				graphTrend.setFails(graph2.getFails());
				graphTrend.setSum(graph2.getSum());
				type.setGraphTrend(graphTrend);

				graph2s.clear();
			}
			super.visitType(type);
		}

		@Override
		public void visitName(TransactionName name) {
			Map<Integer, Graph> graphs = name.getGraphs();

			if (graphs != null && graphs.size() > 0 && name.getGraphTrend() == null) {
				Graph graph = graphs.entrySet().iterator().next().getValue();
				GraphTrend graphTrend = new GraphTrend();

				graphTrend.setDuration(graph.getDuration());
				graphTrend.setAvg(graph.getAvg());
				graphTrend.setCount(graph.getCount());
				graphTrend.setFails(graph.getFails());
				graphTrend.setSum(graph.getSum());
				name.setGraphTrend(graphTrend);

				graphs.clear();
			}
		}
	}

	public class AllMachineRemover extends BaseVisitor {

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			transactionReport.removeMachine(Constants.ALL);
		}
	}

}
