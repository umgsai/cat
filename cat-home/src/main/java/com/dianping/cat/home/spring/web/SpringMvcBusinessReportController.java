package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.business.Type;
import com.dianping.cat.report.page.business.graph.BusinessGraphCreator;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcBusinessReportController {
	private final SimpleDateFormat minuteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Resource
	private BusinessGraphCreator businessGraphCreator;

	@Resource
	private JsonBuilder jsonBuilder;

	@Resource
	private ProjectService projectService;

	@Resource
	private BusinessTagConfigManager businessTagConfigManager;

	@GetMapping("/mvc/r/business")
	public void business(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = parameter(request, "op", "view");

		if ("vueData".equals(action)) {
			writeJson(response, jsonBuilder.toJson(vueBusinessReport(request)));
			return;
		}

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

	private VueBusinessChart vueChart(LineChart chart) {
		VueBusinessChart result = new VueBusinessChart();

		result.setDatas(chart.getDatas());
		result.setHtmlTitle(chart.getHtmlTitle());
		result.setId(chart.getId());
		result.setStart(chart.getStart());
		result.setStep(chart.getStep());
		result.setSubTitles(chart.getSubTitles());
		result.setTitle(chart.getTitle());
		result.setUnit(chart.getUnit());
		return result;
	}

	private VueBusinessReport vueBusinessReport(HttpServletRequest request) {
		Map<String, Object> model = businessModel(request);
		VueBusinessReport report = new VueBusinessReport();
		@SuppressWarnings("unchecked")
		List<LineChart> lineCharts = (List<LineChart>) model.get("lineCharts");
		@SuppressWarnings("unchecked")
		Collection<String> domains = (Collection<String>) model.get("domains");
		@SuppressWarnings("unchecked")
		Collection<String> tags = (Collection<String>) model.get("tags");
		@SuppressWarnings("unchecked")
		List<RangeOption> ranges = (List<RangeOption>) model.get("ranges");

		report.setContextPath((String) model.get("contextPath"));
		report.setDomain((String) model.get("domain"));
		report.setDisplayDomain((String) model.get("displayDomain"));
		report.setEndTime((String) model.get("endTime"));
		report.setName((String) model.get("name"));
		report.setStartTime((String) model.get("startTime"));
		report.setTimeRange((Integer) model.get("timeRange"));
		report.setType((String) model.get("type"));
		report.setDomains(domains == null ? new ArrayList<String>() : new ArrayList<String>(domains));
		report.setTags(tags == null ? new ArrayList<String>() : new ArrayList<String>(tags));
		if (lineCharts != null) {
			for (LineChart chart : lineCharts) {
				report.getLineCharts().add(vueChart(chart));
			}
		}
		if (ranges != null) {
			for (RangeOption range : ranges) {
				VueRangeOption option = new VueRangeOption();

				option.setDuration(range.getDuration());
				option.setTitle(range.getTitle());
				report.getRanges().add(option);
			}
		}
		for (UrlNav nav : UrlNav.values()) {
			VueUrlNav vueNav = new VueUrlNav();

			vueNav.setHours(nav.getHours());
			vueNav.setTitle(nav.getTitle());
			report.getNavs().add(vueNav);
		}
		return report;
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

	private void writeJson(HttpServletResponse response, String body) throws IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(body == null ? "" : body);
	}

	@Data
	public static class VueBusinessChart {
		private List<Map<Long, Double>> datas = new ArrayList<Map<Long, Double>>();

		private String htmlTitle;

		private String id;

		private String start;

		private long step;

		private List<String> subTitles = new ArrayList<String>();

		private String title;

		private String unit;
	}

	@Data
	public static class VueBusinessReport {
		private List<VueBusinessChart> lineCharts = new ArrayList<VueBusinessChart>();

		private String contextPath;

		private String displayDomain;

		private String domain;

		private List<String> domains = new ArrayList<String>();

		private String endTime;

		private String name;

		private List<VueUrlNav> navs = new ArrayList<VueUrlNav>();

		private List<VueRangeOption> ranges = new ArrayList<VueRangeOption>();

		private String startTime;

		private List<String> tags = new ArrayList<String>();

		private int timeRange;

		private String type;
	}

	@Data
	public static class VueRangeOption {
		private int duration;

		private String title;
	}

	@Data
	public static class VueUrlNav {
		private int hours;

		private String title;
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
