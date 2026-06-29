package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.page.matrix.DisplayMatrix;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcMatrixController {
	private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private MatrixReportService matrixReportService;

	@Resource(name = "matrixModelService")
	private ModelService<MatrixReport> matrixModelService;

	@Resource
	private ProjectService projectService;

	@GetMapping("/mvc/r/matrix")
	public void matrix(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = matrixModel(request);
		String view = Boolean.TRUE.equals(model.get("historyMode")) ? "/jsp/spring/report/matrix/matrixHistoryReport.jsp"
				: "/jsp/spring/report/matrix/matrix.jsp";

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> matrixModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String sort = request.getParameter("sort");
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));
		MatrixReport report = historyMode ? queryHistoryReport(domain, historyDates) : queryHourlyReport(domain, ipAddress, date);

		reportType = historyMode ? historyDates.getReportType() : reportType;
		if (report == null) {
			report = emptyReport(domain, historyMode ? historyDates.getStart() : new Date(date),
					historyMode ? historyDates.getEnd() : new Date(date + TimeHelper.ONE_HOUR - 1));
		}

		model.put("action", historyMode ? "history" : "view");
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain() == null ? domain : report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("date", historyMode ? dayFormat.format(new Date(date)) : hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("matrix", new DisplayMatrix(report).setSortBy(sort));
		model.put("sort", sort);
		model.put("historyMode", historyMode);
		model.put("navs", UrlNav.values());
		model.put("historyNavs", HistoryNav.values());
		model.put("currentNav", HistoryNav.getByName(reportType));
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
		model.put("reportStart", subtitleFormat.format(historyMode ? historyDates.getStart() : report.getStartTime()));
		model.put("reportEnd", subtitleFormat.format(historyMode ? historyDates.getDisplayEnd() : report.getEndTime()));
		model.put("navPrefix", "domain=" + domain + "&ip=" + ipAddress);
		model.put("activeReport", "Matrix");
		model.put("baseUri", contextPath + "/mvc/r/matrix");
		model.put("domainGroups", domainGroups());
		return model;
	}

	void setMatrixModelService(ModelService<MatrixReport> service) {
		matrixModelService = service;
	}

	void setMatrixReportService(MatrixReportService service) {
		matrixReportService = service;
	}

	void setProjectService(ProjectService service) {
		projectService = service;
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

	private MatrixReport emptyReport(String domain, Date start, Date end) {
		MatrixReport report = new MatrixReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	private Map<String, Department> domainGroups() {
		if (projectService == null) {
			return Collections.emptyMap();
		}

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

	private MatrixReport queryHistoryReport(String domain, HistoryDates dates) {
		return matrixReportService.queryReport(domain, dates.getStart(), dates.getEnd());
	}

	private MatrixReport queryHourlyReport(String domain, String ipAddress, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("ip", ipAddress);

		if (matrixModelService.isEligible(request)) {
			ModelResponse<MatrixReport> response = matrixModelService.invoke(request);

			return response.getModel();
		}
		return null;
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
