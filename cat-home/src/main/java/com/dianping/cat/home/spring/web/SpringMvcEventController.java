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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.event.DisplayNames;
import com.dianping.cat.report.page.event.DisplayNames.EventNameModel;
import com.dianping.cat.report.page.event.DisplayTypes;
import com.dianping.cat.report.page.event.EventGraphBuilder;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.event.transform.DistributionDetailVisitor;
import com.dianping.cat.report.page.event.transform.EventMergeHelper;
import com.dianping.cat.report.page.event.transform.PieGraphChartVisitor;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcEventController {
	private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final EventGraphBuilder eventGraphBuilder = new EventGraphBuilder();

	@Resource
	private DomainGroupConfigManager domainGroupConfigManager;

	@Resource
	private GraphBuilder graphBuilder;

	@Resource
	private HostinfoService hostinfoService;

	@Resource
	private ProjectService projectService;

	@Resource
	private SampleConfigManager sampleConfigManager;

	@Resource
	private EventMergeHelper eventMergeHelper;

	@Resource
	private EventReportService eventReportService;

	@Resource(name = "eventModelService")
	private ModelService<EventReport> eventModelService;

	@GetMapping("/mvc/r/e")
	public void event(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = eventModel(request);
		String action = (String) model.get("action");
		String view = isHistoryGraphAction(action) ? "/jsp/spring/report/event/eventHistoryGraphs.jsp"
				: "graphs".equals(action) ? "/jsp/spring/report/event/eventGraphs.jsp"
						: "/jsp/spring/report/event/event.jsp";

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> eventModel(HttpServletRequest request) {
		Cat.logMetricForCount("http-request-event");

		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String type = emptyToNull(request.getParameter("type"));
		String name = emptyToNull(request.getParameter("name"));
		String sortBy = emptyToNull(request.getParameter("sort"));
		String group = emptyToNull(request.getParameter("group"));
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));

		if (StringUtils.isEmpty(group)) {
			group = domainGroupConfigManager.queryDefaultGroup(domain);
		}
		reportType = historyMode ? historyDates.getReportType() : reportType;

		EventReport report = historyMode ? queryHistoryReport(domain, historyDates) : queryHourlyReport(domain, ipAddress, type, date);

		if (report != null && isGroupAction(action)) {
			report = filterReportByGroup(report, domain, group);
		}

		if (report != null) {
			report = eventMergeHelper.mergeAllIps(report, ipAddress);
		}
		if (report == null) {
			report = new EventReport(domain);
			report.setStartTime(historyMode ? historyDates.getStart() : new Date(date));
			report.setEndTime(historyMode ? historyDates.getEnd() : new Date(date + TimeHelper.ONE_HOUR));
		}

		if ("graphs".equals(action)) {
			buildGraphs(model, domain, ipAddress, type, name, date);
		} else if (isHistoryGraphAction(action)) {
			buildHistoryGraphs(model, domain, ipAddress, type, name, group, action, historyDates);
		} else if (StringUtils.isEmpty(type)) {
			model.put("displayTypeReport", new DisplayTypes().display(sortBy, ipAddress, report));
		} else {
			DisplayNames displayNames = new DisplayNames().display(sortBy, type, ipAddress, report);

			model.put("displayNameReport", displayNames);
			model.put("pieChart", buildEventNamePieChart(displayNames.getResults()));
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : SortHelper.sortIpAddress(report.getIps());

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("type", type);
		model.put("encodedType", encode(type));
		model.put("name", name);
		model.put("sortBy", sortBy);
		model.put("date", historyMode ? dayFormat.format(new Date(date)) : hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", subtitleFormat.format(historyMode ? historyDates.getStart() : report.getStartTime()));
		model.put("reportEnd", subtitleFormat.format(historyMode ? historyDates.getDisplayEnd() : report.getEndTime()));
		model.put("ips", ips);
		model.put("ipToHostnameStr", new JsonBuilder().toJson(ipToHostname(ips)));
		model.put("groups", domainGroupConfigManager.queryDomainGroup(domain));
		model.put("group", group);
		model.put("groupIps", domainGroupConfigManager.queryIpByDomainAndGroup(domain, group));
		model.put("domainGroups", domainGroups());
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "ip=" + ipAddress + "&domain=" + report.getDomain()
				+ (type == null ? "" : "&type=" + encode(type)));
		model.put("historyMode", historyMode);
		model.put("historyNavs", HistoryNav.values());
		model.put("currentNav", HistoryNav.getByName(reportType));
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
		model.put("baseUri", contextPath + "/mvc/r/e");
		model.put("sample", sample(report.getDomain()));
		model.put("model", model);
		return model;
	}

	private LineChart buildHistoryLineChart(Date start, Date end, String title, long step, double[] values) {
		LineChart chart = new LineChart();
		int size = (int) Math.max(0, (end.getTime() - start.getTime()) / step);

		chart.setStart(start);
		chart.setSize(size);
		chart.setStep(step);
		chart.setSubTitles(Collections.singletonList(buildHistorySubTitle(start, end)));
		chart.setTitle(title);
		chart.addValue(values == null ? new double[0] : values);
		return chart;
	}

	private String buildHistorySubTitle(Date start, Date end) {
		SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat to = new SimpleDateFormat("MM-dd");

		return from.format(start) + "~" + to.format(end);
	}

	private void buildGraphs(Map<String, Object> model, String domain, String ipAddress, String type, String name,
			long date) {
		EventReport report = queryHourlyGraphReport(domain, ipAddress, type, name, date);

		if (report != null) {
			if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
				PieGraphChartVisitor chartVisitor = new PieGraphChartVisitor(type, name);
				DistributionDetailVisitor detailVisitor = new DistributionDetailVisitor(type, name);

				chartVisitor.visitEventReport(report);
				detailVisitor.visitEventReport(report);
				model.put("distributionChart", chartVisitor.getPieChart().getJsonString());
				model.put("distributionDetails", detailVisitor.getDetails());
			}

			report = eventMergeHelper.mergeAllIps(report, ipAddress);
			String graphName = StringUtils.isEmpty(name) ? Constants.ALL : name;

			if (StringUtils.isEmpty(name)) {
				report = eventMergeHelper.mergeAllNames(report, ipAddress, graphName);
			}
			buildEventNameGraph(model, report, type, graphName, ipAddress);
		}
	}

	private void buildHistoryGraphs(Map<String, Object> model, String domain, String ipAddress, String type, String name,
			String group, String action, HistoryDates dates) {
		EventReport report = queryHistoryReport(domain, dates);

		if (report != null) {
			if (isGroupAction(action)) {
				report = filterReportByGroup(report, domain, group);
			}
			if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
				PieGraphChartVisitor chartVisitor = new PieGraphChartVisitor(type, name);
				DistributionDetailVisitor detailVisitor = new DistributionDetailVisitor(type, name);

				chartVisitor.visitEventReport(report);
				detailVisitor.visitEventReport(report);
				model.put("distributionChart", chartVisitor.getPieChart().getJsonString());
				model.put("distributionDetails", detailVisitor.getDetails());
			}

			report = eventMergeHelper.mergeAllIps(report, ipAddress);
			buildEventTrendGraph(model, report, type, name, ipAddress, dates);
		}
	}

	private void buildEventTrendGraph(Map<String, Object> model, EventReport report, String type, String name,
			String ipAddress, HistoryDates dates) {
		GraphTrend graph = findGraphTrend(report, ipAddress, type, name);
		int duration = graph == null || graph.getDuration() <= 0 ? 1 : graph.getDuration();
		long step = historyGraphStep(dates.getReportType()) * duration;
		String display = StringUtils.isEmpty(name) ? type : name;

		model.put("hitTrend", buildHistoryLineChart(dates.getStart(), dates.getEnd(),
				display + historyGraphHitTitle(dates.getReportType()), step,
				parseDoubles(graph == null ? null : graph.getCount())).getJsonString());
		model.put("failureTrend", buildHistoryLineChart(dates.getStart(), dates.getEnd(),
				display + historyGraphErrorTitle(dates.getReportType()), step,
				parseDoubles(graph == null ? null : graph.getFails())).getJsonString());
	}

	private void buildEventNameGraph(Map<String, Object> model, EventReport report, String type, String name, String ip) {
		EventType eventType = report.findOrCreateMachine(ip).findOrCreateType(type);
		EventName eventName = eventType.findOrCreateName(name);

		model.putAll(eventGraphBuilder.build(graphBuilder, eventName));
	}

	private String buildEventNamePieChart(List<EventNameModel> names) {
		PieChart chart = new PieChart();
		List<PieChart.Item> items = new ArrayList<PieChart.Item>();

		for (int i = 1; i < names.size(); i++) {
			EventNameModel name = names.get(i);
			PieChart.Item item = new PieChart.Item();
			EventName event = name.getDetail();

			item.setNumber(event.getTotalCount()).setTitle(event.getId());
			items.add(item);
		}
		chart.addItems(items);
		return new JsonBuilder().toJson(chart);
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
				result = value.length() == 10 ? hourlyFormat.parse(value).getTime()
						: new SimpleDateFormat("yyyyMMdd").parse(value).getTime();
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

	private Map<String, Department> domainGroups() {
		Collection<String> domains = projectService.findAllDomains();

		return projectService.findDepartments(domains);
	}

	private String emptyToNull(String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		return value;
	}

	private GraphTrend findGraphTrend(EventReport report, String ipAddress, String type, String name) {
		if (report == null || StringUtils.isEmpty(type)) {
			return null;
		}
		Machine machine = report.findMachine(ipAddress);

		if (machine == null) {
			return null;
		}
		EventType eventType = machine.findType(type);

		if (eventType == null) {
			return null;
		}
		if (StringUtils.isEmpty(name)) {
			return eventType.getGraphTrend();
		}
		EventName eventName = eventType.findName(name);

		return eventName == null ? null : eventName.getGraphTrend();
	}

	private String encode(String value) {
		if (value == null) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private EventReport filterReportByGroup(EventReport report, String domain, String group) {
		List<String> ips = domainGroupConfigManager.queryIpByDomainAndGroup(domain, group);
		List<String> removes = new ArrayList<String>();

		for (Machine machine : report.getMachines().values()) {
			String ip = machine.getIp();

			if (!ips.contains(ip)) {
				removes.add(ip);
			}
		}
		for (String ip : removes) {
			report.getMachines().remove(ip);
		}
		return report;
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

	private String historyGraphErrorTitle(String reportType) {
		return "day".equals(reportType) ? " Error (count/min)" : " Error (count/day)";
	}

	private String historyGraphHitTitle(String reportType) {
		return "day".equals(reportType) ? " Hits (count/min)" : " Hits (count/day)";
	}

	private long historyGraphStep(String reportType) {
		return "day".equals(reportType) ? TimeHelper.ONE_MINUTE : TimeHelper.ONE_DAY;
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

	private boolean isGroupAction(String action) {
		return "groupReport".equals(action) || "historyGroupReport".equals(action) || "groupGraphs".equals(action)
				|| "historyGroupGraph".equals(action);
	}

	private boolean isHistoryAction(String action) {
		return action != null && action.startsWith("history");
	}

	private boolean isHistoryGraphAction(String action) {
		return "historyGraph".equals(action) || "historyGroupGraph".equals(action);
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

	private String normalizeReportType(String reportType) {
		if ("month".equals(reportType) || "week".equals(reportType)) {
			return reportType;
		}
		return "day";
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

	private double[] parseDoubles(String value) {
		if (value == null || value.length() == 0) {
			return new double[0];
		}
		String[] parts = value.split(GraphTrendUtil.GRAPH_SPLITTER);
		double[] result = new double[parts.length];

		for (int i = 0; i < parts.length; i++) {
			try {
				result[i] = Double.parseDouble(parts[i]);
			} catch (NumberFormatException e) {
				result[i] = 0.0;
				Cat.logError(e);
			}
		}
		return result;
	}

	private Map<String, String> ipToHostname(List<String> ips) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		for (String ip : ips) {
			String hostname = hostinfoService.queryHostnameByIp(ip);

			if (hostname != null && !"null".equalsIgnoreCase(hostname)) {
				result.put(ip, hostname);
			}
		}
		return result;
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	private EventReport queryHourlyGraphReport(String domain, String ipAddress, String type, String name, long date) {
		String graphName = StringUtils.isEmpty(name) ? "*" : name;
		ModelRequest request = new ModelRequest(domain, date).setProperty("type", type).setProperty("name", graphName)
				.setProperty("ip", ipAddress);
		ModelResponse<EventReport> response = eventModelService.invoke(request);

		return response.getModel();
	}

	private EventReport queryHourlyReport(String domain, String ipAddress, String type, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("type", type).setProperty("ip", ipAddress);

		if (eventModelService.isEligable(request)) {
			ModelResponse<EventReport> response = eventModelService.invoke(request);

			return response.getModel();
		}
		return eventReportService.queryReport(domain, new Date(date), new Date(date + TimeHelper.ONE_HOUR));
	}

	private EventReport queryHistoryReport(String domain, HistoryDates dates) {
		return eventReportService.queryReport(domain, dates.getStart(), dates.getEnd());
	}

	private double sample(String domain) {
		Domain sampleDomain = sampleConfigManager.getConfig().findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
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
