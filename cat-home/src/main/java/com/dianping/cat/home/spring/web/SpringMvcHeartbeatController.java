package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.heartbeat.HeartbeatSvgGraph;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcHeartbeatController {
	private static final int MINUTE_ONE_DAY = 1440;

	private final SimpleDateFormat m_dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat m_hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat m_subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private DomainGroupConfigManager m_configManager;

	@Resource
	private GraphBuilder m_graphBuilder;

	@Resource
	private HeartbeatDisplayPolicyManager m_displayPolicyManager;

	@Resource
	private HostinfoService m_hostinfoService;

	@Resource
	private ProjectService m_projectService;

	@Resource
	private SampleConfigManager m_sampleConfigManager;

	@Resource
	private HeartbeatReportService m_reportService;

	@Resource
	@Qualifier("heartbeatModelService")
	private ModelService<HeartbeatReport> m_heartbeatService;

	@GetMapping("/mvc/r/h")
	public void heartbeat(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = heartbeatModel(request);
		String action = (String) model.get("action");
		String view = "historyPart".equals(action) ? "/jsp/spring/report/heartbeat/heartbeatPartHistoryGraph.jsp"
				: isHistoryAction(action) ? "/jsp/spring/report/heartbeat/heartbeatHistory.jsp"
						: "/jsp/spring/report/heartbeat/heartbeat.jsp";

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> heartbeatModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String type = parameter(request, "type", "frameworkThread");
		String extensionType = emptyToNull(request.getParameter("extensionType"));
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));

		reportType = historyMode ? historyDates.getReportType() : reportType;
		HeartbeatReport report = historyMode ? queryHistorySummaryReport(domain, historyDates)
				: queryHourlyReport(domain, ipAddress, date);

		if (report == null) {
			report = new HeartbeatReport(domain);
			report.setStartTime(historyMode ? historyDates.getStart() : new Date(date));
			report.setEndTime(historyMode ? historyDates.getEnd() : new Date(date + TimeHelper.ONE_HOUR));
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : SortHelper.sortIpAddress(report.getIps());
		String realIp = realIp(ipAddress, ips);

		if (historyMode) {
			List<String> extensionGroups = extractExtensionGroups(report);

			model.put("extensionGroups", extensionGroups);
			if ("historyPart".equals(action)) {
				buildExtensionHistoryGraphs(model, domain, realIp, extensionType, historyDates);
			}
		} else {
			HeartbeatSvgGraph heartbeat = new HeartbeatSvgGraph(m_graphBuilder, m_displayPolicyManager).display(report, realIp);

			model.put("extensionGraph", heartbeat.getExtensionGraph());
		}

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("realIp", realIp);
		model.put("reportType", reportType);
		model.put("type", type);
		model.put("extensionType", extensionType);
		model.put("date", historyMode ? m_dayFormat.format(new Date(date)) : m_hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", m_subtitleFormat.format(historyMode ? historyDates.getStart() : report.getStartTime()));
		model.put("reportEnd", m_subtitleFormat.format(historyMode ? historyDates.getDisplayEnd() : report.getEndTime()));
		model.put("ips", ips);
		model.put("ipToHostnameStr", new JsonBuilder().toJson(ipToHostname(ips)));
		model.put("groups", m_configManager.queryDomainGroup(domain));
		model.put("domainGroups", domainGroups());
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "ip=" + ipAddress + "&domain=" + report.getDomain());
		model.put("historyMode", historyMode);
		model.put("historyNavs", Collections.singletonList(HistoryNav.DAY));
		model.put("currentNav", HistoryNav.DAY);
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
		model.put("baseUri", contextPath + "/mvc/r/h");
		model.put("sample", sample(report.getDomain()));
		model.put("model", model);
		return model;
	}

	private void addMachineDataToMap(Map<String, double[]> datas, Machine machine, Set<String> extensionMetrics) {
		for (Period period : machine.getPeriods()) {
			int minute = period.getMinute();

			dealWithExtensions(datas, minute, period, extensionMetrics);
		}
		convertToDeltaArray(datas, extensionMetrics);
	}

	private Map<String, double[]> buildHeartbeatDatas(HeartbeatReport report, String ip) {
		Map<String, double[]> datas = new HashMap<String, double[]>();
		Set<String> extensionMetrics = new HashSet<String>();

		if (report != null) {
			Machine machine = report.findMachine(ip);

			if (machine != null) {
				addMachineDataToMap(datas, machine, extensionMetrics);
			}
		}
		return datas;
	}

	private void buildExtensionHistoryGraphs(Map<String, Object> model, String domain, String ip, String extensionType,
			HistoryDates dates) {
		HeartbeatReport report = m_reportService.queryReport(domain, dates.getStart(), dates.getEnd());
		Map<String, double[]> graphData = buildHeartbeatDatas(report, ip);
		List<String> metrics = m_displayPolicyManager.sortMetricNames(extensionType, queryMetricNames(report, extensionType));
		List<LineChart> graphs = extensionGraphs(metrics, graphData, dates.getStart(), dates.getSize());

		model.put("extensionCount", metrics.size());
		model.put("extensionHistoryGraphs", new JsonBuilder().toJson(graphs));
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
				result = value.length() == 10 ? m_hourlyFormat.parse(value).getTime()
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
				return value.length() == 10 ? m_hourlyFormat.parse(value) : m_dayFormat.parse(value);
			} catch (ParseException e) {
				// ignore invalid date and fall back to the same default as old MVC.
			}
		}
		return TimeHelper.getCurrentDay(-1);
	}

	private void dealWithExtensions(Map<String, double[]> datas, int minute, Period period, Set<String> extensionMetrics) {
		if (minute < 0 || minute >= MINUTE_ONE_DAY) {
			return;
		}
		for (String group : period.getExtensions().keySet()) {
			Extension currentExtension = period.findExtension(group);

			for (String metric : currentExtension.getDetails().keySet()) {
				extensionMetrics.add(metric);
				double value = currentExtension.findDetail(metric).getValue();
				int unit = m_displayPolicyManager.queryUnit(group, metric);

				updateMetricArray(datas, minute, metric, value / unit);
			}
		}
	}

	private Map<String, Department> domainGroups() {
		Collection<String> domains = m_projectService.findAllDomains();

		return m_projectService.findDepartments(domains);
	}

	private String emptyToNull(String value) {
		return value == null || value.length() == 0 ? null : value;
	}

	private List<LineChart> extensionGraphs(List<String> metrics, Map<String, double[]> graphData, Date start, int size) {
		List<LineChart> graphs = new ArrayList<LineChart>();

		for (String metric : metrics) {
			LineChart chart = new LineChart();

			chart.setStart(start);
			chart.setSize(size);
			chart.setTitle(metric);
			chart.addSubTitle(metric);
			chart.setStep(TimeHelper.ONE_MINUTE);
			chart.addValue(graphData.get(metric));
			graphs.add(chart);
		}
		return graphs;
	}

	private List<String> extractExtensionGroups(HeartbeatReport report) {
		Set<String> groupNames = new HashSet<String>();

		if (report != null) {
			for (Machine machine : report.getMachines().values()) {
				for (Period period : machine.getPeriods()) {
					groupNames.addAll(period.getExtensions().keySet());
				}
			}
		}
		return m_displayPolicyManager.sortGroupNames(groupNames);
	}

	private HistoryDates historyDates(HttpServletRequest request) {
		String reportType = "day";
		long date = normalizeHistoryDate(dateParameter(request.getParameter("date")).getTime(), intParameter(request, "step", 0));
		Date start = historyStartDate(date, request.getParameter("startDate"));
		Date end = historyEndDate(start.getTime(), request.getParameter("endDate"));

		return new HistoryDates(start.getTime(), reportType, start, end);
	}

	private Date historyEndDate(long date, String customEnd) {
		Date custom = parseCustomDate(customEnd);

		if (custom != null) {
			return custom;
		}
		return new Date(date + TimeHelper.ONE_DAY);
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
		return action != null && action.startsWith("history");
	}

	private long normalizeHistoryDate(long date, int step) {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date = cal.getTimeInMillis() + TimeHelper.ONE_DAY * step;
		if (date > currentStartDay()) {
			date = currentStartDay();
		}
		if (date == currentStartDay()) {
			date = date - TimeHelper.ONE_DAY;
		}
		return date;
	}

	private Map<String, String> ipToHostname(List<String> ips) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		for (String ip : ips) {
			String hostname = m_hostinfoService.queryHostnameByIp(ip);

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

	private Date parseCustomDate(String value) {
		if (value != null && value.length() > 0) {
			try {
				if (value.length() == 10) {
					return m_hourlyFormat.parse(value);
				} else if (value.length() == 8) {
					return m_dayFormat.parse(value);
				}
			} catch (ParseException e) {
				// ignore invalid custom date.
			}
		}
		return null;
	}

	private Set<String> queryMetricNames(HeartbeatReport report, String groupName) {
		Set<String> result = new HashSet<String>();

		if (report != null && groupName != null) {
			for (Machine machine : report.getMachines().values()) {
				for (Period period : machine.getPeriods()) {
					Extension extension = period.findExtension(groupName);

					if (extension != null) {
						result.addAll(extension.getDetails().keySet());
					}
				}
			}
		}
		return result;
	}

	private void convertToDeltaArray(Map<String, double[]> datas, Set<String> extensionMetrics) {
		convertToDeltaArrayPerHour(datas, "TotalStartedThread");
		convertToDeltaArrayPerHour(datas, "StartedThread");
		convertToDeltaArrayPerHour(datas, "NewGcCount");
		convertToDeltaArrayPerHour(datas, "OldGcCount");
		convertToDeltaArrayPerHour(datas, "CatMessageSize");
		convertToDeltaArrayPerHour(datas, "CatMessageOverflow");
		for (String metric : extensionMetrics) {
			convertToDeltaArrayPerHour(datas, metric);
		}
	}

	private void convertToDeltaArrayPerHour(Map<String, double[]> datas, String metric) {
		double[] values = datas.get(metric);

		if (values != null) {
			double[] targets = new double[MINUTE_ONE_DAY];

			for (int i = 1; i < MINUTE_ONE_DAY; i++) {
				if (values[i - 1] > 0) {
					double delta = values[i] - values[i - 1];

					if (delta >= 0) {
						targets[i] = delta;
					}
				}
			}
			datas.put(metric, targets);
		}
	}

	private HeartbeatReport queryHourlyReport(String domain, String ipAddress, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("ip", ipAddress);

		if (m_heartbeatService.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_heartbeatService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private HeartbeatReport queryHistorySummaryReport(String domain, HistoryDates dates) {
		Date start = new Date(dates.getDate() + 23 * TimeHelper.ONE_HOUR);
		Date end = new Date(dates.getDate() + 24 * TimeHelper.ONE_HOUR);

		return m_reportService.queryReport(domain, start, end);
	}

	private String realIp(String ipAddress, List<String> ips) {
		if ((ipAddress == null || ipAddress.length() == 0 || Constants.ALL.equals(ipAddress)) && !ips.isEmpty()) {
			return ips.get(0);
		}
		return ipAddress;
	}

	private double sample(String domain) {
		Domain sampleDomain = m_sampleConfigManager.getConfig().findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}

	private void updateMetricArray(Map<String, double[]> datas, int minute, String metricName, double value) {
		double[] values = datas.get(metricName);

		if (values == null) {
			values = new double[MINUTE_ONE_DAY];
			datas.put(metricName, values);
		}
		values[minute] = value;
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
			return "&startDate=" + m_dayFormat.format(m_start) + "&endDate=" + m_dayFormat.format(m_end);
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

		private int getSize() {
			return (int) ((m_end.getTime() - m_start.getTime()) / TimeHelper.ONE_HOUR * 60);
		}

		private Date getStart() {
			return m_start;
		}
	}
}
