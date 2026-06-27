package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.business.Type;
import com.dianping.cat.report.page.business.graph.BusinessGraphCreator;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcBusinessReportController {
	private final SimpleDateFormat minuteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Resource
	private BusinessGraphCreator businessGraphCreator;

	@Resource
	private ProjectService projectService;

	@Resource
	private BusinessTagConfigManager businessTagConfigManager;

	@GetMapping("/mvc/r/business")
	public void business(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = businessModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/business/business.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> businessModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String typeValue = parameter(request, "type", Type.Domain.getName());
		String name = parameter(request, "name", Constants.CAT);
		int timeRange = intParameter(request, "timeRange", 4);
		int step = intParameter(request, "step", 0);
		Type type = Type.getType(typeValue, Type.Domain);
		Date endTime = endDate(request.getParameter("endDate"), step);
		Date startTime = startDate(request.getParameter("startDate"), endTime, timeRange, step);
		Map<String, LineChart> charts = Type.Tag == type ? businessGraphCreator.buildGraphByTag(startTime, endTime, name)
				: businessGraphCreator.buildGraphByDomain(startTime, endTime, name);

		model.put("contextPath", contextPath);
		model.put("name", name);
		model.put("domain", Type.Domain == type ? name : Constants.CAT);
		model.put("displayDomain", Type.Domain == type ? name : Constants.CAT);
		model.put("type", type.getName());
		model.put("timeRange", timeRange);
		model.put("startTime", minuteFormat.format(startTime));
		model.put("endTime", minuteFormat.format(endTime));
		model.put("lineCharts", new ArrayList<LineChart>(charts.values()));
		model.put("domains", projectService.findAllDomains());
		model.put("tags", businessTagConfigManager.findAllTags());
		model.put("ranges", Arrays.asList(new RangeOption("1小时", 1), new RangeOption("2小时", 2),
				new RangeOption("4小时", 4), new RangeOption("6小时", 6), new RangeOption("8小时", 8),
				new RangeOption("12小时", 12), new RangeOption("24小时", 24), new RangeOption("48小时", 48)));
		model.put("navs", UrlNav.values());
		model.put("baseUri", contextPath + "/mvc/r/business");
		return model;
	}

	private Date endDate(String value, int step) {
		Date end = null;

		if (value != null && value.length() > 0) {
			try {
				end = minuteFormat.parse(value);
			} catch (ParseException e) {
				end = null;
			}
		}
		if (end == null) {
			end = TimeHelper.getCurrentHour(1);
		}
		return alignHour(end, step);
	}

	private Date startDate(String value, Date end, int timeRange, int step) {
		Date start = null;

		if (value != null && value.length() > 0) {
			try {
				start = minuteFormat.parse(value);
			} catch (ParseException e) {
				start = null;
			}
		}
		if (start == null) {
			start = new Date(end.getTime() - TimeHelper.ONE_HOUR * timeRange);
			return start;
		}
		return alignHour(start, step);
	}

	private Date alignHour(Date date, int step) {
		long time = date.getTime();

		return new Date(time - time % TimeHelper.ONE_HOUR + step * TimeHelper.ONE_HOUR);
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

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	public static class RangeOption {
		private final String m_title;

		private final int m_duration;

		public RangeOption(String title, int duration) {
			m_title = title;
			m_duration = duration;
		}

		public int getDuration() {
			return m_duration;
		}

		public String getTitle() {
			return m_title;
		}
	}
}
