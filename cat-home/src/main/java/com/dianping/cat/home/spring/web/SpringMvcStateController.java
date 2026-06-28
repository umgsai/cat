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
	private StateReportService stateReportService;

	@Resource(name = "stateModelService")
	private ModelService<StateReport> stateModelService;

	@GetMapping("/mvc/r/state")
	public void state(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

		if (stateModelService.isEligable(request)) {
			ModelResponse<StateReport> response = stateModelService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private StateReport queryHistoryReport(HistoryDates dates) {
		return stateReportService.queryReport(Constants.CAT, dates.getStart(), dates.getEnd());
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
