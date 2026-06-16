package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.dianping.cat.Cat;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.router.service.CachedRouterConfigService;

@Controller
public class SpringMvcRouterController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcRouterController.class);

	@Resource
	private CachedRouterConfigService m_cachedReportService;

	@Resource
	private RouterConfigManager m_configManager;

	@Resource
	private SampleConfigManager m_sampleConfigManager;

	@Resource
	private ServerFilterConfigManager m_filterManager;

	@GetMapping("/s/router")
	public void router(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String action = action(request);

		response.setCharacterEncoding("utf-8");

		if ("build".equals(action)) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		RouterConfig report = m_cachedReportService.queryLastRouterConfig();
		String domain = request.getParameter("domain");
		String ip = request.getParameter("ip");

		if ("json".equals(action)) {
			writeText(response, JSON.toJSONString(buildKvs(report, domain, ip)));
		} else if ("model".equals(action)) {
			writeText(response, report == null ? "" : report.toString());
		} else if ("view".equals(action)) {
			writeText(response, "spring-mvc-migration");
		} else {
			writeText(response, buildRouterInfo(ip, domain, report));
		}
	}

	String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		if (action == null || action.length() == 0) {
			return "api";
		}
		return action;
	}

	Map<String, String> buildKvs(RouterConfig report, String domain, String ip) {
		Map<String, String> kvs = new HashMap<String, String>();

		kvs.put("block", String.valueOf(m_configManager.shouldBlock(ip)));
		kvs.put("routers", buildRouterInfo(ip, domain, report));
		kvs.put("sample", String.valueOf(buildSampleInfo(domain)));
		kvs.put("startTransactionTypes", m_filterManager.getAtomicStartTypes());
		kvs.put("matchTransactionTypes", m_filterManager.getAtomicMatchTypes());

		return kvs;
	}

	double buildSampleInfo(String domain) {
		double defaultValue = 1.0;
		com.dianping.cat.sample.entity.Domain domainConfig = m_sampleConfigManager.getConfig().findDomain(domain);

		if (domainConfig != null) {
			defaultValue = domainConfig.getSample();
		}
		return defaultValue;
	}

	String buildServerStr(List<Server> servers) {
		StringBuilder sb = new StringBuilder();

		for (Server server : servers) {
			sb.append(server.getId()).append(":").append(server.getPort()).append(";");
		}
		return sb.toString();
	}

	private String buildRouterInfo(String ip, String domain, RouterConfig config) {
		String group = m_configManager.queryServerGroupByIp(ip);
		Domain domainConfig = m_configManager.getRouterConfig().findDomain(domain);
		List<Server> servers = new ArrayList<Server>();

		if (domainConfigNotExist(group, domainConfig)) {
			if (config != null) {
				Domain d = config.findDomain(domain);

				if (d != null && d.findGroup(group) != null) {
					servers = d.findGroup(group).getServers();

					if (servers.isEmpty()) {
						LOGGER.warn("Router report has empty servers for domain={}, group={}, ip={}.", domain, group, ip);
						Cat.logError(new RuntimeException("Error when build router config, domain: " + domain));
					}
				}
			}

			if (servers.isEmpty()) {
				servers = m_configManager.queryServersByDomain(group, domain);
			}
		} else {
			servers = domainConfig.findGroup(group).getServers();
		}
		return buildServerStr(servers);
	}

	private boolean domainConfigNotExist(String group, Domain domainConfig) {
		return domainConfig == null || domainConfig.findGroup(group) == null
				|| domainConfig.findGroup(group).getServers().isEmpty();
	}

	private void writeText(HttpServletResponse response, String content) throws IOException {
		response.setContentType("text/plain;charset=utf-8");
		response.getWriter().write(content == null ? "" : content);
	}
}
