package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.heartbeat.HeartbeatSvgGraph;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcHeartbeatController {
	private final SimpleDateFormat m_hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private final SimpleDateFormat m_subtitleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private DomainGroupConfigManager m_configManager;

	@Resource
	private GraphBuilder m_graphBuilder;

	@Resource
	private HeartbeatDisplayPolicyManager m_displayPolicyManager;

	@Resource
	private HostinfoService m_hostinfoService;

	@Resource
	private ProjectService m_projectService;

	@Resource
	private SampleConfigManager m_sampleConfigManager;

	@Resource
	@Qualifier("heartbeatModelService")
	private ModelService<HeartbeatReport> m_heartbeatService;

	@GetMapping("/mvc/r/h")
	public void heartbeat(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = heartbeatModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/heartbeat/heartbeat.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> heartbeatModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String action = parameter(request, "op", "view");
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		long date = date(request.getParameter("date"), intParameter(request, "step", 0));

		HeartbeatReport report = queryHourlyReport(domain, ipAddress, date);

		if (report == null) {
			report = new HeartbeatReport(domain);
			report.setStartTime(new Date(date));
			report.setEndTime(new Date(date + TimeHelper.ONE_HOUR));
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : SortHelper.sortIpAddress(report.getIps());
		String realIp = realIp(ipAddress, ips);
		HeartbeatSvgGraph heartbeat = new HeartbeatSvgGraph(m_graphBuilder, m_displayPolicyManager).display(report, realIp);

		model.put("action", action);
		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("realIp", realIp);
		model.put("reportType", reportType);
		model.put("date", m_hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", m_subtitleFormat.format(report.getStartTime()));
		model.put("reportEnd", m_subtitleFormat.format(report.getEndTime()));
		model.put("ips", ips);
		model.put("extensionGraph", heartbeat.getExtensionGraph());
		model.put("ipToHostnameStr", new JsonBuilder().toJson(ipToHostname(ips)));
		model.put("groups", m_configManager.queryDomainGroup(domain));
		model.put("domainGroups", domainGroups());
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "ip=" + ipAddress + "&domain=" + report.getDomain());
		model.put("baseUri", contextPath + "/mvc/r/h");
		model.put("sample", sample(report.getDomain()));
		model.put("model", model);
		return model;
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

	private HeartbeatReport queryHourlyReport(String domain, String ipAddress, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("ip", ipAddress);

		if (m_heartbeatService.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_heartbeatService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private String realIp(String ipAddress, List<String> ips) {
		if ((ipAddress == null || ipAddress.length() == 0 || Constants.ALL.equals(ipAddress)) && !ips.isEmpty()) {
			return ips.get(0);
		}
		return ipAddress;
	}

	private double sample(String domain) {
		Domain sampleDomain = m_sampleConfigManager.getConfig().findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}
}
