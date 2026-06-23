package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.transaction.DisplayNames;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.page.transaction.DisplayTypes;
import com.dianping.cat.report.page.transaction.TransactionGraphBuilder;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.DistributionDetailVisitor;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcTransactionController {
	private final SimpleDateFormat m_hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat m_subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final TransactionGraphBuilder m_transactionGraphBuilder = new TransactionGraphBuilder();

	@Resource
	private DomainGroupConfigManager m_configManager;

	@Resource
	private GraphBuilder m_graphBuilder;

	@Resource
	private HostinfoService m_hostinfoService;

	@Resource
	private ProjectService m_projectService;

	@Resource
	private SampleConfigManager m_sampleConfigManager;

	@Resource
	private TransactionMergeHelper m_mergeHelper;

	@Resource
	private TransactionReportService m_reportService;

	@Resource
	@Qualifier("transactionModelService")
	private ModelService<TransactionReport> m_transactionService;

	@GetMapping("/mvc/r/t")
	public void transaction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = transactionModel(request);
		String view = "graphs".equals(model.get("action")) ? "/jsp/spring/report/transaction/transactionGraphs.jsp"
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
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String type = emptyToNull(request.getParameter("type"));
		String name = emptyToNull(request.getParameter("name"));
		String queryName = emptyToNull(request.getParameter("queryname"));
		String sortBy = emptyToNull(request.getParameter("sort"));
		String group = emptyToNull(request.getParameter("group"));
		long date = date(request.getParameter("date"), intParameter(request, "step", 0));

		if (StringUtils.isEmpty(group)) {
			group = m_configManager.queryDefaultGroup(domain);
		}

		TransactionReport report = queryHourlyReport(domain, ipAddress, type, date);

		if (report != null) {
			report = m_mergeHelper.mergeAllMachines(report, ipAddress);
		}
		if (report == null) {
			report = new TransactionReport(domain);
			report.setStartTime(new Date(date));
			report.setEndTime(new Date(date + TimeHelper.ONE_HOUR));
		}

		if ("graphs".equals(action)) {
			buildGraphs(model, domain, ipAddress, type, name, date);
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
		model.put("sortBy", sortBy);
		model.put("date", m_hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", m_subtitleFormat.format(report.getStartTime()));
		model.put("reportEnd", m_subtitleFormat.format(report.getEndTime()));
		model.put("ips", ips);
		model.put("ipToHostnameStr", new JsonBuilder().toJson(ipToHostname(ips)));
		model.put("groups", m_configManager.queryDomainGroup(domain));
		model.put("group", group);
		model.put("groupIps", m_configManager.queryIpByDomainAndGroup(domain, group));
		model.put("domainGroups", domainGroups());
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "ip=" + ipAddress + "&queryname=" + (queryName == null ? "" : queryName) + "&domain="
				+ report.getDomain() + (type == null ? "" : "&type=" + encode(type)));
		model.put("baseUri", contextPath + "/mvc/r/t");
		model.put("sample", sample(report.getDomain()));
		model.put("model", model);
		return model;
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

			report = m_mergeHelper.mergeAllNames(report, ipAddress, graphName);
			buildTransactionNameGraph(model, report, type, graphName, ipAddress);
		}
	}

	private void buildTransactionNameGraph(Map<String, Object> model, TransactionReport report, String type, String name,
			String ip) {
		TransactionType transactionType = report.findOrCreateMachine(ip).findOrCreateType(type);
		TransactionName transactionName = transactionType.findOrCreateName(name);

		model.putAll(m_transactionGraphBuilder.build(m_graphBuilder, transactionName));
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

	private Map<String, Department> domainGroups() {
		Collection<String> domains = m_projectService.findAllDomains();

		return m_projectService.findDepartments(domains);
	}

	private String emptyToNull(String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		return value;
	}

	private String encode(String value) {
		if (value == null) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
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

	private Map<String, String> ipToHostname(List<String> ips) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		for (String ip : ips) {
			String hostname = m_hostinfoService.queryHostnameByIp(ip);

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
		ModelResponse<TransactionReport> response = m_transactionService.invoke(request);

		return response.getModel();
	}

	private TransactionReport queryHourlyReport(String domain, String ipAddress, String type, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("type", type).setProperty("ip", ipAddress);

		if (m_transactionService.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);

			return response.getModel();
		}
		return m_reportService.queryReport(domain, new Date(date), new Date(date + TimeHelper.ONE_HOUR));
	}

	private double sample(String domain) {
		Domain sampleDomain = m_sampleConfigManager.getConfig().findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}
}
