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
import com.dianping.cat.report.page.cross.CrossMethodVisitor;
import com.dianping.cat.report.page.cross.display.HostInfo;
import com.dianping.cat.report.page.cross.display.MethodInfo;
import com.dianping.cat.report.page.cross.display.MethodQueryInfo;
import com.dianping.cat.report.page.cross.display.MethodQueryInfo.Item;
import com.dianping.cat.report.page.cross.display.TypeDetailInfo;
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
import lombok.Data;
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
	private JsonBuilder jsonBuilder;

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
		String action = parameter(request, "op", "view");

		if ("vueData".equals(action)) {
			writeJson(response, jsonBuilder.toJson(vueCrossReport(request)));
			return;
		}

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

		if ("vueData".equals(action)) {
			action = parameter(request, "vueAction", "view");
		}
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String callSort = parameter(request, "callSort", "avg");
		String serviceSort = parameter(request, "serviceSort", "avg");
		String method = parameter(request, "method", "");
		String project = parameter(request, "project", "All");
		String remoteIp = parameter(request, "remote", "");
		String queryName = parameter(request, "queryName", "");
		boolean queryAction = isQueryAction(action);
		boolean historyMode = isHistoryAction(action) || isQueryHistory(request.getParameter("date"));
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
		MethodQueryInfo queryInfo = null;

		if (queryAction) {
			CrossMethodVisitor visitor = new CrossMethodVisitor(method);

			visitor.visitCrossReport(report);
			queryInfo = visitor.getInfo();
		} else if (isHostAction(action)) {
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
		model.put("queryInfo", queryInfo);
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

	private VueCrossReport vueCrossReport(HttpServletRequest request) {
		Map<String, Object> model = crossModel(request);
		VueCrossReport report = new VueCrossReport();
		ProjectInfo projectInfo = (ProjectInfo) model.get("projectInfo");
		MethodQueryInfo queryInfo = (MethodQueryInfo) model.get("queryInfo");
		@SuppressWarnings("unchecked")
		List<String> ips = (List<String>) model.get("ips");
		@SuppressWarnings("unchecked")
		Map<String, Department> domainGroups = (Map<String, Department>) model.get("domainGroups");

		report.setAction((String) model.get("action"));
		report.setCallSort((String) model.get("callSort"));
		report.setContextPath((String) model.get("contextPath"));
		report.setDate((String) model.get("date"));
		report.setDisplayDomain((String) model.get("displayDomain"));
		report.setDomain((String) model.get("domain"));
		report.setDomainGroups(vueDomainGroups(domainGroups));
		report.setHistoryMode((Boolean) model.get("historyMode"));
		report.setIpAddress((String) model.get("ipAddress"));
		report.setIpToHostname(ipToHostname(ips == null ? new ArrayList<String>() : ips));
		report.setIps(ips == null ? new ArrayList<String>() : ips);
		report.setLongDate((Long) model.get("longDate"));
		report.setMethod((String) model.get("method"));
		report.setProject((String) model.get("project"));
		report.setQueryName((String) model.get("queryName"));
		report.setRemoteIp((String) model.get("remoteIp"));
		report.setReportEnd((String) model.get("reportEnd"));
		report.setReportStart((String) model.get("reportStart"));
		report.setReportType((String) model.get("reportType"));
		report.setSample((Double) model.get("sample"));
		report.setServiceSort((String) model.get("serviceSort"));

		if (projectInfo != null) {
			Map<String, TypeDetailInfo> callerInfo = projectInfo.getCallerProjectsInfo();

			report.setCallProjects(vueTypeRows(projectInfo.getCallProjectsInfo()));
			report.setServiceProjects(vueTypeRows(projectInfo.getServiceProjectsInfo()));
			report.setCallerProjects(vueTypeMap(callerInfo));
		}
		if (queryInfo != null) {
			report.setQueryItems(vueQueryRows(queryInfo.getItems()));
		}
		return report;
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

	private List<VueDomainDepartment> vueDomainGroups(Map<String, Department> domainGroups) {
		List<VueDomainDepartment> departments = new ArrayList<VueDomainDepartment>();

		if (domainGroups == null) {
			return departments;
		}
		for (Map.Entry<String, Department> departmentEntry : domainGroups.entrySet()) {
			VueDomainDepartment department = new VueDomainDepartment();

			department.setName(departmentEntry.getKey());
			for (Map.Entry<String, ProjectService.ProjectLine> lineEntry : departmentEntry.getValue().getProjectLines()
					.entrySet()) {
				VueDomainLine line = new VueDomainLine();

				line.setName(lineEntry.getKey());
				line.setDomains(lineEntry.getValue().getLineDomains());
				department.getLines().add(line);
			}
			departments.add(department);
		}
		return departments;
	}

	private VueCrossQueryRow vueQueryRow(Item item) {
		VueCrossQueryRow row = new VueCrossQueryRow();

		row.setAvg(item.getAvg());
		row.setDomain(item.getDomain());
		row.setFailureCount(item.getFailureCount());
		row.setFailurePercent(item.getFailurePercent());
		row.setIp(item.getIp());
		row.setMethod(item.getMethod());
		row.setTotalCount(item.getTotalCount());
		row.setTps(item.getTps());
		row.setType(item.getType());
		return row;
	}

	private List<VueCrossQueryRow> vueQueryRows(Collection<Item> items) {
		List<VueCrossQueryRow> rows = new ArrayList<VueCrossQueryRow>();

		if (items == null) {
			return rows;
		}
		for (Item item : items) {
			rows.add(vueQueryRow(item));
		}
		return rows;
	}

	private Map<String, VueCrossTypeRow> vueTypeMap(Map<String, TypeDetailInfo> infos) {
		Map<String, VueCrossTypeRow> rows = new LinkedHashMap<String, VueCrossTypeRow>();

		if (infos == null) {
			return rows;
		}
		for (Map.Entry<String, TypeDetailInfo> entry : infos.entrySet()) {
			rows.put(entry.getKey(), vueTypeRow(entry.getValue()));
		}
		return rows;
	}

	private VueCrossTypeRow vueTypeRow(TypeDetailInfo info) {
		VueCrossTypeRow row = new VueCrossTypeRow();

		row.setAvg(info.getAvg());
		row.setFailureCount(info.getFailureCount());
		row.setFailurePercent(info.getFailurePercent());
		row.setIp(info.getIp());
		row.setProjectName(info.getProjectName());
		row.setTotalCount(info.getTotalCount());
		row.setTps(info.getTps());
		row.setType(info.getType());
		return row;
	}

	private List<VueCrossTypeRow> vueTypeRows(Collection<TypeDetailInfo> infos) {
		List<VueCrossTypeRow> rows = new ArrayList<VueCrossTypeRow>();

		if (infos == null) {
			return rows;
		}
		for (TypeDetailInfo info : infos) {
			rows.add(vueTypeRow(info));
		}
		return rows;
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

	private boolean isQueryAction(String action) {
		return "query".equals(action);
	}

	private boolean isQueryHistory(String date) {
		return date != null && date.length() == 8;
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

		if (crossModelService.isEligible(request)) {
			ModelResponse<CrossReport> response = crossModelService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private CrossReport queryHistoryReport(String domain, HistoryDates dates) {
		return crossReportService.queryReport(domain, dates.getStart(), dates.getEnd());
	}

	void setCrossModelService(ModelService<CrossReport> crossModelService) {
		this.crossModelService = crossModelService;
	}

	void setCrossReportService(CrossReportService crossReportService) {
		this.crossReportService = crossReportService;
	}

	void setDomainGroupConfigManager(DomainGroupConfigManager domainGroupConfigManager) {
		this.domainGroupConfigManager = domainGroupConfigManager;
	}

	void setHostinfoService(HostinfoService hostinfoService) {
		this.hostinfoService = hostinfoService;
	}

	void setJsonBuilder(JsonBuilder jsonBuilder) {
		this.jsonBuilder = jsonBuilder;
	}

	void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	void setSampleConfigManager(SampleConfigManager sampleConfigManager) {
		this.sampleConfigManager = sampleConfigManager;
	}

	private double sample(String domain) {
		Domain sampleDomain = sampleConfigManager.getConfig().findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}

	private void writeJson(HttpServletResponse response, String body) throws IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(body == null ? "" : body);
	}

	@Data
	public static class VueCrossQueryRow {
		private double avg;

		private String domain;

		private long failureCount;

		private double failurePercent;

		private String ip;

		private String method;

		private long totalCount;

		private double tps;

		private String type;
	}

	@Data
	public static class VueCrossReport {
		private String action;

		private String callSort;

		private List<VueCrossTypeRow> callProjects = new ArrayList<VueCrossTypeRow>();

		private Map<String, VueCrossTypeRow> callerProjects = new LinkedHashMap<String, VueCrossTypeRow>();

		private String contextPath;

		private String date;

		private String displayDomain;

		private String domain;

		private List<VueDomainDepartment> domainGroups = new ArrayList<VueDomainDepartment>();

		private boolean historyMode;

		private String ipAddress;

		private Map<String, String> ipToHostname = new LinkedHashMap<String, String>();

		private List<String> ips = new ArrayList<String>();

		private long longDate;

		private String method;

		private String project;

		private List<VueCrossQueryRow> queryItems = new ArrayList<VueCrossQueryRow>();

		private String queryName;

		private String remoteIp;

		private String reportEnd;

		private String reportStart;

		private String reportType;

		private double sample;

		private String serviceSort;

		private List<VueCrossTypeRow> serviceProjects = new ArrayList<VueCrossTypeRow>();
	}

	@Data
	public static class VueCrossTypeRow {
		private double avg;

		private long failureCount;

		private double failurePercent;

		private String ip;

		private String projectName;

		private long totalCount;

		private double tps;

		private String type;
	}

	@Data
	public static class VueDomainDepartment {
		private List<VueDomainLine> lines = new ArrayList<VueDomainLine>();

		private String name;
	}

	@Data
	public static class VueDomainLine {
		private List<String> domains = new ArrayList<String>();

		private String name;
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
