package com.dianping.cat.home.spring.web;

import java.io.IOException;
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
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.cross.display.HostInfo;
import com.dianping.cat.report.page.cross.display.MethodInfo;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcCrossController {
	private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private DomainGroupConfigManager domainGroupConfigManager;

	@Resource
	private HostinfoService hostinfoService;

	@Resource
	private ProjectService projectService;

	@Resource
	private SampleConfigManager sampleConfigManager;

	@Resource
	private CrossReportService crossReportService;

	@Resource(name = "crossModelService")
	private ModelService<CrossReport> crossModelService;

	@GetMapping("/mvc/r/cross")
	public void cross(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = crossModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/cross/cross.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> crossModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String callSort = parameter(request, "callSort", "avg");
		String serviceSort = parameter(request, "serviceSort", "avg");
		String method = parameter(request, "method", "");
		String project = parameter(request, "project", "All");
		String remoteIp = parameter(request, "remote", "");
		String queryName = parameter(request, "queryName", "");
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));

		reportType = historyMode ? historyDates.getReportType() : reportType;
		action = normalizeAction(action, project, remoteIp);
		CrossReport report = historyMode ? queryHistoryReport(domain, historyDates) : queryHourlyReport(domain, ipAddress, date);

		if (report == null) {
			report = new CrossReport(domain);
			report.setStartTime(historyMode ? historyDates.getStart() : new Date(date));
			report.setEndTime(historyMode ? historyDates.getEnd() : new Date(date + TimeHelper.ONE_HOUR));
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : SortHelper.sortIpAddress(report.getIps());
		long duration = historyMode ? historyDates.getDurationSeconds() : hourDuration(date);
		ProjectInfo projectInfo = null;
		HostInfo hostInfo = null;
		MethodInfo methodInfo = null;

		if (isHostAction(action)) {
			hostInfo = new HostInfo(duration);
			hostInfo.setHostinfoService(hostinfoService);
			hostInfo.setClientIp(ipAddress).setCallSortBy(callSort).setServiceSortBy(serviceSort);
			hostInfo.setProjectName(project);
			hostInfo.visitCrossReport(report);
		} else if (isMethodAction(action)) {
			methodInfo = new MethodInfo(duration);
			methodInfo.setHostinfoService(hostinfoService);
			methodInfo.setClientIp(ipAddress).setCallSortBy(callSort).setServiceSortBy(serviceSort);
			methodInfo.setRemoteProject(project);
			methodInfo.setRemoteIp(remoteIp).setQuery(queryName);
			methodInfo.visitCrossReport(report);
		} else {
			projectInfo = new ProjectInfo(duration);
			projectInfo.setClientIp(ipAddress).setCallSortBy(callSort).setServiceSortBy(serviceSort);
			projectInfo.visitCrossReport(report);
		}

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("date", historyMode ? dayFormat.format(new Date(date)) : hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", subtitleFormat.format(historyMode ? historyDates.getStart() : report.getStartTime()));
		model.put("reportEnd", subtitleFormat.format(historyMode ? historyDates.getDisplayEnd() : report.getEndTime()));
		model.put("ips", ips);
		model.put("projectInfo", projectInfo);
		model.put("hostInfo", hostInfo);
		model.put("methodInfo", methodInfo);
		model.put("callSort", callSort);
		model.put("serviceSort", serviceSort);
		model.put("method", method);
		model.put("project", project);
		model.put("remoteIp", remoteIp);
		model.put("queryName", queryName);
		model.put("ipToHostnameStr", new JsonBuilder().toJson(ipToHostname(ips)));
		model.put("groups", domainGroupConfigManager.queryDomainGroup(domain));
		model.put("domainGroups", domainGroups());
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "ip=" + ipAddress + "&domain=" + report.getDomain() + "&callSort=" + callSort
				+ "&serviceSort=" + serviceSort);
		model.put("historyMode", historyMode);
		model.put("historyNavs", HistoryNav.values());
		model.put("currentNav", HistoryNav.getByName(reportType));
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
		model.put("baseUri", contextPath + "/mvc/r/cross");
		model.put("sample", sample(report.getDomain()));
		return model;
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

	private HistoryDates historyDates(HttpServletRequest request, String reportType) {
		String normalizedReportType = normalizeReportType(reportType);
		long date = normalizeHistoryDate(dateParameter(request.getParameter("date")).getTime(), normalizedReportType,
				intParameter(request, "step", 0));
		Date start = historyStartDate(date, request.getParameter("startDate"));
		Date end = historyEndDate(date, normalizedReportType, request.getParameter("endDate"));

		return new HistoryDates(start.getTime(), normalizedReportType, start, end);
	}

	private long hourDuration(long date) {
		long currentHour = System.currentTimeMillis() - System.currentTimeMillis() % TimeHelper.ONE_HOUR;

		if (date == currentHour) {
			return System.currentTimeMillis() % TimeHelper.ONE_HOUR / 1000;
		}
		return TimeHelper.ONE_HOUR / 1000;
	}

	private boolean isHistoryAction(String action) {
		return action != null && action.startsWith("history");
	}

	private boolean isHostAction(String action) {
		return "host".equals(action) || "historyHost".equals(action);
	}

	private boolean isMethodAction(String action) {
		return "method".equals(action) || "historyMethod".equals(action);
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

	private String normalizeAction(String action, String project, String remoteIp) {
		if ("host".equals(action) && (project == null || project.length() == 0 || "All".equals(project))) {
			return "view";
		}
		if ("historyHost".equals(action) && (project == null || project.length() == 0 || "All".equals(project))) {
			return "history";
		}
		if ("method".equals(action) && (remoteIp == null || remoteIp.length() == 0)) {
			return "view";
		}
		if ("historyMethod".equals(action) && (remoteIp == null || remoteIp.length() == 0)) {
			return "history";
		}
		return action;
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

	private CrossReport queryHourlyReport(String domain, String ipAddress, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("ip", ipAddress);

		if (crossModelService.isEligable(request)) {
			ModelResponse<CrossReport> response = crossModelService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private CrossReport queryHistoryReport(String domain, HistoryDates dates) {
		return crossReportService.queryReport(domain, dates.getStart(), dates.getEnd());
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

		private long getDurationSeconds() {
			return (m_end.getTime() - m_start.getTime()) / 1000;
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
