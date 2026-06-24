package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.state.Payload;
import com.dianping.cat.report.page.state.StateBuilder;
import com.dianping.cat.report.page.state.StateDisplay;
import com.dianping.cat.report.page.state.StateGraphBuilder;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcStateController {
	private final SimpleDateFormat m_hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat m_subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Resource
	private StateBuilder m_stateBuilder;

	@Resource
	private StateGraphBuilder m_stateGraphBuilder;

	@Resource
	@Qualifier("stateModelService")
	private ModelService<StateReport> m_stateService;

	@GetMapping("/mvc/r/state")
	public void state(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = stateModel(request);
		String view = "graph".equals(model.get("action")) ? "/jsp/spring/report/state/stateGraphs.jsp"
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
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String sort = emptyToNull(request.getParameter("sort"));
		boolean show = !"false".equalsIgnoreCase(parameter(request, "show", "true"));
		long date = date(request.getParameter("date"), intParameter(request, "step", 0));
		StateReport report = queryHourlyReport(ipAddress, date);

		if (report == null) {
			report = emptyReport(date);
		}

		if ("graph".equals(action)) {
			buildGraph(model, report, ipAddress, date, request.getParameter("key"));
		} else {
			StateDisplay display = new StateDisplay(ipAddress, m_serverFilterConfigManager.getUnusedDomains());

			display.setSortType(sort);
			display.visitStateReport(report);
			model.put("state", display);
			model.put("message", m_stateBuilder.buildStateMessage(date, ipAddress));
		}

		List<String> ips = report.getMachines() == null ? new ArrayList<String>()
				: SortHelper.sortIpAddress(report.getMachines().keySet());

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", Constants.CAT);
		model.put("displayDomain", Constants.CAT);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("date", m_hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", m_subtitleFormat.format(report.getStartTime()));
		model.put("reportEnd", m_subtitleFormat.format(report.getEndTime()));
		model.put("ips", ips);
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "domain=cat&ip=" + ipAddress + "&show=" + show);
		model.put("show", show);
		model.put("sort", sort);
		model.put("baseUri", contextPath + "/mvc/r/state");
		return model;
	}

	private void buildGraph(Map<String, Object> model, StateReport report, String ipAddress, long date, String key) {
		Payload payload = new Payload();

		payload.setIpAddress(ipAddress);
		payload.setDate(m_hourlyFormat.format(new Date(date)));
		payload.setKey(key);

		Pair<LineChart, PieChart> pair = m_stateGraphBuilder.buildGraph(payload, key, report);

		model.put("key", key);
		model.put("graph", new JsonBuilder().toJson(pair.getKey()));
		model.put("pieChart", new JsonBuilder().toJson(pair.getValue()));
	}

	private long date(String value, int step) {
		long current = System.currentTimeMillis();
		long currentHour = current - current % TimeHelper.ONE_HOUR;
		long result = currentHour;

		if (value != null && value.length() > 0) {
			try {
				result = value.length() == 10 ? m_hourlyFormat.parse(value).getTime()
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

	private StateReport emptyReport(long date) {
		StateReport report = new StateReport(Constants.CAT);

		report.setStartTime(new Date(date));
		report.setEndTime(new Date(date + TimeHelper.ONE_HOUR - 1));
		return report;
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

	private StateReport queryHourlyReport(String ipAddress, long date) {
		ModelRequest request = new ModelRequest(Constants.CAT, date).setProperty("ip", ipAddress);

		if (m_stateService.isEligable(request)) {
			ModelResponse<StateReport> response = m_stateService.invoke(request);

			return response.getModel();
		}
		return null;
	}
}
