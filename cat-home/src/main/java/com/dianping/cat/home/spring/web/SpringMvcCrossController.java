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
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
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
public class SpringMvcCrossController {
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
	@Qualifier("crossModelService")
	private ModelService<CrossReport> m_crossService;

	@GetMapping("/mvc/r/cross")
	public void cross(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = crossModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/cross/cross.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> crossModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();
		String domain = parameter(request, "domain", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = parameter(request, "reportType", "day");
		String callSort = parameter(request, "callSort", "avg");
		String serviceSort = parameter(request, "serviceSort", "avg");
		String method = parameter(request, "method", "");
		long date = date(request.getParameter("date"), intParameter(request, "step", 0));
		CrossReport report = queryHourlyReport(domain, ipAddress, date);

		if (report == null) {
			report = new CrossReport(domain);
			report.setStartTime(new Date(date));
			report.setEndTime(new Date(date + TimeHelper.ONE_HOUR));
		}

		List<String> ips = report.getIps() == null ? new ArrayList<String>() : SortHelper.sortIpAddress(report.getIps());
		ProjectInfo projectInfo = new ProjectInfo(hourDuration(date));

		projectInfo.setClientIp(ipAddress).setCallSortBy(callSort).setServiceSortBy(serviceSort);
		projectInfo.visitCrossReport(report);

		model.put("contextPath", contextPath);
		model.put("domain", report.getDomain());
		model.put("displayDomain", domain);
		model.put("ipAddress", ipAddress);
		model.put("reportType", reportType);
		model.put("date", m_hourlyFormat.format(new Date(date)));
		model.put("longDate", date);
		model.put("report", report);
		model.put("reportStart", m_subtitleFormat.format(report.getStartTime()));
		model.put("reportEnd", m_subtitleFormat.format(report.getEndTime()));
		model.put("ips", ips);
		model.put("projectInfo", projectInfo);
		model.put("callSort", callSort);
		model.put("serviceSort", serviceSort);
		model.put("method", method);
		model.put("ipToHostnameStr", new JsonBuilder().toJson(ipToHostname(ips)));
		model.put("groups", m_configManager.queryDomainGroup(domain));
		model.put("domainGroups", domainGroups());
		model.put("navs", UrlNav.values());
		model.put("navPrefix", "ip=" + ipAddress + "&domain=" + report.getDomain() + "&callSort=" + callSort
				+ "&serviceSort=" + serviceSort);
		model.put("baseUri", contextPath + "/mvc/r/cross");
		model.put("sample", sample(report.getDomain()));
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

	private long hourDuration(long date) {
		long currentHour = System.currentTimeMillis() - System.currentTimeMillis() % TimeHelper.ONE_HOUR;

		if (date == currentHour) {
			return System.currentTimeMillis() % TimeHelper.ONE_HOUR / 1000;
		}
		return TimeHelper.ONE_HOUR / 1000;
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

	private CrossReport queryHourlyReport(String domain, String ipAddress, long date) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("ip", ipAddress);

		if (m_crossService.isEligable(request)) {
			ModelResponse<CrossReport> response = m_crossService.invoke(request);

			return response.getModel();
		}
		return null;
	}

	private double sample(String domain) {
		Domain sampleDomain = m_sampleConfigManager.getConfig().findDomain(domain);

		return sampleDomain == null ? 1.0 : sampleDomain.getSample();
	}
}
