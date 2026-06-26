package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

import com.dianping.cat.Constants;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.problem.LongConfig;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.problem.transform.HourlyLineChartVisitor;
import com.dianping.cat.report.page.problem.transform.PieGraphChartVisitor;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.SampleConfig;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcProblemController {
	private final SimpleDateFormat m_dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat m_hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat m_subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private DomainGroupConfigManager m_configManager;

	@Resource
	private HostinfoService m_hostinfoService;

	@Resource
	private ProjectService m_projectService;

	@Resource
	private SampleConfigManager m_sampleConfigManager;

	@Resource
	private ServerConfigManager m_serverConfigManager;

	@Resource
	private ProblemReportService m_reportService;

	@Resource
	@Qualifier("problemModelService")
	private ModelService<ProblemReport> m_problemService;

	@GetMapping("/mvc/r/p")
	public void problem(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = problemModel(request);
		String action = (String) model.get("action");
		String view = isHistoryGraphAction(action) ? "/jsp/spring/report/problem/problemHistoryGraphs.jsp"
				: isHourlyGraphAction(action) ? "/jsp/spring/report/problem/problemHourlyGraphs.jsp"
						: isHistoryAction(action) ? "/jsp/spring/report/problem/problemHistory.jsp"
								: "/jsp/spring/report/problem/problemStatics.jsp";

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> problemModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String type = emptyToNull(request.getParameter("type"));
		String status = emptyToNull(request.getParameter("status"));
		String group = emptyToNull(request.getParameter("group"));
		int urlThreshold = intParameter(request, "urlThreshold", 1000);
		int sqlThreshold = intParameter(request, "sqlThreshold", 100);
		int serviceThreshold = intParameter(request, "serviceThreshold", 50);
		int cacheThreshold = intParameter(request, "cacheThreshold", 10);
		int callThreshold = intParameter(request, "callThreshold", 50);
		LongConfig longConfig = new LongConfig().setUrlThreshold(urlThreshold).setSqlThreshold(sqlThreshold)
				.setServiceThreshold(serviceThreshold).setCacheThreshold(cacheThreshold).setCallThreshold(callThreshold);
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate()
				: date(request.getParameter("date"), intParameter(request, "step", 0));

		if (StringUtils.isEmpty(group)) {
			group = m_configManager.queryDefaultGroup(domain);
		}
		reportType = historyMode ? historyDates.getReportType() : reportType;

		ProblemReport report = historyMode ? queryHistoryReport(domain, historyDates)
				: queryHourlyReport(domain, ipAddress, type, status, date, isHourlyGraphAction(action) ? "detail" : "view");

		if (report != null && isGroupAction(action)) {
			report = filterReportByGroup(report, domain, group);
		}

		if (report == null) {
			report = new ProblemReport(domain);
			report.setStartTime(historyMode ? historyDates.getStart() : new Date(date));
			report.setEndTime(historyMode ? historyDates.getEnd() : new Date(date + TimeHelper.ONE_HOUR));
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : SortHelper.sortIpAddress(report.getIps());

		if (isHourlyGraphAction(action)) {
			buildHourlyGraphs(model, report, isGroupAction(action) ? Constants.ALL : ipAddress, type, status);
		} else if (isHistoryGraphAction(action)) {
			buildHistoryGraphs(model, report, ipAddress, type, status, historyDates);
		} else {
			ProblemStatistics statistics = new ProblemStatistics().setLongConfig(longConfig);

			if (Constants.ALL.equals(ipAddress)) {
				statistics.setAllIp(true);
			} else {
				statistics.setIp(ipAddress);
			}
			statistics.visitProblemReport(report);
			model.put("allStatistics", statistics);
			model.put("defaultThreshold", defaultUrlThreshold(domain));
			model.put("defaultSqlThreshold", defaultSqlThreshold(domain));
		}

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("type", type);
		model.put("status", status);
		model.put("date", historyMode ? m_dayFormat.format(new Date(date)) : m_hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", m_subtitleFormat.format(historyMode ? historyDates.getStart() : report.getStartTime()));
		model.put("reportEnd", m_subtitleFormat.format(historyMode ? historyDates.getDisplayEnd() : report.getEndTime()));
		model.put("ips", ips);
		model.put("ipToHostnameStr", new JsonBuilder().toJson(ipToHostname(ips)));
		model.put("groups", m_configManager.queryDomainGroup(domain));
		model.put("group", group);
		model.put("groupIps", m_configManager.queryIpByDomainAndGroup(domain, group));
		model.put("domainGroups", domainGroups());
		model.put("navs", UrlNav.values());
		model.put("baseUri", contextPath + "/mvc/r/p");
		model.put("sample", sample(report.getDomain()));
		model.put("queryString", queryString(urlThreshold, sqlThreshold, serviceThreshold, cacheThreshold, callThreshold));
		model.put("urlThreshold", urlThreshold);
		model.put("sqlThreshold", sqlThreshold);
		model.put("serviceThreshold", serviceThreshold);
		model.put("cacheThreshold", cacheThreshold);
		model.put("callThreshold", callThreshold);
		model.put("navPrefix", "op=view&domain=" + report.getDomain() + "&ip=" + ipAddress
				+ queryString(urlThreshold, sqlThreshold, serviceThreshold, cacheThreshold, callThreshold));
		model.put("historyMode", historyMode);
		model.put("historyNavs", HistoryNav.values());
		model.put("currentNav", HistoryNav.getByName(reportType));
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
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

	private void buildHourlyGraphs(Map<String, Object> model, ProblemReport report, String ipAddress, String type,
			String status) {
		HourlyLineChartVisitor visitor = new HourlyLineChartVisitor(ipAddress, type, status, report.getStartTime());

		visitor.visitProblemReport(report);
		model.put("errorsTrend", new JsonBuilder().toJson(visitor.getGraphItem()));
		buildDistributionChart(model, report, ipAddress, type, status);
	}

	private void buildDistributionChart(Map<String, Object> model, ProblemReport report, String ipAddress, String type,
			String status) {
		if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
			PieGraphChartVisitor pieChart = new PieGraphChartVisitor(type, status);

			pieChart.visitProblemReport(report);
			model.put("distributionChart", pieChart.getPieChart().getJsonString());
		}
	}

	private void buildHistoryGraphs(Map<String, Object> model, ProblemReport report, String ipAddress, String type,
			String status, HistoryDates dates) {
		buildDistributionChart(model, report, ipAddress, type, status);
		buildProblemTrendGraph(model, report, ipAddress, type, status, dates);
	}

	private void buildProblemTrendGraph(Map<String, Object> model, ProblemReport report, String ipAddress, String type,
			String status, HistoryDates dates) {
		TrendData data = findTrendData(report, ipAddress, type, status);
		long step = historyGraphStep(dates.getReportType()) * data.getDuration();

		model.put("errorsTrend", buildHistoryLineChart(dates.getStart(), dates.getEnd(),
				historyGraphErrorTitle(dates.getReportType()), step, data.getValues()).getJsonString());
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

	private String defaultSqlThreshold(String domain) {
		Map<String, Domain> domains = m_serverConfigManager.getLongConfigDomains();
		Domain config = domains.get(domain);

		if (config != null) {
			int threshold = config.getSqlThreshold();

			if (threshold != 100 && threshold != 500 && threshold != 1000) {
				NumberFormat format = new DecimalFormat("#");

				return "<option value=\"" + threshold + "\">" + format.format(threshold) + " ms</option>";
			}
		}
		return "";
	}

	private String defaultUrlThreshold(String domain) {
		Map<String, Domain> domains = m_serverConfigManager.getLongConfigDomains();
		Domain config = domains.get(domain);

		if (config != null) {
			int threshold = config.getUrlThreshold() == null ? m_serverConfigManager.getLongUrlDefaultThreshold()
					: config.getUrlThreshold();

			if (threshold != 500 && threshold != 1000 && threshold != 2000 && threshold != 3000 && threshold != 4000
					&& threshold != 5000) {
				NumberFormat format = new DecimalFormat("#.##");

				return "<option value=\"" + threshold + "\">" + format.format(threshold / 1000.0) + " Sec</option>";
			}
		}
		return "";
	}

	private Map<String, Department> domainGroups() {
		Collection<String> domains = m_projectService.findAllDomains();

		return m_projectService.findDepartments(domains);
	}

	private String emptyToNull(String value) {
		return value == null || value.length() == 0 ? null : value;
	}

	private ProblemReport filterReportByGroup(ProblemReport report, String domain, String group) {
		List<String> ips = m_configManager.queryIpByDomainAndGroup(domain, group);
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

	private TrendData findTrendData(ProblemReport report, String ipAddress, String type, String status) {
		TrendData result = new TrendData();

		if (report == null || StringUtils.isEmpty(type)) {
			return result;
		}
		for (Machine machine : report.getMachines().values()) {
			if (Constants.ALL.equalsIgnoreCase(ipAddress) || ipAddress.equalsIgnoreCase(machine.getIp())) {
				for (Entity entity : machine.getEntities().values()) {
					if (matches(entity, type, status)) {
						result.add(entity.getGraphTrend());
					}
				}
			}
		}
		return result;
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
		return "day".equals(reportType) ? "閿欒閲?(count/min)" : "閿欒閲?(count/day)";
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

	private boolean isHourlyGraphAction(String action) {
		return "hourlyGraph".equals(action) || "groupGraphs".equals(action);
	}

	private boolean matches(Entity entity, String type, String status) {
		if (!type.equalsIgnoreCase(entity.getType())) {
			return false;
		}
		return StringUtils.isEmpty(status) || status.equalsIgnoreCase(entity.getStatus());
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
			}
		}
		return result;
	}

	private ProblemReport queryHourlyReport(String domain, String ipAddress, String type, String status, long date,
			String queryType) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("queryType", queryType);

		if (!Constants.ALL.equals(ipAddress)) {
			request.setProperty("ip", ipAddress);
		}
		if (!StringUtils.isEmpty(type)) {
			request.setProperty("type", type);
		}
		if (!StringUtils.isEmpty(status)) {
			request.setProperty("name", status);
		}
		if (m_problemService.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_problemService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private ProblemReport queryHistoryReport(String domain, HistoryDates dates) {
		return m_reportService.queryReport(domain, dates.getStart(), dates.getEnd());
	}

	private String queryString(int urlThreshold, int sqlThreshold, int serviceThreshold, int cacheThreshold,
			int callThreshold) {
		return "&urlThreshold=" + urlThreshold + "&sqlThreshold=" + sqlThreshold + "&serviceThreshold="
				+ serviceThreshold + "&cacheThreshold=" + cacheThreshold + "&callThreshold=" + callThreshold;
	}

	private double sample(String domain) {
		SampleConfig config = m_sampleConfigManager.getConfig();
		com.dianping.cat.sample.entity.Domain sampleDomain = config.findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}

	@SuppressWarnings("unused")
	private String encode(String value) {
		return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
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

		private Date getStart() {
			return m_start;
		}
	}

	private class TrendData {
		private int m_duration = 1;

		private double[] m_values = new double[0];

		private void add(GraphTrend graphTrend) {
			if (graphTrend == null) {
				return;
			}
			m_duration = graphTrend.getDuration() <= 0 ? 1 : graphTrend.getDuration();
			double[] values = parseDoubles(graphTrend.getFails());

			if (values.length > m_values.length) {
				double[] result = new double[values.length];

				System.arraycopy(m_values, 0, result, 0, m_values.length);
				m_values = result;
			}
			for (int i = 0; i < values.length; i++) {
				m_values[i] += values[i];
			}
		}

		private int getDuration() {
			return m_duration;
		}

		private double[] getValues() {
			return m_values;
		}
	}
}
