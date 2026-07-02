package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.transaction.DisplayNames;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.page.transaction.DisplayTypes;
import com.dianping.cat.report.page.transaction.TransactionGraphBuilder;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.DistributionDetailVisitor;
import com.dianping.cat.report.page.transaction.transform.DistributionDetailVisitor.DistributionDetail;
import com.dianping.cat.report.page.transaction.transform.PieGraphChartVisitor;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcTransactionController {
	private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final TransactionGraphBuilder transactionGraphBuilder = new TransactionGraphBuilder();

	@Resource
	private DomainGroupConfigManager domainGroupConfigManager;

	@Resource
	private GraphBuilder graphBuilder;

	@Resource
	private HostinfoService hostinfoService;

	@Resource
	private JsonBuilder jsonBuilder;

	@Resource
	private ProjectService projectService;

	@Resource
	private SampleConfigManager sampleConfigManager;

	@Resource
	private TransactionMergeHelper transactionMergeHelper;

	@Resource
	private TransactionReportService transactionReportService;

	@Resource(name = "transactionModelService")
	private ModelService<TransactionReport> transactionModelService;

	@GetMapping("/mvc/r/t")
	public void transaction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = parameter(request, "op", "view");

		if ("vueGraphData".equals(action)) {
			writeJson(response, jsonBuilder.toJson(vueTransactionGraph(request)));
			return;
		}
		if ("vueData".equals(action)) {
			writeJson(response, jsonBuilder.toJson(vueTransactionReport(request)));
			return;
		}

		Map<String, Object> model = transactionModel(request);
		String view = isHistoryGraphAction(action) ? "/jsp/spring/report/transaction/transactionHistoryGraphs.jsp"
				: "graphs".equals(action) ? "/jsp/spring/report/transaction/transactionGraphs.jsp"
						: "/jsp/spring/report/transaction/transaction.jsp";

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> transactionModel(HttpServletRequest request) {
		Cat.logMetricForCount("http-request-transaction");

		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");

		if ("vueData".equals(action)) {
			action = parameter(request, "vueAction", "view");
		}
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String type = emptyToNull(request.getParameter("type"));
		String name = emptyToNull(request.getParameter("name"));
		String queryName = emptyToNull(request.getParameter("queryname"));
		String sortBy = emptyToNull(request.getParameter("sort"));
		String group = emptyToNull(request.getParameter("group"));
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));

		if (StringUtils.isEmpty(group)) {
			group = domainGroupConfigManager.queryDefaultGroup(domain);
		}
		reportType = historyMode ? historyDates.getReportType() : reportType;

		TransactionReport report = historyMode ? queryHistoryReport(domain, historyDates) : queryHourlyReport(domain, ipAddress, type, date);

		if (report != null && isGroupAction(action)) {
			report = filterReportByGroup(report, domain, group);
		}

		if (report != null) {
			report = transactionMergeHelper.mergeAllMachines(report, ipAddress);
		}
		if (report == null) {
			report = new TransactionReport(domain);
			report.setStartTime(historyMode ? historyDates.getStart() : new Date(date));
			report.setEndTime(historyMode ? historyDates.getEnd() : new Date(date + TimeHelper.ONE_HOUR));
		}

		if ("graphs".equals(action)) {
			buildGraphs(model, domain, ipAddress, type, name, date);
		} else if (isHistoryGraphAction(action)) {
			buildHistoryGraphs(model, domain, ipAddress, type, name, group, action, historyDates);
		} else if (StringUtils.isEmpty(type)) {
			model.put("displayTypeReport", new DisplayTypes().display(sortBy, ipAddress, report));
		} else {
			DisplayNames displayNames = new DisplayNames().display(sortBy, type, ipAddress, report, queryName);

			model.put("displayNameReport", displayNames);
			model.put("pieChart", buildTransactionNamePieChart(displayNames.getResults()));
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : SortHelper.sortIpAddress(report.getIps());

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("type", type);
		model.put("encodedType", encode(type));
		model.put("name", name);
		model.put("queryName", queryName);
		model.put("encodedQueryName", encode(queryName));
		model.put("sortBy", sortBy);
		model.put("date", historyMode ? dayFormat.format(new Date(date)) : hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", subtitleFormat.format(historyMode ? historyDates.getStart() : report.getStartTime()));
		model.put("reportEnd", subtitleFormat.format(historyMode ? historyDates.getDisplayEnd() : report.getEndTime()));
		model.put("ips", ips);
		model.put("ipToHostnameStr", new JsonBuilder().toJson(ipToHostname(ips)));
		model.put("groups", domainGroupConfigManager.queryDomainGroup(domain));
		model.put("group", group);
		model.put("groupIps", domainGroupConfigManager.queryIpByDomainAndGroup(domain, group));
		model.put("domainGroups", domainGroups());
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "ip=" + ipAddress + "&queryname=" + (queryName == null ? "" : queryName) + "&domain="
				+ report.getDomain() + (type == null ? "" : "&type=" + encode(type)));
		model.put("historyMode", historyMode);
		model.put("historyNavs", HistoryNav.values());
		model.put("currentNav", HistoryNav.getByName(reportType));
		model.put("customDate", historyMode ? historyDates.getCustomDate() : "");
		model.put("baseUri", contextPath + "/mvc/r/t");
		model.put("sample", sample(report.getDomain()));
		model.put("model", model);
		return model;
	}

	private VueTransactionReport vueTransactionReport(HttpServletRequest request) {
		Map<String, Object> model = transactionModel(request);
		VueTransactionReport report = new VueTransactionReport();
		@SuppressWarnings("unchecked")
		List<String> ips = (List<String>) model.get("ips");
		@SuppressWarnings("unchecked")
		Map<String, String> ipToHostname = ipToHostname(ips);
		@SuppressWarnings("unchecked")
		Map<String, Department> domainGroups = (Map<String, Department>) model.get("domainGroups");
		@SuppressWarnings("unchecked")
		List<String> groups = (List<String>) model.get("groups");
		@SuppressWarnings("unchecked")
		List<String> groupIps = (List<String>) model.get("groupIps");

		report.setContextPath((String) model.get("contextPath"));
		report.setDomain((String) model.get("domain"));
		report.setDisplayDomain((String) model.get("displayDomain"));
		report.setIpAddress((String) model.get("ipAddress"));
		report.setReportType((String) model.get("reportType"));
		report.setType((String) model.get("type"));
		report.setEncodedType((String) model.get("encodedType"));
		report.setName((String) model.get("name"));
		report.setQueryName((String) model.get("queryName"));
		report.setEncodedQueryName((String) model.get("encodedQueryName"));
		report.setSortBy((String) model.get("sortBy"));
		report.setDate((String) model.get("date"));
		report.setLongDate((Long) model.get("longDate"));
		report.setReportStart((String) model.get("reportStart"));
		report.setReportEnd((String) model.get("reportEnd"));
		report.setIps(ips == null ? new ArrayList<String>() : ips);
		report.setIpToHostname(ipToHostname);
		report.setGroups(groups == null ? new ArrayList<String>() : groups);
		report.setGroup((String) model.get("group"));
		report.setGroupIps(groupIps == null ? new ArrayList<String>() : groupIps);
		report.setDomainGroups(vueDomainGroups(domainGroups));
		report.setHistoryMode((Boolean) model.get("historyMode"));
		report.setPieChart((String) model.get("pieChart"));
		report.setSample((Double) model.get("sample"));
		report.setRows(vueTransactionRows(model));
		return report;
	}

	private VueTransactionDistributionDetail vueDistributionDetail(DistributionDetail detail) {
		VueTransactionDistributionDetail row = new VueTransactionDistributionDetail();

		row.setAvg(detail.getAvg());
		row.setFailCount(detail.getFailCount());
		row.setFailPercent(detail.getFailPercent() / 100.0);
		row.setIp(detail.getIp());
		row.setMax(detail.getMax());
		row.setMin(detail.getMin());
		row.setStd(detail.getStd());
		row.setTotalCount(detail.getTotalCount());
		return row;
	}

	private VueTransactionGraph vueTransactionGraph(HttpServletRequest request) {
		String action = parameter(request, "vueAction", "graphs");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String type = emptyToNull(request.getParameter("type"));
		String name = emptyToNull(request.getParameter("name"));
		String group = emptyToNull(request.getParameter("group"));
		boolean historyMode = isHistoryAction(action);
		HistoryDates historyDates = historyMode ? historyDates(request, reportType) : null;
		long date = historyMode ? historyDates.getDate() : date(request.getParameter("date"), intParameter(request, "step", 0));
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		VueTransactionGraph graph = new VueTransactionGraph();

		if (historyMode) {
			buildHistoryGraphs(model, domain, ipAddress, type, name, group, action, historyDates);
		} else {
			buildGraphs(model, domain, ipAddress, type, name, date);
		}
		graph.setHistoryMode(historyMode);
		graph.setGraph1((String) model.get("graph1"));
		graph.setGraph2((String) model.get("graph2"));
		graph.setGraph3((String) model.get("graph3"));
		graph.setGraph4((String) model.get("graph4"));
		graph.setDistributionChart((String) model.get("distributionChart"));
		graph.setResponseTrend((String) model.get("responseTrend"));
		graph.setHitTrend((String) model.get("hitTrend"));
		graph.setErrorTrend((String) model.get("errorTrend"));
		@SuppressWarnings("unchecked")
		List<DistributionDetail> distributionDetails = (List<DistributionDetail>) model.get("distributionDetails");

		if (distributionDetails != null) {
			for (DistributionDetail detail : distributionDetails) {
				graph.getDistributionDetails().add(vueDistributionDetail(detail));
			}
		}
		return graph;
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

	private List<VueTransactionRow> vueTransactionNameRows(DisplayNames names) {
		List<VueTransactionRow> rows = new ArrayList<VueTransactionRow>();
		int index = 0;

		for (TransactionNameModel item : names.getResults()) {
			TransactionName name = item.getDetail();
			VueTransactionRow row = vueTransactionRow(name);

			row.setIndex(index);
			row.setTotalRow(index == 0);
			row.setEncodedName(item.getName());
			row.setEncodedType(encode(item.getType()));
			row.setTotalPercent(name.getTotalPercent());
			rows.add(row);
			index++;
		}
		return rows;
	}

	private VueTransactionRow vueTransactionRow(TransactionName name) {
		VueTransactionRow row = new VueTransactionRow();

		row.setId(name.getId());
		row.setTotalCount(name.getTotalCount());
		row.setFailCount(name.getFailCount());
		row.setFailPercent(name.getFailPercent() / 100.0);
		row.setMessageUrl(StringUtils.isEmpty(name.getFailMessageUrl()) ? name.getSuccessMessageUrl()
				: name.getFailMessageUrl());
		row.setMin(name.getMin());
		row.setMax(name.getMax());
		row.setAvg(name.getAvg());
		row.setLine95Value(name.getLine95Value());
		row.setLine99Value(name.getLine99Value());
		row.setStd(name.getStd());
		row.setTps(name.getTps());
		return row;
	}

	private VueTransactionRow vueTransactionRow(TransactionType type) {
		VueTransactionRow row = new VueTransactionRow();

		row.setId(type.getId());
		row.setTotalCount(type.getTotalCount());
		row.setFailCount(type.getFailCount());
		row.setFailPercent(type.getFailPercent() / 100.0);
		row.setMessageUrl(StringUtils.isEmpty(type.getFailMessageUrl()) ? type.getSuccessMessageUrl()
				: type.getFailMessageUrl());
		row.setMin(type.getMin());
		row.setMax(type.getMax());
		row.setAvg(type.getAvg());
		row.setLine95Value(type.getLine95Value());
		row.setLine99Value(type.getLine99Value());
		row.setStd(type.getStd());
		row.setTps(type.getTps());
		return row;
	}

	private List<VueTransactionRow> vueTransactionRows(Map<String, Object> model) {
		DisplayTypes types = (DisplayTypes) model.get("displayTypeReport");

		if (types != null) {
			List<VueTransactionRow> rows = new ArrayList<VueTransactionRow>();
			int index = 0;

			for (DisplayTypes.TransactionTypeModel item : types.getResults()) {
				VueTransactionRow row = vueTransactionRow(item.getDetail());

				row.setIndex(index);
				row.setEncodedType(item.getType());
				rows.add(row);
				index++;
			}
			return rows;
		}

		DisplayNames names = (DisplayNames) model.get("displayNameReport");

		if (names != null) {
			return vueTransactionNameRows(names);
		}
		return new ArrayList<VueTransactionRow>();
	}

	private LineChart buildHistoryLineChart(Date start, Date end, String title, long step, double[] values) {
		LineChart chart = new LineChart();
		int size = (int) Math.max(0, (end.getTime() - start.getTime()) / step);

		chart.setStart(start);
		chart.setSize(size);
		chart.setStep(step);
		chart.setSubTitles(Collections.singletonList(buildHistorySubTitle(start, end)));
		chart.setTitle(title);
		chart.addValue(values == null ? new double[0] : values);
		return chart;
	}

	private String buildHistorySubTitle(Date start, Date end) {
		SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat to = new SimpleDateFormat("MM-dd");

		return from.format(start) + "~" + to.format(end);
	}

	private void buildGraphs(Map<String, Object> model, String domain, String ipAddress, String type, String name,
			long date) {
		TransactionReport report = queryHourlyGraphReport(domain, ipAddress, type, name, date);

		if (report != null) {
			if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
				PieGraphChartVisitor chartVisitor = new PieGraphChartVisitor(type, name);
				DistributionDetailVisitor detailVisitor = new DistributionDetailVisitor(type, name);

				chartVisitor.visitTransactionReport(report);
				detailVisitor.visitTransactionReport(report);
				model.put("distributionChart", chartVisitor.getPieChart().getJsonString());
				model.put("distributionDetails", detailVisitor.getDetails());
			}

			String graphName = StringUtils.isEmpty(name) ? Constants.ALL : name;

			report = transactionMergeHelper.mergeAllNames(report, ipAddress, graphName);
			buildTransactionNameGraph(model, report, type, graphName, ipAddress);
		}
	}

	private void buildHistoryGraphs(Map<String, Object> model, String domain, String ipAddress, String type, String name,
			String group, String action, HistoryDates dates) {
		TransactionReport report = queryHistoryReport(domain, dates);

		if (report != null) {
			if (isGroupAction(action)) {
				report = filterReportByGroup(report, domain, group);
			}
			if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
				PieGraphChartVisitor chartVisitor = new PieGraphChartVisitor(type, name);
				DistributionDetailVisitor detailVisitor = new DistributionDetailVisitor(type, name);

				chartVisitor.visitTransactionReport(report);
				detailVisitor.visitTransactionReport(report);
				model.put("distributionChart", chartVisitor.getPieChart().getJsonString());
				model.put("distributionDetails", detailVisitor.getDetails());
			}

			report = transactionMergeHelper.mergeAllMachines(report, ipAddress);
			buildTransactionTrendGraph(model, report, type, name, ipAddress, dates);
		}
	}

	private void buildTransactionTrendGraph(Map<String, Object> model, TransactionReport report, String type, String name,
			String ipAddress, HistoryDates dates) {
		GraphTrend graph = findGraphTrend(report, ipAddress, type, name);
		int duration = graph == null || graph.getDuration() <= 0 ? 1 : graph.getDuration();
		long step = historyGraphStep(dates.getReportType()) * duration;
		String display = StringUtils.isEmpty(name) ? type : name;

		model.put("responseTrend", buildHistoryLineChart(dates.getStart(), dates.getEnd(),
				display + " Response Time (ms)", step, parseDoubles(graph == null ? null : graph.getAvg())).getJsonString());
		model.put("hitTrend", buildHistoryLineChart(dates.getStart(), dates.getEnd(),
				display + historyGraphHitTitle(dates.getReportType()), step,
				parseDoubles(graph == null ? null : graph.getCount())).getJsonString());
		model.put("errorTrend", buildHistoryLineChart(dates.getStart(), dates.getEnd(),
				display + historyGraphErrorTitle(dates.getReportType()), step,
				parseDoubles(graph == null ? null : graph.getFails())).getJsonString());
	}

	private void buildTransactionNameGraph(Map<String, Object> model, TransactionReport report, String type, String name,
			String ip) {
		TransactionType transactionType = report.findOrCreateMachine(ip).findOrCreateType(type);
		TransactionName transactionName = transactionType.findOrCreateName(name);

		model.putAll(transactionGraphBuilder.build(graphBuilder, transactionName));
	}

	private String buildTransactionNamePieChart(List<TransactionNameModel> names) {
		PieChart chart = new PieChart();
		List<PieChart.Item> items = new ArrayList<PieChart.Item>();

		for (int i = 1; i < names.size(); i++) {
			TransactionNameModel name = names.get(i);
			PieChart.Item item = new PieChart.Item();
			TransactionName transaction = name.getDetail();

			item.setNumber(transaction.getTotalCount()).setTitle(transaction.getId());
			items.add(item);
		}
		chart.addItems(items);
		return new JsonBuilder().toJson(chart);
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

	private String emptyToNull(String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		return value;
	}

	private GraphTrend findGraphTrend(TransactionReport report, String ipAddress, String type, String name) {
		if (report == null || StringUtils.isEmpty(type)) {
			return null;
		}
		Machine machine = report.findMachine(ipAddress);

		if (machine == null) {
			return null;
		}
		TransactionType transactionType = machine.findType(type);

		if (transactionType == null) {
			return null;
		}
		if (StringUtils.isEmpty(name)) {
			return transactionType.getGraphTrend();
		}
		TransactionName transactionName = transactionType.findName(name);

		return transactionName == null ? null : transactionName.getGraphTrend();
	}

	private String encode(String value) {
		if (value == null) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private TransactionReport filterReportByGroup(TransactionReport report, String domain, String group) {
		List<String> ips = domainGroupConfigManager.queryIpByDomainAndGroup(domain, group);
		List<String> removes = new ArrayList<String>();

		for (Machine machine : report.getMachines().values()) {
			String ip = machine.getIp();

			if (!ips.contains(ip)) {
				removes.add(ip);
			}
		}
		for (String ip : removes) {
			report.getMachines().remove(ip);
		}
		return report;
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

	private String historyGraphErrorTitle(String reportType) {
		return "day".equals(reportType) ? " Error (count/min)" : " Error (count/day)";
	}

	private String historyGraphHitTitle(String reportType) {
		return "day".equals(reportType) ? " Hits (count/min)" : " Hits (count/day)";
	}

	private long historyGraphStep(String reportType) {
		return "day".equals(reportType) ? TimeHelper.ONE_MINUTE : TimeHelper.ONE_DAY;
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

	private boolean isGroupAction(String action) {
		return "groupReport".equals(action) || "historyGroupReport".equals(action) || "groupGraphs".equals(action)
				|| "historyGroupGraph".equals(action);
	}

	private boolean isHistoryAction(String action) {
		return action != null && action.startsWith("history");
	}

	private boolean isHistoryGraphAction(String action) {
		return "historyGraph".equals(action) || "historyGroupGraph".equals(action);
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

	private String normalizeReportType(String reportType) {
		if ("month".equals(reportType) || "week".equals(reportType)) {
			return reportType;
		}
		return "day";
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

	private double[] parseDoubles(String value) {
		if (value == null || value.length() == 0) {
			return new double[0];
		}
		String[] parts = value.split(GraphTrendUtil.GRAPH_SPLITTER);
		double[] result = new double[parts.length];

		for (int i = 0; i < parts.length; i++) {
			try {
				result[i] = Double.parseDouble(parts[i]);
			} catch (NumberFormatException e) {
				result[i] = 0.0;
				Cat.logError(e);
			}
		}
		return result;
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

	private TransactionReport queryHourlyGraphReport(String domain, String ipAddress, String type, String name, long date) {
		String graphName = StringUtils.isEmpty(name) ? "*" : name;
		ModelRequest request = new ModelRequest(domain, date).setProperty("type", type).setProperty("name", graphName)
				.setProperty("ip", ipAddress);
		ModelResponse<TransactionReport> response = transactionModelService.invoke(request);

		return response.getModel();
	}

	private TransactionReport queryHourlyReport(String domain, String ipAddress, String type, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("type", type).setProperty("ip", ipAddress);

		if (transactionModelService.isEligible(request)) {
			ModelResponse<TransactionReport> response = transactionModelService.invoke(request);

			return response.getModel();
		}
		return transactionReportService.queryReport(domain, new Date(date), new Date(date + TimeHelper.ONE_HOUR));
	}

	private TransactionReport queryHistoryReport(String domain, HistoryDates dates) {
		return transactionReportService.queryReport(domain, dates.getStart(), dates.getEnd());
	}

	private double sample(String domain) {
		Domain sampleDomain = sampleConfigManager.getConfig().findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}

	void setJsonBuilder(JsonBuilder jsonBuilder) {
		this.jsonBuilder = jsonBuilder;
	}

	private void writeJson(HttpServletResponse response, String body) throws IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(body == null ? "" : body);
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

	@Data
	public static class VueTransactionDistributionDetail {
		private double avg;

		private long failCount;

		private double failPercent;

		private String ip;

		private double max;

		private double min;

		private double std;

		private long totalCount;
	}

	@Data
	public static class VueTransactionGraph {
		private String distributionChart;

		private List<VueTransactionDistributionDetail> distributionDetails = new ArrayList<VueTransactionDistributionDetail>();

		private String errorTrend;

		private String graph1;

		private String graph2;

		private String graph3;

		private String graph4;

		private String hitTrend;

		private boolean historyMode;

		private String responseTrend;
	}

	@Data
	public static class VueTransactionReport {
		private String contextPath;

		private String date;

		private String displayDomain;

		private String domain;

		private List<VueDomainDepartment> domainGroups = new ArrayList<VueDomainDepartment>();

		private String encodedQueryName;

		private String encodedType;

		private String group;

		private List<String> groupIps = new ArrayList<String>();

		private List<String> groups = new ArrayList<String>();

		private boolean historyMode;

		private String ipAddress;

		private Map<String, String> ipToHostname = new LinkedHashMap<String, String>();

		private List<String> ips = new ArrayList<String>();

		private long longDate;

		private String name;

		private String pieChart;

		private String queryName;

		private String reportEnd;

		private String reportStart;

		private String reportType;

		private List<VueTransactionRow> rows = new ArrayList<VueTransactionRow>();

		private double sample;

		private String sortBy;

		private String type;
	}

	@Data
	public static class VueTransactionRow {
		private double avg;

		private String encodedName;

		private String encodedType;

		private long failCount;

		private double failPercent;

		private String id;

		private int index;

		private double line95Value;

		private double line99Value;

		private double max;

		private String messageUrl;

		private double min;

		private double std;

		private long totalCount;

		private double totalPercent;

		private boolean totalRow;

		private double tps;
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
