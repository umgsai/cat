package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.heavy.entity.HeavyCache;
import com.dianping.cat.home.heavy.entity.HeavyCall;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.HeavySql;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportMerger.ServiceComparator;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportMerger.UrlComparator;
import com.dianping.cat.report.page.statistics.task.jar.JarReportBuilder;
import com.dianping.cat.service.ProjectService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SpringMvcStatisticsController {
	private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat queryDayFormat = new SimpleDateFormat("yyyy-MM-dd");

	private final SimpleDateFormat summaryTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Resource
	private AlertSummaryExecutor alertSummaryExecutor;

	@Resource
	private ClientReportService clientReportService;

	@Resource
	private HeavyReportService heavyReportService;

	@Resource
	private JarReportService jarReportService;

	@Resource
	private ProjectService projectService;

	@Resource
	private ServiceReportService serviceReportService;

	@Resource
	private UtilizationReportService utilizationReportService;

	@GetMapping("/mvc/r/statistics")
	public void statistics(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		forward(request, response);
	}

	@PostMapping("/mvc/r/statistics")
	public void submit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		forward(request, response);
	}

	Map<String, Object> statisticsModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = normalizeAction(parameter(request, "op", Constants.REPORT_SERVICE));
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = normalizeReportType(parameter(request, "reportType", "day"));
		String sort = parameter(request, "sort", "avg");
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));
		Date start = historyMode ? historyDates.getStart() : startDate(date);
		Date end = historyMode ? historyDates.getEnd() : new Date(start.getTime() + TimeHelper.ONE_HOUR);
		Date displayEnd = historyMode ? historyDates.getDisplayEnd() : end;

		model.put("contextPath", contextPath);
		model.put("baseUri", contextPath + "/mvc/r/statistics");
		model.put("activeReport", "Statistics");
		model.put("action", action);
		model.put("domain", domain);
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", historyMode ? historyDates.getReportType() : reportType);
		model.put("date", historyMode ? dayFormat.format(new Date(date)) : hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("sort", sort);
		model.put("historyMode", historyMode);
		model.put("navs", UrlNav.values());
		model.put("historyNavs", HistoryNav.values());
		model.put("currentNav", HistoryNav.getByName((String) model.get("reportType")));
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
		model.put("reportStart", displayFormat.format(start));
		model.put("reportEnd", displayFormat.format(displayEnd));
		model.put("encodedDomain", encode(domain));
		model.put("encodedIpAddress", encode(ipAddress));
		model.put("navAction", hourlyAction(action));

		if (Constants.REPORT_SERVICE.equals(action) || "historyService".equals(action)) {
			buildServiceInfo(model, domain, start, end, sort);
		} else if (Constants.REPORT_HEAVY.equals(action) || "historyHeavy".equals(action)) {
			buildHeavyInfo(model, start, end);
		} else if (Constants.REPORT_UTILIZATION.equals(action) || "historyUtilization".equals(action)) {
			buildUtilizationInfo(model, start, end);
		} else if (Constants.REPORT_JAR.equals(action)) {
			buildJarInfo(model, start, end);
		} else if (Constants.REPORT_CLIENT.equals(action)) {
			buildClientReport(model, request);
		} else if ("summary".equals(action)) {
			buildAlertSummary(model, request);
		}
		return model;
	}

	void setAlertSummaryExecutor(AlertSummaryExecutor executor) {
		alertSummaryExecutor = executor;
	}

	void setClientReportService(ClientReportService service) {
		clientReportService = service;
	}

	void setHeavyReportService(HeavyReportService service) {
		heavyReportService = service;
	}

	void setJarReportService(JarReportService service) {
		jarReportService = service;
	}

	void setProjectService(ProjectService service) {
		projectService = service;
	}

	void setServiceReportService(ServiceReportService service) {
		serviceReportService = service;
	}

	void setUtilizationReportService(UtilizationReportService service) {
		utilizationReportService = service;
	}

	private void buildAlertSummary(Map<String, Object> model, HttpServletRequest request) {
		String summaryDomain = request.getParameter("summarydomain");
		String summaryEmails = request.getParameter("summaryemails");
		Date summaryTime = summaryTime(request.getParameter("summarytime"));
		String summaryContent = "";

		if (StringUtils.isNotEmpty(summaryDomain)) {
			summaryContent = alertSummaryExecutor.execute(summaryDomain, summaryTime,
					StringUtils.isEmpty(summaryEmails) ? null : summaryEmails);
		}
		model.put("summaryContent", summaryContent);
		model.put("summarydomain", summaryDomain);
		model.put("summaryemails", summaryEmails);
		model.put("summarytime", summaryTime);
		model.put("summarytimeText", summaryTimeFormat.format(summaryTime));
	}

	private void buildClientReport(Map<String, Object> model, HttpServletRequest request) {
		Date startDate = clientDay(request.getParameter("day"));
		Date endDate = TimeHelper.addDays(startDate, 1);
		ClientReport report = clientReportService.queryReport(Constants.CAT, startDate, endDate);

		model.put("clientReport", report);
		model.put("day", queryDayFormat.format(startDate));
	}

	private void buildHeavyInfo(Map<String, Object> model, Date start, Date end) {
		HeavyReport heavyReport = heavyReportService.queryReport(Constants.CAT, start, end);

		model.put("heavyReport", heavyReport);
		buildSortedHeavyInfo(model, heavyReport);
	}

	private void buildJarInfo(Map<String, Object> model, Date start, Date end) {
		JarReport report = jarReportService.queryReport(Constants.CAT, start, end);

		model.put("jars", JarReportBuilder.s_jars);
		model.put("jarReport", report);
	}

	private void buildServiceInfo(Map<String, Object> model, String domain, Date start, Date end, String sort) {
		ServiceReport report = serviceReportService.queryReport(Constants.CAT, start, end);

		model.put("serviceList", sort(report, sort));
		model.put("serviceReport", report);
		model.put("domain", domain);
	}

	private void buildSortedHeavyInfo(Map<String, Object> model, HeavyReport report) {
		HeavyCall heavyCall = report.getHeavyCall();

		if (heavyCall != null) {
			List<Url> callUrls = new ArrayList<Url>(heavyCall.getUrls().values());
			List<Service> callServices = new ArrayList<Service>(heavyCall.getServices().values());

			Collections.sort(callUrls, new UrlComparator());
			Collections.sort(callServices, new ServiceComparator());
			model.put("callUrls", callUrls);
			model.put("callServices", callServices);
		}

		HeavySql heavySql = report.getHeavySql();

		if (heavySql != null) {
			List<Url> sqlUrls = new ArrayList<Url>(heavySql.getUrls().values());
			List<Service> sqlServices = new ArrayList<Service>(heavySql.getServices().values());

			Collections.sort(sqlUrls, new UrlComparator());
			Collections.sort(sqlServices, new ServiceComparator());
			model.put("sqlUrls", sqlUrls);
			model.put("sqlServices", sqlServices);
		}

		HeavyCache heavyCache = report.getHeavyCache();

		if (heavyCache != null) {
			List<Url> cacheUrls = new ArrayList<Url>(heavyCache.getUrls().values());
			List<Service> cacheServices = new ArrayList<Service>(heavyCache.getServices().values());

			Collections.sort(cacheUrls, new UrlComparator());
			Collections.sort(cacheServices, new ServiceComparator());
			model.put("cacheUrls", cacheUrls);
			model.put("cacheServices", cacheServices);
		}
	}

	private void buildUtilizationInfo(Map<String, Object> model, Date start, Date end) {
		UtilizationReport report = utilizationReportService.queryReport(Constants.CAT, start, end);
		Collection<com.dianping.cat.home.utilization.entity.Domain> domains = report.getDomains().values();
		List<com.dianping.cat.home.utilization.entity.Domain> webList = new LinkedList<com.dianping.cat.home.utilization.entity.Domain>();
		List<com.dianping.cat.home.utilization.entity.Domain> serviceList = new LinkedList<com.dianping.cat.home.utilization.entity.Domain>();

		for (com.dianping.cat.home.utilization.entity.Domain domain : domains) {
			Project project = projectService.findByDomain(domain.getId());

			if (project != null) {
				domain.setCmdbId(project.getCmdbDomain());
			}
			if (domain.findApplicationState("URL") != null) {
				webList.add(domain);
			}
			if (domain.findApplicationState("PigeonService") != null) {
				serviceList.add(domain);
			}
		}
		model.put("utilizationReport", report);
		model.put("utilizationWebList", webList);
		model.put("utilizationServiceList", serviceList);
	}

	private Date clientDay(String value) {
		if (value != null && value.length() == 10) {
			try {
				return queryDayFormat.parse(value);
			} catch (ParseException e) {
				// fall through
			}
		}
		return TimeHelper.getYesterday();
	}

	private long currentStartDay() {
		Calendar cal = Calendar.getInstance();

		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}

	private long date(String value, int step) {
		long current = System.currentTimeMillis();
		long currentHour = current - current % TimeHelper.ONE_HOUR;
		long result = currentHour;

		if (value != null && value.length() > 0) {
			try {
				result = value.length() == 10 ? hourlyFormat.parse(value).getTime() : dayFormat.parse(value).getTime();
			} catch (ParseException e) {
				result = currentHour;
			}
		}
		result = result + step * TimeHelper.ONE_HOUR;
		return Math.min(result, currentHour);
	}

	private Date dateParameter(String value) {
		if (value != null && value.length() > 0) {
			try {
				return value.length() == 10 ? hourlyFormat.parse(value) : dayFormat.parse(value);
			} catch (ParseException e) {
				// fall through
			}
		}
		return TimeHelper.getCurrentDay(-1);
	}

	private String encode(String value) {
		return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = statisticsModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/statistics/statistics.jsp");

		dispatcher.forward(request, response);
	}

	private HistoryDates historyDates(HttpServletRequest request, String reportType) {
		String normalizedReportType = normalizeReportType(reportType);
		long date = normalizeHistoryDate(dateParameter(request.getParameter("date")).getTime(), normalizedReportType,
				intParameter(request, "step", 0));
		Date start = historyStartDate(date, request.getParameter("startDate"));
		Date end = historyEndDate(date, normalizedReportType, request.getParameter("endDate"));

		return new HistoryDates(start.getTime(), normalizedReportType, start, end);
	}

	private Date historyEndDate(long date, String reportType, String customEnd) {
		Date custom = parseCustomDate(customEnd);

		if (custom != null) {
			return custom;
		}

		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(date);
		if ("month".equals(reportType)) {
			cal.add(Calendar.MONTH, 1);
		} else if ("week".equals(reportType)) {
			cal.add(Calendar.DATE, 7);
		} else {
			cal.add(Calendar.DATE, 1);
		}
		return cal.getTime();
	}

	private Date historyStartDate(long date, String customStart) {
		Date custom = parseCustomDate(customStart);

		return custom == null ? new Date(date) : custom;
	}

	private String hourlyAction(String action) {
		if ("historyService".equals(action)) {
			return Constants.REPORT_SERVICE;
		}
		if ("historyHeavy".equals(action)) {
			return Constants.REPORT_HEAVY;
		}
		if ("historyUtilization".equals(action)) {
			return Constants.REPORT_UTILIZATION;
		}
		return action;
	}

	private int intParameter(HttpServletRequest request, String name, int defaultValue) {
		String value = request.getParameter(name);

		if (value != null && value.length() > 0) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	private boolean isHistoryAction(String action) {
		return "historyService".equals(action) || "historyHeavy".equals(action) || "historyUtilization".equals(action);
	}

	private long normalizeHistoryDate(long date, String reportType, int step) {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date = cal.getTimeInMillis();

		if ("month".equals(reportType)) {
			cal.set(Calendar.DATE, 1);
			date = cal.getTimeInMillis();
		} else if ("week".equals(reportType)) {
			int weekOfDay = cal.get(Calendar.DAY_OF_WEEK) % 7;

			date = date - TimeHelper.ONE_DAY * (weekOfDay % 7);
			if (date > System.currentTimeMillis()) {
				date = date - 7 * TimeHelper.ONE_DAY;
			}
			cal.setTimeInMillis(date);
		}
		if (step < 0) {
			if ("month".equals(reportType)) {
				cal.add(Calendar.MONTH, step);
				date = cal.getTimeInMillis();
			} else if ("week".equals(reportType)) {
				date = date + 7 * TimeHelper.ONE_DAY * step;
			} else {
				date = date + TimeHelper.ONE_DAY * step;
			}
		} else {
			long temp;

			if ("month".equals(reportType)) {
				cal.add(Calendar.MONTH, step);
				temp = cal.getTimeInMillis();
			} else if ("week".equals(reportType)) {
				temp = date + 7 * TimeHelper.ONE_DAY * step;
			} else {
				temp = date + TimeHelper.ONE_DAY * step;
			}
			if (temp <= currentStartDay()) {
				date = temp;
			}
		}
		if ("day".equals(reportType) && date == currentStartDay()) {
			date = date - TimeHelper.ONE_DAY;
		}
		return date;
	}

	private String normalizeAction(String action) {
		if (Constants.REPORT_SERVICE.equals(action) || Constants.REPORT_CLIENT.equals(action)
				|| Constants.REPORT_HEAVY.equals(action) || Constants.REPORT_UTILIZATION.equals(action)
				|| Constants.REPORT_JAR.equals(action) || "summary".equals(action) || "historyService".equals(action)
				|| "historyHeavy".equals(action) || "historyUtilization".equals(action)) {
			return action;
		}
		return Constants.REPORT_SERVICE;
	}

	private String normalizeReportType(String reportType) {
		if ("month".equals(reportType) || "week".equals(reportType)) {
			return reportType;
		}
		return "day";
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		return value == null || value.length() == 0 ? defaultValue : value;
	}

	private Date parseCustomDate(String value) {
		if (value != null && value.length() > 0) {
			try {
				if (value.length() == 10) {
					return hourlyFormat.parse(value);
				} else if (value.length() == 8) {
					return dayFormat.parse(value);
				}
			} catch (ParseException e) {
				// ignore invalid custom date
			}
		}
		return null;
	}

	private List<com.dianping.cat.home.service.entity.Domain> sort(ServiceReport report, final String sortBy) {
		List<com.dianping.cat.home.service.entity.Domain> result = new ArrayList<com.dianping.cat.home.service.entity.Domain>(
				report.getDomains().values());

		Collections.sort(result, new Comparator<com.dianping.cat.home.service.entity.Domain>() {
			@Override
			public int compare(com.dianping.cat.home.service.entity.Domain d1,
					com.dianping.cat.home.service.entity.Domain d2) {
				if ("failure".equals(sortBy)) {
					return (int) (d2.getFailureCount() - d1.getFailureCount());
				} else if ("total".equals(sortBy)) {
					long value = d2.getTotalCount() - d1.getTotalCount();

					return value == 0 ? 0 : (value > 0 ? 1 : -1);
				} else if ("failurePercent".equals(sortBy) || "availability".equals(sortBy)) {
					return (int) (100000 * d2.getFailurePercent() - 100000 * d1.getFailurePercent());
				}
				return (int) (d2.getAvg() - d1.getAvg());
			}
		});
		return result;
	}

	private Date startDate(long date) {
		long current = System.currentTimeMillis();
		long currentHour = current - current % TimeHelper.ONE_HOUR;

		if (date == currentHour) {
			return new Date(date - TimeHelper.ONE_HOUR);
		}
		return new Date(date);
	}

	private Date summaryTime(String value) {
		if (value != null && value.length() > 0) {
			try {
				return summaryTimeFormat.parse(value);
			} catch (ParseException e) {
				// fall through
			}
		}
		return new Date();
	}

	private class HistoryDates {
		private final long m_date;

		private final Date m_displayEnd;

		private final Date m_end;

		private final String m_reportType;

		private final Date m_start;

		private HistoryDates(long date, String reportType, Date start, Date end) {
			m_date = date;
			m_reportType = reportType;
			m_start = start;
			m_end = end;
			m_displayEnd = new Date(end.getTime() - 1000);
		}

		private String getCustomDate() {
			return "&startDate=" + dayFormat.format(m_start) + "&endDate=" + dayFormat.format(m_end);
		}

		private long getDate() {
			return m_date;
		}

		private Date getDisplayEnd() {
			return m_displayEnd;
		}

		private Date getEnd() {
			return m_end;
		}

		private String getReportType() {
			return m_reportType;
		}

		private Date getStart() {
			return m_start;
		}
	}
}
