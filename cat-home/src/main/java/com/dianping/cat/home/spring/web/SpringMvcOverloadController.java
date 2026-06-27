package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.overload.task.OverloadReport;
import com.dianping.cat.report.page.overload.task.TableCapacityService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcOverloadController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcOverloadController.class);

	private final DateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Resource
	private TableCapacityService m_tableCapacityService;

	@GetMapping("/mvc/r/overload")
	public void overload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = overloadModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/overload/overload.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> overloadModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		Date startTime = startTime(request.getParameter("startTime"));
		Date endTime = endTime(request.getParameter("endTime"));
		List<OverloadReport> reports;

		try {
			reports = m_tableCapacityService.queryOverloadReports(startTime, endTime);
		} catch (RuntimeException e) {
			LOGGER.error("Unable to query overload reports, startTime={}, endTime={}.", startTime, endTime, e);
			Cat.logError(e);
			reports = java.util.Collections.emptyList();
		}

		model.put("contextPath", request.getContextPath());
		model.put("actionName", "view");
		model.put("domain", "cat");
		model.put("ipAddress", "All");
		model.put("reportType", "day");
		model.put("activeReport", "Overload");
		model.put("startTime", startTime);
		model.put("endTime", endTime);
		model.put("fullScreen", booleanParameter(request, "fullScreen", false));
		model.put("showHourly", booleanParameter(request, "showHourly", true));
		model.put("showDaily", booleanParameter(request, "showDaily", true));
		model.put("showWeekly", booleanParameter(request, "showWeekly", true));
		model.put("showMonthly", booleanParameter(request, "showMonthly", true));
		model.put("reports", reports);
		return model;
	}

	void setTableCapacityService(TableCapacityService tableCapacityService) {
		m_tableCapacityService = tableCapacityService;
	}

	private boolean booleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	private Date endTime(String value) {
		try {
			return parse(value);
		} catch (ParseException e) {
			LOGGER.warn("Unable to parse overload endTime, value={}. Use current time.", value);
			return new Date();
		}
	}

	private Date parse(String value) throws ParseException {
		if (value == null || value.length() == 0) {
			throw new ParseException("empty date", 0);
		}
		synchronized (m_format) {
			return m_format.parse(value);
		}
	}

	private Date startTime(String value) {
		try {
			return parse(value);
		} catch (ParseException e) {
			LOGGER.warn("Unable to parse overload startTime, value={}. Use one day ago.", value);
			return new Date(System.currentTimeMillis() - TimeHelper.ONE_DAY);
		}
	}
}
