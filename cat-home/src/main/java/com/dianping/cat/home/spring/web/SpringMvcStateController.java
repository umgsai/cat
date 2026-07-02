package com.dianping.cat.home.spring.web;

import java.io.IOException;
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
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.spring.view.state.StateGraphBuilder;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.state.StateBuilder;
import com.dianping.cat.report.page.state.StateDisplay;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcStateController {
	private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private ServerFilterConfigManager serverFilterConfigManager;

	@Resource
	private StateBuilder stateBuilder;

	@Resource(name = "springStateGraphBuilder")
	private StateGraphBuilder stateGraphBuilder;

	@Resource
	private JsonBuilder jsonBuilder;

	@Resource
	private StateReportService stateReportService;

	@Resource(name = "stateModelService")
	private ModelService<StateReport> stateModelService;

	@GetMapping("/mvc/r/state")
	public void state(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = parameter(request, "op", "view");

		if ("vueGraphData".equals(action)) {
			writeJson(response, jsonBuilder.toJson(vueStateGraph(request)));
			return;
		}
		if ("vueData".equals(action)) {
			writeJson(response, jsonBuilder.toJson(vueStateReport(request)));
			return;
		}

		Map<String, Object> model = stateModel(request);
		String view = isGraphAction((String) model.get("action")) ? "/jsp/spring/report/state/stateGraphs.jsp"
				: "/jsp/spring/report/state/state.jsp";

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> stateModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");

		if ("vueData".equals(action) || "vueGraphData".equals(action)) {
			action = parameter(request, "vueAction", "view");
		}
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String sort = emptyToNull(request.getParameter("sort"));
		boolean show = !"false".equalsIgnoreCase(parameter(request, "show", "true"));
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));
		StateReport report = isHistoryGraphAction(action) ? emptyReport(historyDates)
				: historyMode ? queryHistoryReport(historyDates) : queryHourlyReport(ipAddress, date);

		if (report == null) {
			report = historyMode ? emptyReport(historyDates) : emptyReport(date);
		}
		reportType = historyMode ? historyDates.getReportType() : reportType;

		if ("graph".equals(action)) {
			buildGraph(model, report, ipAddress, date, request.getParameter("key"));
		} else if (isHistoryGraphAction(action)) {
			buildHistoryGraph(model, ipAddress, historyDates, request.getParameter("key"));
		} else {
			StateDisplay display = new StateDisplay(ipAddress, serverFilterConfigManager.getUnusedDomains());

			display.setSortType(sort);
			display.visitStateReport(report);
			model.put("state", display);
			if (!historyMode) {
				model.put("message", stateBuilder.buildStateMessage(date, ipAddress));
			}
		}

		List<String> ips = report.getMachines() == null ? new ArrayList<String>()
				: SortHelper.sortIpAddress(report.getMachines().keySet());

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", domain);
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("date", historyMode ? dayFormat.format(new Date(date)) : hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", subtitleFormat.format(historyMode ? historyDates.getStart() : report.getStartTime()));
		model.put("reportEnd", subtitleFormat.format(historyMode ? historyDates.getDisplayEnd() : report.getEndTime()));
		model.put("ips", ips);
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "domain=" + domain + "&ip=" + ipAddress + "&show=" + show);
		model.put("historyMode", historyMode);
		model.put("historyNavs", HistoryNav.values());
		model.put("currentNav", HistoryNav.getByName(reportType));
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
		model.put("show", show);
		model.put("sort", sort);
		model.put("baseUri", contextPath + "/mvc/r/state");
		return model;
	}

	private VueStateMetric vueMetric(String key, String title, double value, String remark, boolean warning) {
		VueStateMetric metric = new VueStateMetric();

		metric.setKey(key);
		metric.setTitle(title);
		metric.setValue(value);
		metric.setRemark(remark);
		metric.setWarning(warning);
		return metric;
	}

	private VueStateProcessDomain vueProcessDomain(ProcessDomain domain) {
		VueStateProcessDomain row = new VueStateProcessDomain();

		row.setAvgKb(domain.getAvg() / 1024.0);
		row.setIps(new ArrayList<String>(domain.getIps()));
		row.setMachineCount(domain.getIps().size());
		row.setName(domain.getName());
		row.setSizeGb(domain.getSize() / 1024.0 / 1024.0 / 1024.0);
		row.setTotal(domain.getTotal());
		row.setTotalLoss(domain.getTotalLoss());
		return row;
	}

	private VueStateReport vueStateReport(HttpServletRequest request) {
		Map<String, Object> model = stateModel(request);
		VueStateReport report = new VueStateReport();
		StateDisplay state = (StateDisplay) model.get("state");
		@SuppressWarnings("unchecked")
		List<String> ips = (List<String>) model.get("ips");

		report.setAction((String) model.get("action"));
		report.setContextPath((String) model.get("contextPath"));
		report.setDate((String) model.get("date"));
		report.setDisplayDomain((String) model.get("displayDomain"));
		report.setDomain((String) model.get("domain"));
		report.setHistoryMode((Boolean) model.get("historyMode"));
		report.setIpAddress((String) model.get("ipAddress"));
		report.setIps(ips == null ? new ArrayList<String>() : ips);
		report.setLongDate((Long) model.get("longDate"));
		report.setMessage((String) model.get("message"));
		report.setReportEnd((String) model.get("reportEnd"));
		report.setReportStart((String) model.get("reportStart"));
		report.setReportType((String) model.get("reportType"));
		report.setShow((Boolean) model.get("show"));
		report.setSort((String) model.get("sort"));

		for (UrlNav nav : UrlNav.values()) {
			VueUrlNav vueNav = new VueUrlNav();

			vueNav.setHours(nav.getHours());
			vueNav.setTitle(nav.getTitle());
			report.getNavs().add(vueNav);
		}
		for (HistoryNav nav : HistoryNav.values()) {
			VueHistoryNav vueNav = new VueHistoryNav();

			vueNav.setLast(nav.getLast());
			vueNav.setNext(nav.getNext());
			vueNav.setTitle(nav.getTitle());
			report.getHistoryNavs().add(vueNav);
		}

		if (state != null) {
			Machine machine = state.getMachine();

			report.setTotalSize(state.getTotalSize());
			report.getMetrics().add(vueMetric("total", "处理消息总量", machine.getTotal(), "服务器接受到消息总量", false));
			report.getMetrics().add(vueMetric("totalLoss", "丢失消息总量", machine.getTotalLoss(),
					"服务器进行encode以及analyze处理来不及而丢失消息总量", machine.getTotalLoss() > 0));
			report.getMetrics().add(vueMetric("avgTps", "每分钟平均处理数", machine.getAvgTps(), "平均每分钟处理消息量", false));
			report.getMetrics().add(vueMetric("maxTps", "单台机器每分钟最大处理数", machine.getMaxTps(),
					"单台机器平均每分钟最大处理消息数目", false));
			report.getMetrics().add(vueMetric("dump", "压缩成功消息数量", machine.getDump(), "将消息进行压缩消息数目", false));
			report.getMetrics().add(vueMetric("dumpLoss", "来不及压缩丢失消息数量", machine.getDumpLoss(),
					"将消息进行压缩，线程太忙而丢失消息丢失数目", machine.getDumpLoss() > 0));
			report.getMetrics().add(vueMetric("pigeonTimeError", "两台机器时钟不准导致消息存储丢失", machine.getPigeonTimeError(),
					"这个场景用于Pigeon，服务端id是由客户端产生，客户端和服务端时钟差2小时，会导致存储丢失", false));
			report.getMetrics().add(vueMetric("networkTimeError", "网络传输或者客户端延迟发送导致消息丢失",
					machine.getNetworkTimeError(), "CAT分小时处理，当一个小时过去了，默认会延迟3分钟结束当前小时，在3分钟后还接受上个小时消息，直接丢弃",
					false));
			report.getMetrics().add(vueMetric("blockTotal", "存储消息块数量", machine.getBlockTotal(),
					"CAT是分块存储，消息块成功放入存储队列", false));
			report.getMetrics().add(vueMetric("blockLoss", "存储消息块丢失数量", machine.getBlockLoss(),
					"将存储块写入磁盘的线程太忙，存储队列溢出的消息块数量", machine.getBlockLoss() > 0));
			report.getMetrics().add(vueMetric("blockTime", "存储消息块花费时间(分钟)", machine.getBlockTime() / 1000.0 / 60.0,
					"存储消息花费的CPU时间", false));
			report.getMetrics().add(vueMetric("size", "压缩前消息大小(GB)", machine.getSize() / 1024.0 / 1024.0 / 1024.0,
					"压缩前所有存储消息的总大小", false));
			report.getMetrics().add(vueMetric("delayAvg", "系统处理延迟(ms)", machine.getDelayAvg(),
					"客户端产生消息，到服务端存储之间的时钟误差。（在机器时钟完全准确的情况下）", false));
			for (ProcessDomain processDomain : state.getProcessDomains()) {
				report.getProcessDomains().add(vueProcessDomain(processDomain));
			}
		}
		return report;
	}

	private VueStateGraph vueStateGraph(HttpServletRequest request) {
		Map<String, Object> model = stateModel(request);
		VueStateGraph graph = new VueStateGraph();

		graph.setGraph((String) model.get("graph"));
		graph.setKey((String) model.get("key"));
		graph.setPieChart((String) model.get("pieChart"));
		return graph;
	}

	private void buildGraph(Map<String, Object> model, StateReport report, String ipAddress, long date, String key) {
		Pair<LineChart, PieChart> pair = stateGraphBuilder.buildGraph(Constants.CAT, ipAddress, key, report);

		model.put("key", key);
		model.put("graph", new JsonBuilder().toJson(pair.getKey()));
		model.put("pieChart", new JsonBuilder().toJson(pair.getValue()));
	}

	private void buildHistoryGraph(Map<String, Object> model, String ipAddress, HistoryDates dates, String key) {
		Pair<LineChart, PieChart> pair = stateGraphBuilder.buildGraph(Constants.CAT, dates.getStart(), dates.getEnd(),
				ipAddress, key);

		model.put("key", key);
		model.put("graph", new JsonBuilder().toJson(pair.getKey()));
		model.put("pieChart", new JsonBuilder().toJson(pair.getValue()));
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

	private String emptyToNull(String value) {
		return value == null || value.length() == 0 ? null : value;
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

	private StateReport emptyReport(long date) {
		StateReport report = new StateReport(Constants.CAT);

		report.setStartTime(new Date(date));
		report.setEndTime(new Date(date + TimeHelper.ONE_HOUR - 1));
		return report;
	}

	private StateReport emptyReport(HistoryDates dates) {
		StateReport report = new StateReport(Constants.CAT);

		report.setStartTime(dates.getStart());
		report.setEndTime(dates.getEnd());
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

	private boolean isGraphAction(String action) {
		return "graph".equals(action) || isHistoryGraphAction(action);
	}

	private boolean isHistoryAction(String action) {
		return action != null && action.startsWith("history");
	}

	private boolean isHistoryGraphAction(String action) {
		return "historyGraph".equals(action);
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

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
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

	private StateReport queryHourlyReport(String ipAddress, long date) {
		ModelRequest request = new ModelRequest(Constants.CAT, date).setProperty("ip", ipAddress);

		if (stateModelService.isEligible(request)) {
			ModelResponse<StateReport> response = stateModelService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private StateReport queryHistoryReport(HistoryDates dates) {
		return stateReportService.queryReport(Constants.CAT, dates.getStart(), dates.getEnd());
	}

	private void writeJson(HttpServletResponse response, String body) throws IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(body == null ? "" : body);
	}

	@Data
	public static class VueStateGraph {
		private String graph;

		private String key;

		private String pieChart;
	}

	@Data
	public static class VueHistoryNav {
		private String last;

		private String next;

		private String title;
	}

	@Data
	public static class VueStateMetric {
		private String key;

		private String remark;

		private String title;

		private double value;

		private boolean warning;
	}

	@Data
	public static class VueStateProcessDomain {
		private double avgKb;

		private List<String> ips = new ArrayList<String>();

		private int machineCount;

		private String name;

		private double sizeGb;

		private long total;

		private long totalLoss;
	}

	@Data
	public static class VueStateReport {
		private String action;

		private String contextPath;

		private String date;

		private String displayDomain;

		private String domain;

		private List<VueHistoryNav> historyNavs = new ArrayList<VueHistoryNav>();

		private boolean historyMode;

		private String ipAddress;

		private List<String> ips = new ArrayList<String>();

		private long longDate;

		private String message;

		private List<VueStateMetric> metrics = new ArrayList<VueStateMetric>();

		private List<VueUrlNav> navs = new ArrayList<VueUrlNav>();

		private List<VueStateProcessDomain> processDomains = new ArrayList<VueStateProcessDomain>();

		private String reportEnd;

		private String reportStart;

		private String reportType;

		private boolean show;

		private String sort;

		private int totalSize;
	}

	@Data
	public static class VueUrlNav {
		private int hours;

		private String title;
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
