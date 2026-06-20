package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.page.dependency.TopExceptionExclude;
import com.dianping.cat.report.page.dependency.TopMetric;
import com.dianping.cat.report.page.state.StateBuilder;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcTopController {
	private final SimpleDateFormat m_hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat m_subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private ExceptionRuleConfigManager m_configManager;

	@Resource
	private StateBuilder m_stateBuilder;

	@Resource
	private TopReportService m_topReportService;

	@Resource
	@Qualifier("topModelService")
	private ModelService<TopReport> m_topService;

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
		TopMetric topMetric = new TopMetric(minuteCount, topCount, m_configManager, Arrays.asList(Constants.FRONT_END));

		topMetric.setStart(start).setEnd(end);
		if (minuteCount > minute) {
			TopReport lastReport = queryTopReport(date - TimeHelper.ONE_HOUR);

			topMetric.visitTopReport(lastReport);
		}
		topMetric.visitTopReport(report);

		model.put("webapp", contextPath);
		model.put("domain", domain);
		model.put("ipAddress", ipAddress);
		model.put("date", m_hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("minute", minute);
		model.put("maxMinute", maxMinute);
		model.put("minutes", minutes());
		model.put("minuteCount", minuteCount);
		model.put("topCount", topCount);
		model.put("fullScreen", booleanParameter(request, "fullScreen"));
		model.put("refresh", booleanParameter(request, "refresh"));
		model.put("frequency", intParameter(request, "frequency", 10));
		model.put("reportStart", m_subtitleFormat.format(new Date(date)));
		model.put("reportEnd", m_subtitleFormat.format(new Date(date + TimeHelper.ONE_HOUR - 1)));
		model.put("message", m_stateBuilder.buildStateMessage(date, ipAddress));
		model.put("topReport", report);
		model.put("topMetric", topMetric);
		model.put("topResult", topMetric.getError().getResult());
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
				Date date = value.length() == 10 ? m_hourlyFormat.parse(value) : new SimpleDateFormat("yyyyMMdd").parse(value);

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

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	private TopReport queryTopReport(long date) {
		ModelRequest request = new ModelRequest(Constants.CAT, date).setProperty("date", String.valueOf(date));

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();

			if (report == null || report.getDomains().size() == 0) {
				report = m_topReportService.queryReport(Constants.CAT, new Date(date), new Date(date + TimeHelper.ONE_HOUR));
			}
			if (report == null) {
				report = new TopReport();
				report.setStartTime(new Date(date));
				report.setEndTime(new Date(date + TimeHelper.ONE_HOUR));
			}
			report.accept(new TopExceptionExclude(m_configManager));
			return report;
		}
		return new TopReport().setStartTime(new Date(date)).setEndTime(new Date(date + TimeHelper.ONE_HOUR));
	}
}
