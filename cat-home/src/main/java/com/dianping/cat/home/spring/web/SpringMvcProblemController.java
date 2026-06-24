package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.problem.LongConfig;
import com.dianping.cat.report.page.problem.transform.HourlyLineChartVisitor;
import com.dianping.cat.report.page.problem.transform.PieGraphChartVisitor;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.SampleConfig;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcProblemController {
	private final SimpleDateFormat m_hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat m_subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private DomainGroupConfigManager m_configManager;

	@Resource
	private HostinfoService m_hostinfoService;

	@Resource
	private ProjectService m_projectService;

	@Resource
	private SampleConfigManager m_sampleConfigManager;

	@Resource
	private ServerConfigManager m_serverConfigManager;

	@Resource
	@Qualifier("problemModelService")
	private ModelService<ProblemReport> m_problemService;

	@GetMapping("/mvc/r/p")
	public void problem(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = problemModel(request);
		String view = "hourlyGraph".equals(model.get("action")) ? "/jsp/spring/report/problem/problemHourlyGraphs.jsp"
				: "/jsp/spring/report/problem/problemStatics.jsp";

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> problemModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String type = emptyToNull(request.getParameter("type"));
		String status = emptyToNull(request.getParameter("status"));
		String group = emptyToNull(request.getParameter("group"));
		int urlThreshold = intParameter(request, "urlThreshold", 1000);
		int sqlThreshold = intParameter(request, "sqlThreshold", 100);
		int serviceThreshold = intParameter(request, "serviceThreshold", 50);
		int cacheThreshold = intParameter(request, "cacheThreshold", 10);
		int callThreshold = intParameter(request, "callThreshold", 50);
		long date = date(request.getParameter("date"), intParameter(request, "step", 0));
		LongConfig longConfig = new LongConfig().setUrlThreshold(urlThreshold).setSqlThreshold(sqlThreshold)
				.setServiceThreshold(serviceThreshold).setCacheThreshold(cacheThreshold).setCallThreshold(callThreshold);

		if (StringUtils.isEmpty(group)) {
			group = m_configManager.queryDefaultGroup(domain);
		}

		ProblemReport report = queryHourlyReport(domain, ipAddress, type, status, date,
				"hourlyGraph".equals(action) ? "detail" : "view");

		if (report == null) {
			report = new ProblemReport(domain);
			report.setStartTime(new Date(date));
			report.setEndTime(new Date(date + TimeHelper.ONE_HOUR));
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : new ArrayList<String>(report.getIps());

		if ("hourlyGraph".equals(action)) {
			buildHourlyGraphs(model, report, ipAddress, type, status);
		} else {
			ProblemStatistics statistics = new ProblemStatistics().setLongConfig(longConfig);

			if (Constants.ALL.equals(ipAddress)) {
				statistics.setAllIp(true);
			} else {
				statistics.setIp(ipAddress);
			}
			statistics.visitProblemReport(report);
			model.put("allStatistics", statistics);
			model.put("defaultThreshold", defaultUrlThreshold(domain));
			model.put("defaultSqlThreshold", defaultSqlThreshold(domain));
		}

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("type", type);
		model.put("status", status);
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
		model.put("baseUri", contextPath + "/mvc/r/p");
		model.put("sample", sample(report.getDomain()));
		model.put("queryString", queryString(urlThreshold, sqlThreshold, serviceThreshold, cacheThreshold, callThreshold));
		model.put("urlThreshold", urlThreshold);
		model.put("sqlThreshold", sqlThreshold);
		model.put("serviceThreshold", serviceThreshold);
		model.put("cacheThreshold", cacheThreshold);
		model.put("callThreshold", callThreshold);
		model.put("navPrefix", "op=view&domain=" + report.getDomain() + "&ip=" + ipAddress
				+ queryString(urlThreshold, sqlThreshold, serviceThreshold, cacheThreshold, callThreshold));
		return model;
	}

	private void buildHourlyGraphs(Map<String, Object> model, ProblemReport report, String ipAddress, String type,
			String status) {
		HourlyLineChartVisitor visitor = new HourlyLineChartVisitor(ipAddress, type, status, report.getStartTime());

		visitor.visitProblemReport(report);
		model.put("errorsTrend", new JsonBuilder().toJson(visitor.getGraphItem()));

		if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
			PieGraphChartVisitor pieChart = new PieGraphChartVisitor(type, status);

			pieChart.visitProblemReport(report);
			model.put("distributionChart", pieChart.getPieChart().getJsonString());
		}
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

	private String defaultSqlThreshold(String domain) {
		Map<String, Domain> domains = m_serverConfigManager.getLongConfigDomains();
		Domain config = domains.get(domain);

		if (config != null) {
			int threshold = config.getSqlThreshold();

			if (threshold != 100 && threshold != 500 && threshold != 1000) {
				NumberFormat format = new DecimalFormat("#");

				return "<option value=\"" + threshold + "\">" + format.format(threshold) + " ms</option>";
			}
		}
		return "";
	}

	private String defaultUrlThreshold(String domain) {
		Map<String, Domain> domains = m_serverConfigManager.getLongConfigDomains();
		Domain config = domains.get(domain);

		if (config != null) {
			int threshold = config.getUrlThreshold() == null ? m_serverConfigManager.getLongUrlDefaultThreshold()
					: config.getUrlThreshold();

			if (threshold != 500 && threshold != 1000 && threshold != 2000 && threshold != 3000 && threshold != 4000
					&& threshold != 5000) {
				NumberFormat format = new DecimalFormat("#.##");

				return "<option value=\"" + threshold + "\">" + format.format(threshold / 1000.0) + " Sec</option>";
			}
		}
		return "";
	}

	private Map<String, Department> domainGroups() {
		Collection<String> domains = m_projectService.findAllDomains();

		return m_projectService.findDepartments(domains);
	}

	private String emptyToNull(String value) {
		return value == null || value.length() == 0 ? null : value;
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

	private ProblemReport queryHourlyReport(String domain, String ipAddress, String type, String status, long date,
			String queryType) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("queryType", queryType);

		if (!Constants.ALL.equals(ipAddress)) {
			request.setProperty("ip", ipAddress);
		}
		if (!StringUtils.isEmpty(type)) {
			request.setProperty("type", type);
		}
		if (!StringUtils.isEmpty(status)) {
			request.setProperty("name", status);
		}
		if (m_problemService.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_problemService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private String queryString(int urlThreshold, int sqlThreshold, int serviceThreshold, int cacheThreshold,
			int callThreshold) {
		return "&urlThreshold=" + urlThreshold + "&sqlThreshold=" + sqlThreshold + "&serviceThreshold="
				+ serviceThreshold + "&cacheThreshold=" + cacheThreshold + "&callThreshold=" + callThreshold;
	}

	private double sample(String domain) {
		SampleConfig config = m_sampleConfigManager.getConfig();
		com.dianping.cat.sample.entity.Domain sampleDomain = config.findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}

	@SuppressWarnings("unused")
	private String encode(String value) {
		return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
