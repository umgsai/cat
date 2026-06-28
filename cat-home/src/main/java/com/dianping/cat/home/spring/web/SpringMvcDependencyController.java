package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.home.dependency.graph.transform.DefaultJsonBuilder;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.graph.LineGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.ProductLinesDashboard;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcDependencyController {
	private final SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	@Resource(name = "dependencyModelService")
	private ModelService<DependencyReport> dependencyModelService;

	@Resource
	private TopologyGraphManager topologyGraphManager;

	@Resource
	private TopoGraphFormatConfigManager topoGraphFormatConfigManager;

	@GetMapping("/mvc/r/dependency")
	public void dependency(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = dependencyModel(request);
		String action = (String) model.get("action");
		String view = "/jsp/spring/report/dependency/dependency.jsp";

		if ("dependencyGraph".equals(action)) {
			view = "/jsp/spring/report/dependency/dependencyTopologyGraph.jsp";
		} else if ("dashboard".equals(action)) {
			view = "/jsp/spring/report/dependency/dependencyDashboard.jsp";
		}

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> dependencyModel(HttpServletRequest request) {
		Map<String, Object> model = baseModel(request);
		String action = (String) model.get("action");
		Date reportTime = new Date(((Long) model.get("longDate")).longValue()
				+ TimeHelper.ONE_MINUTE * ((Integer) model.get("minute")).intValue());

		if ("dashboard".equals(action)) {
			buildDependencyDashboard(model, reportTime);
		} else if ("dependencyGraph".equals(action)) {
			buildProjectTopology(model, reportTime);
		} else {
			buildDependencyLineChart(model);
		}
		return model;
	}

	void setDependencyModelService(ModelService<DependencyReport> service) {
		dependencyModelService = service;
	}

	void setTopoGraphFormatConfigManager(TopoGraphFormatConfigManager manager) {
		topoGraphFormatConfigManager = manager;
	}

	void setTopologyGraphManager(TopologyGraphManager manager) {
		topologyGraphManager = manager;
	}

	private String action(String value) {
		if ("dependencyGraph".equals(value) || "dashboard".equals(value)) {
			return value;
		}
		return "lineChart";
	}

	private boolean booleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
		String value = request.getParameter(name);

		return StringUtils.isEmpty(value) ? defaultValue : Boolean.parseBoolean(value);
	}

	private Map<String, Object> baseModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String action = action(parameter(request, "op", "lineChart"));
		String domain = parameter(request, "domain", Constants.CAT);
		long date = date(request.getParameter("date"), intParameter(request, "step", 0));
		int minute = parseQueryMinute(request);
		int maxMinute = maxMinute(date);
		List<Integer> minutes = new ArrayList<Integer>();

		for (int i = 0; i < 60; i++) {
			minutes.add(Integer.valueOf(i));
		}

		model.put("action", action);
		model.put("activeReport", "Dependency");
		model.put("all", Boolean.valueOf(booleanParameter(request, "all", false)));
		model.put("baseUri", request.getContextPath() + "/mvc/r/dependency");
		model.put("contextPath", request.getContextPath());
		model.put("date", hourlyFormat.format(new Date(date)));
		model.put("displayDomain", domain);
		model.put("domain", domain);
		model.put("frequency", Integer.valueOf(intParameter(request, "frequency", 10)));
		model.put("fullScreen", Boolean.valueOf(booleanParameter(request, "fullScreen", false)));
		model.put("hideNav", Boolean.valueOf(booleanParameter(request, "hideNav", true)));
		model.put("ipAddress", Constants.ALL);
		model.put("longDate", Long.valueOf(date));
		model.put("maxMinute", Integer.valueOf(maxMinute));
		model.put("minute", Integer.valueOf(minute));
		model.put("minutes", minutes);
		model.put("navs", UrlNav.values());
		model.put("productLine", parameter(request, "productLine", ""));
		model.put("refresh", Boolean.valueOf(booleanParameter(request, "refresh", false)));
		model.put("reportType", "");
		model.put("tab", parameter(request, "tab", "tab1"));
		return model;
	}

	private Segment buildAllSegmentsInfo(DependencyReport report) {
		Segment result = new Segment();
		DependencyReportMerger merger = new DependencyReportMerger(null);

		for (Segment segment : report.getSegments().values()) {
			for (Index index : segment.getIndexs().values()) {
				Index temp = result.findOrCreateIndex(index.getName());

				merger.mergeIndex(temp, index);
			}
			for (Dependency dependency : segment.getDependencies().values()) {
				Dependency temp = result.findOrCreateDependency(dependency.getKey());

				merger.mergeDependency(temp, dependency);
			}
		}
		return result;
	}

	private void buildDependencyDashboard(Map<String, Object> model, Date reportTime) {
		ProductLinesDashboard dashboardGraph = topologyGraphManager.buildDependencyDashboard(reportTime.getTime());
		String minute = String.valueOf(model.get("minute"));

		for (List<TopologyNode> nodes : dashboardGraph.getNodes().values()) {
			for (TopologyNode node : nodes) {
				String domain = node.getId();
				String link = String.format("?op=dependencyGraph&minute=%s&domain=%s&date=%s", minute, domain,
						model.get("date"));

				node.setLink(link);
			}
		}

		model.put("dashboardGraph", dashboardGraph.toJson());
		model.put("dashboardGraphData", dashboardGraph);
		model.put("format", topoGraphFormatConfigManager.buildFormatJson());
		model.put("reportStart", displayFormat.format(new Date(((Long) model.get("longDate")).longValue())));
		model.put("reportEnd",
				displayFormat.format(new Date(((Long) model.get("longDate")).longValue() + TimeHelper.ONE_HOUR - 1)));
	}

	private void buildDependencyLineChart(Map<String, Object> model) {
		DependencyReport report = queryDependencyReport(model);

		buildHourlyReport(report, model);
		buildHourlyLineGraph(report, model);
	}

	private void buildHourlyLineGraph(DependencyReport report, Map<String, Object> model) {
		LineGraphBuilder builder = new LineGraphBuilder();

		builder.visitDependencyReport(report);
		model.put("indexGraph", buildLineChartGraph(builder.queryIndex()));
		model.put("dependencyGraph", buildLineChartGraphs(builder.queryDependencyGraph()));
	}

	private void buildHourlyReport(DependencyReport report, Map<String, Object> model) {
		Segment segment = report.findSegment((Integer) model.get("minute"));

		if (Boolean.TRUE.equals(model.get("all"))) {
			segment = buildAllSegmentsInfo(report);
		}

		model.put("report", report);
		model.put("segment", segment == null ? new Segment((Integer) model.get("minute")) : segment);
		model.put("reportStart", displayFormat.format(report.getStartTime()));
		model.put("reportEnd", displayFormat.format(report.getEndTime()));
	}

	private List<String> buildLineChartGraph(List<LineChart> charts) {
		List<String> result = new ArrayList<String>();

		for (LineChart chart : charts) {
			result.add(chart.getJsonString());
		}
		return result;
	}

	private Map<String, List<String>> buildLineChartGraphs(Map<String, List<LineChart>> charts) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		for (Entry<String, List<LineChart>> entry : charts.entrySet()) {
			result.put(entry.getKey(), buildLineChartGraph(entry.getValue()));
		}
		return result;
	}

	private void buildProjectTopology(Map<String, Object> model, Date reportTime) {
		TopologyGraph topologyGraph = topologyGraphManager.buildTopologyGraph((String) model.get("domain"), reportTime.getTime());
		DependencyReport report = queryDependencyReport(model);

		buildHourlyReport(report, model);
		buildTopologyNodeLinks(model, topologyGraph);
		model.put("reportStart", displayFormat.format(new Date(((Long) model.get("longDate")).longValue())));
		model.put("reportEnd",
				displayFormat.format(new Date(((Long) model.get("longDate")).longValue() + TimeHelper.ONE_HOUR - 1)));
		model.put("topologyGraph", new DefaultJsonBuilder().build(topologyGraph));
	}

	private void buildTopologyNodeLinks(Map<String, Object> model, TopologyGraph graph) {
		for (TopologyNode node : graph.getNodes().values()) {
			node.setLink(String.format("?op=dependencyGraph&minute=%s&domain=%s&date=%s", model.get("minute"),
					node.getId(), model.get("date")));
		}
	}

	private long currentHour() {
		long current = System.currentTimeMillis() - TimeHelper.ONE_MINUTE;

		return current - current % TimeHelper.ONE_HOUR;
	}

	private long date(String value, int step) {
		long current = currentHour();
		long result = current;

		if (StringUtils.isNotEmpty(value)) {
			try {
				result = value.length() == 10 ? hourlyFormat.parse(value).getTime() : new SimpleDateFormat("yyyyMMdd").parse(value).getTime();
			} catch (ParseException e) {
				result = current;
			}
		}
		result = result + step * TimeHelper.ONE_HOUR;
		return Math.min(result, current);
	}

	private int intParameter(HttpServletRequest request, String name, int defaultValue) {
		String value = request.getParameter(name);

		if (StringUtils.isNotEmpty(value)) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	private int maxMinute(long date) {
		if (date == currentHour()) {
			long current = (System.currentTimeMillis() - TimeHelper.ONE_MINUTE) / 1000 / 60;

			return (int) (current % 60);
		}
		return 60;
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

	private int parseQueryMinute(HttpServletRequest request) {
		String minute = request.getParameter("minute");

		if (StringUtils.isEmpty(minute)) {
			long current = (System.currentTimeMillis() - TimeHelper.ONE_MINUTE) / 1000 / 60;

			return (int) (current % 60);
		}
		return Integer.parseInt(minute);
	}

	private DependencyReport queryDependencyReport(Map<String, Object> model) {
		String domain = (String) model.get("domain");
		long date = ((Long) model.get("longDate")).longValue();
		ModelRequest request = new ModelRequest(domain, date);

		if (dependencyModelService.isEligable(request)) {
			ModelResponse<DependencyReport> response = dependencyModelService.invoke(request);
			DependencyReport report = response.getModel();

			if (report == null) {
				report = new DependencyReport(domain);
			}
			if (report.getStartTime() == null) {
				report.setStartTime(new Date(date));
			}
			if (report.getEndTime() == null) {
				report.setEndTime(new Date(date + TimeHelper.ONE_HOUR - 1));
			}
			return report;
		}
		throw new RuntimeException("Internal error: no eligible dependency service registered for " + request + "!");
	}
}
