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
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.dependency.TopExceptionExclude;
import com.dianping.cat.report.page.dependency.TopMetric;
import com.dianping.cat.report.page.state.StateBuilder;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcTopController {
	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private ExceptionRuleConfigManager exceptionRuleConfigManager;

	@Resource
	private DomainGroupConfigManager domainGroupConfigManager;

	@Resource
	private ProjectService projectService;

	@Resource
	private SampleConfigManager sampleConfigManager;

	@Resource
	private StateBuilder stateBuilder;

	@Resource
	private TopReportService topReportService;

	@Resource(name = "topModelService")
	private ModelService<TopReport> topModelService;

	@GetMapping("/mvc/r/top")
	public void top(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = topModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/top/top.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> topModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		long date = date(request.getParameter("date"));
		int step = intParameter(request, "step", 0);
		int minuteCount = intParameter(request, "count", 8);
		int topCount = intParameter(request, "tops", 11);

		if (step != 0) {
			date = date + step * TimeHelper.ONE_HOUR;
			long currentHour = System.currentTimeMillis() - System.currentTimeMillis() % TimeHelper.ONE_HOUR;

			if (date > currentHour) {
				date = currentHour;
			}
		}

		int minute = minute(request, date);
		int maxMinute = maxMinute(date);
		TopReport report = queryTopReport(date);
		Date end = new Date(date + TimeHelper.ONE_MINUTE * minute);
		Date start = new Date(end.getTime() - TimeHelper.ONE_MINUTE * minuteCount);
		TopMetric topMetric = new TopMetric(minuteCount, topCount, exceptionRuleConfigManager, Arrays.asList(Constants.FRONT_END));

		topMetric.setStart(start).setEnd(end);
		if (minuteCount > minute) {
			TopReport lastReport = queryTopReport(date - TimeHelper.ONE_HOUR);

			topMetric.visitTopReport(lastReport);
		}
		topMetric.visitTopReport(report);

		model.put("webapp", contextPath);
		model.put("domain", domain);
		model.put("ipAddress", ipAddress);
		model.put("date", hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("minute", minute);
		model.put("maxMinute", maxMinute);
		model.put("minutes", minutes());
		model.put("minuteCount", minuteCount);
		model.put("topCount", topCount);
		model.put("fullScreen", booleanParameter(request, "fullScreen"));
		model.put("refresh", booleanParameter(request, "refresh"));
		model.put("frequency", intParameter(request, "frequency", 10));
		model.put("reportStart", subtitleFormat.format(new Date(date)));
		model.put("reportEnd", subtitleFormat.format(new Date(date + TimeHelper.ONE_HOUR - 1)));
		model.put("message", stateBuilder.buildStateMessage(date, ipAddress));
		model.put("topReport", report);
		model.put("topMetric", topMetric);
		model.put("topResult", topMetric.getError().getResult());
		model.put("topResultView", topResultView(topMetric.getError().getResult()));
		model.put("domainGroups", domainGroups());
		model.put("groups", domainGroupConfigManager.queryDomainGroup(domain));
		model.put("navs", UrlNav.values());
		model.put("baseUri", contextPath + "/mvc/r/top");
		model.put("sample", sample(domain));
		model.put("homeUrl", contextPath + "/mvc/r/home");
		model.put("model", model);
		return model;
	}

	private boolean booleanParameter(HttpServletRequest request, String name) {
		return "true".equalsIgnoreCase(request.getParameter(name));
	}

	private long date(String value) {
		if (value != null && value.length() > 0) {
			try {
				Date date = value.length() == 10 ? hourlyFormat.parse(value) : new SimpleDateFormat("yyyyMMdd").parse(value);

				return date.getTime();
			} catch (ParseException e) {
				// use current hour
			}
		}

		long now = System.currentTimeMillis();

		return now - now % TimeHelper.ONE_HOUR;
	}

	private int intParameter(HttpServletRequest request, String name, int defaultValue) {
		String value = request.getParameter(name);

		if (value != null && value.length() > 0) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// use default value
			}
		}
		return defaultValue;
	}

	private int maxMinute(long date) {
		long currentHour = System.currentTimeMillis() - System.currentTimeMillis() % TimeHelper.ONE_HOUR;

		if (date == currentHour) {
			long current = System.currentTimeMillis() / 1000 / 60;

			return (int) (current % 60);
		}
		return 60;
	}

	private int minute(HttpServletRequest request, long date) {
		String value = request.getParameter("minute");

		if (value != null && value.length() > 0) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// use current minute
			}
		}
		return maxMinute(date);
	}

	private List<Integer> minutes() {
		List<Integer> minutes = new ArrayList<Integer>();

		for (int i = 0; i < 60; i++) {
			minutes.add(i);
		}
		return minutes;
	}

	private Map<String, Department> domainGroups() {
		return projectService.findDepartments(projectService.findAllDomains());
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	private Map<String, List<TopItemView>> topResultView(Map<String, List<TopMetric.Item>> topResult) {
		Map<String, List<TopItemView>> result = new LinkedHashMap<String, List<TopItemView>>();

		for (Map.Entry<String, List<TopMetric.Item>> entry : topResult.entrySet()) {
			List<TopItemView> items = new ArrayList<TopItemView>();

			for (TopMetric.Item item : entry.getValue()) {
				items.add(new TopItemView(item));
			}
			result.put(entry.getKey(), items);
		}
		return result;
	}

	private double sample(String domain) {
		Domain sampleDomain = sampleConfigManager.getConfig().findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}

	public static class TopItemView {
		private final TopMetric.Item m_item;

		private final String m_linkStyle;

		private final String m_shortDomain;

		private final String m_style;

		public TopItemView(TopMetric.Item item) {
			m_item = item;
			m_shortDomain = item == null ? "" : item.getDomain() == null ? "" : item.getDomain().length() <= 18
					? item.getDomain() : item.getDomain().substring(0, 18);

			if (item != null && item.getAlert() == 2) {
				m_style = "background-color:red;color:white;";
				m_linkStyle = "color:white;";
			} else if (item != null && item.getAlert() == 1) {
				m_style = "background-color:#bfa22f;color:white;";
				m_linkStyle = "color:white;";
			} else {
				m_style = "";
				m_linkStyle = "";
			}
		}

		public String getDomain() {
			return m_item.getDomain();
		}

		public String getErrorInfo() {
			return m_item.getErrorInfo();
		}

		public String getLinkStyle() {
			return m_linkStyle;
		}

		public String getShortDomain() {
			return m_shortDomain;
		}

		public String getStyle() {
			return m_style;
		}

		public double getValue() {
			return m_item.getValue();
		}
	}

	private TopReport queryTopReport(long date) {
		ModelRequest request = new ModelRequest(Constants.CAT, date).setProperty("date", String.valueOf(date));

		if (topModelService.isEligable(request)) {
			ModelResponse<TopReport> response = topModelService.invoke(request);
			TopReport report = response.getModel();

			if (report == null || report.getDomains().size() == 0) {
				report = topReportService.queryReport(Constants.CAT, new Date(date), new Date(date + TimeHelper.ONE_HOUR));
			}
			if (report == null) {
				report = new TopReport();
				report.setStartTime(new Date(date));
				report.setEndTime(new Date(date + TimeHelper.ONE_HOUR));
			}
			report.accept(new TopExceptionExclude(exceptionRuleConfigManager));
			return report;
		}
		return new TopReport().setStartTime(new Date(date)).setEndTime(new Date(date + TimeHelper.ONE_HOUR));
	}
}
