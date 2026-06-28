package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.cache.CacheReport;
import com.dianping.cat.report.page.cache.CacheReport.CacheNameItem;
import com.dianping.cat.report.page.cache.TransactionReportVistor;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.AllMachineMerger;
import com.dianping.cat.report.page.transaction.transform.AllNameMerger;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcCacheController {
	private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource(name = "eventModelService")
	private ModelService<EventReport> eventModelService;

	@Resource
	private EventReportService eventReportService;

	@Resource(name = "transactionModelService")
	private ModelService<TransactionReport> transactionModelService;

	@Resource
	private TransactionReportService transactionReportService;

	@GetMapping("/mvc/r/cache")
	public void cache(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = cacheModel(request);
		String view = Boolean.TRUE.equals(model.get("historyMode")) ? "/jsp/spring/report/cache/cacheHistory.jsp"
				: "/jsp/spring/report/cache/cache.jsp";

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> cacheModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String queryName = parameter(request, "queryname", "");
		String sort = request.getParameter("sort");
		String type = request.getParameter("type");
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));
		TransactionReport transactionReport = historyMode ? queryHistoryTransactionReport(domain, ipAddress, type, historyDates)
				: queryHourlyTransactionReport(domain, ipAddress, type, date);
		EventReport eventReport = historyMode ? queryHistoryEventReport(domain, ipAddress, type, historyDates)
				: queryHourlyEventReport(domain, ipAddress, type, date);
		CacheReport report = null;
		String pieChart = "{}";

		reportType = historyMode ? historyDates.getReportType() : reportType;
		if (transactionReport != null && eventReport != null) {
			report = buildCacheReport(transactionReport, eventReport, type, queryName, ipAddress, sort);
			if (!StringUtils.isEmpty(type)) {
				pieChart = buildPieChart(report);
			}
		}
		if (report == null) {
			report = emptyReport(domain, historyMode ? historyDates.getStart() : new Date(date),
					historyMode ? historyDates.getEnd() : new Date(date + TimeHelper.ONE_HOUR - 1), sort);
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : SortHelper.sortDomain(report.getIps());

		model.put("action", historyMode ? "history" : "view");
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain() == null ? domain : report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("date", historyMode ? dayFormat.format(new Date(date)) : hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", subtitleFormat.format(historyMode ? historyDates.getStart() : report.getStartTime()));
		model.put("reportEnd", subtitleFormat.format(historyMode ? historyDates.getDisplayEnd() : report.getEndTime()));
		model.put("ips", ips);
		model.put("queryName", queryName);
		model.put("encodedQueryName", encode(queryName));
		model.put("sort", sort);
		model.put("type", type);
		model.put("encodedType", encode(type));
		model.put("pieChart", pieChart);
		model.put("ipToHostnameStr", "{}");
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "ip=" + encode(ipAddress) + "&queryname=" + encode(queryName) + "&domain=" + encode(domain)
				+ (StringUtils.isEmpty(type) ? "" : "&type=" + encode(type)));
		model.put("historyMode", historyMode);
		model.put("historyNavs", HistoryNav.values());
		model.put("currentNav", HistoryNav.getByName(reportType));
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
		model.put("baseUri", contextPath + "/mvc/r/cache");
		model.put("activeReport", "Cache");
		return model;
	}

	void setEventModelService(ModelService<EventReport> service) {
		eventModelService = service;
	}

	void setEventReportService(EventReportService service) {
		eventReportService = service;
	}

	void setTransactionModelService(ModelService<TransactionReport> service) {
		transactionModelService = service;
	}

	void setTransactionReportService(TransactionReportService service) {
		transactionReportService = service;
	}

	private CacheReport buildCacheReport(TransactionReport transactionReport, EventReport eventReport, String type,
			String queryName, String ipAddress, String sort) {
		TransactionReportVistor visitor = new TransactionReportVistor();

		visitor.setType(type).setQueryName(queryName).setSortBy(sort).setCurrentIp(ipAddress);
		visitor.setEventReport(eventReport);
		visitor.visitTransactionReport(transactionReport);
		return visitor.getCacheReport();
	}

	private String buildPieChart(CacheReport report) {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (CacheNameItem cacheItem : report.getNameItems()) {
			String name = cacheItem.getName().getId();

			if (name.endsWith(":get") || name.endsWith(":mGet")) {
				items.add(new Item().setTitle(name).setNumber(cacheItem.getName().getTotalCount()));
			}
		}
		chart.addItems(items);
		return chart.getJsonString();
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
				// ignore invalid date and fall back to the same default as old MVC.
			}
		}
		return TimeHelper.getCurrentDay(-1);
	}

	private CacheReport emptyReport(String domain, Date start, Date end, String sort) {
		CacheReport report = new CacheReport();

		report.setDomain(domain);
		report.setStartTime(start);
		report.setEndTime(end);
		report.setSortBy(sort);
		return report;
	}

	private String encode(String value) {
		return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
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

	private HistoryDates historyDates(HttpServletRequest request, String reportType) {
		String normalizedReportType = normalizeReportType(reportType);
		long date = normalizeHistoryDate(dateParameter(request.getParameter("date")).getTime(), normalizedReportType,
				intParameter(request, "step", 0));
		Date start = historyStartDate(date, request.getParameter("startDate"));
		Date end = historyEndDate(date, normalizedReportType, request.getParameter("endDate"));

		return new HistoryDates(start.getTime(), normalizedReportType, start, end);
	}

	private Date historyStartDate(long date, String customStart) {
		Date custom = parseCustomDate(customStart);

		return custom == null ? new Date(date) : custom;
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
		return "history".equals(action);
	}

	private EventReport mergeAllEventMachines(EventReport report, String ipAddress) {
		if (report != null && Constants.ALL.equalsIgnoreCase(ipAddress)) {
			com.dianping.cat.report.page.event.transform.AllMachineMerger allEvent = new com.dianping.cat.report.page.event.transform.AllMachineMerger();

			allEvent.visitEventReport(report);
			report = allEvent.getReport();
		}
		return report;
	}

	private EventReport mergeAllEventNames(EventReport report, String type) {
		if (report != null && Constants.ALL.equalsIgnoreCase(type)) {
			com.dianping.cat.report.page.event.transform.AllNameMerger allEvent = new com.dianping.cat.report.page.event.transform.AllNameMerger();

			allEvent.visitEventReport(report);
			report = allEvent.getReport();
		}
		return report;
	}

	private TransactionReport mergeAllTransactionMachines(TransactionReport report, String ipAddress) {
		if (report != null && Constants.ALL.equalsIgnoreCase(ipAddress)) {
			AllMachineMerger all = new AllMachineMerger();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

	private TransactionReport mergeAllTransactionNames(TransactionReport report, String type) {
		if (report != null && Constants.ALL.equalsIgnoreCase(type)) {
			AllNameMerger all = new AllNameMerger();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
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
			long temp = date;

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
				// ignore invalid custom date.
			}
		}
		return null;
	}

	private EventReport queryHistoryEventReport(String domain, String ipAddress, String type, HistoryDates dates) {
		EventReport report = eventReportService.queryReport(domain, dates.getStart(), dates.getEnd());

		report = mergeAllEventMachines(report, ipAddress);
		report = mergeAllEventNames(report, type);
		return report;
	}

	private TransactionReport queryHistoryTransactionReport(String domain, String ipAddress, String type, HistoryDates dates) {
		TransactionReport report = transactionReportService.queryReport(domain, dates.getStart(), dates.getEnd());

		report = mergeAllTransactionMachines(report, ipAddress);
		report = mergeAllTransactionNames(report, type);
		return report;
	}

	private EventReport queryHourlyEventReport(String domain, String ipAddress, String type, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("ip", ipAddress);
		ModelResponse<EventReport> response;

		if (!StringUtils.isEmpty(type)) {
			request.setProperty("type", type);
		}
		response = eventModelService.invoke(request);
		EventReport report = response.getModel();

		report = mergeAllEventMachines(report, ipAddress);
		report = mergeAllEventNames(report, type);
		return report;
	}

	private TransactionReport queryHourlyTransactionReport(String domain, String ipAddress, String type, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("ip", ipAddress);

		if (!StringUtils.isEmpty(type)) {
			request.setProperty("type", type);
		}
		ModelResponse<TransactionReport> response = transactionModelService.invoke(request);
		TransactionReport report = response.getModel();

		report = mergeAllTransactionMachines(report, ipAddress);
		report = mergeAllTransactionNames(report, type);
		return report;
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
