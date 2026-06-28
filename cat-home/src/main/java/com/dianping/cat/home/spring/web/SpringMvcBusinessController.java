package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Constants;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SpringMvcBusinessController {
	@Resource
	private ProjectService projectService;

	@Resource
	private BusinessConfigManager businessConfigManager;

	@Resource
	private BusinessTagConfigManager businessTagConfigManager;

	@Resource
	private ConfigHtmlParser configHtmlParser;

	@RequestMapping(value = "/s/business", method = { RequestMethod.GET, RequestMethod.POST })
	public void business(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = action(request);

		if (!"list".equals(action) && !"tagConfig".equals(action)) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		Map<String, Object> model = businessModel(request, action);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(jsp(action));

		dispatcher.forward(request, response);
	}

	String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		if (action == null || action.length() == 0) {
			return "list";
		}
		return action;
	}

	Map<String, Object> businessModel(HttpServletRequest request, String action) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String domain = domain(request);
		BusinessReportConfig config = businessConfigManager.queryConfigByDomain(domain);

		model.put("contextPath", request.getContextPath());
		model.put("actionName", action);
		model.put("domain", domain);
		model.put("businessUrl", request.getContextPath() + "/mvc/s/business");
		model.put("domains", projectService.findAllDomains());
		if ("tagConfig".equals(action)) {
			String tagConfig = request.getParameter("content");

			if (tagConfig != null && tagConfig.length() > 0) {
				model.put("opState", businessTagConfigManager.store(tagConfig) ? "Success" : "Failure");
			}
			model.put("content", configHtmlParser.parse(businessTagConfigManager.getConfig().toString()));
		} else {
			model.put("configs", businessItemConfigs(config));
			model.put("customConfigs", customConfigs(config));
			model.put("tags", businessTagConfigManager.findTagByDomain(domain));
		}
		return model;
	}

	List<BusinessItemConfig> businessItemConfigs(BusinessReportConfig config) {
		List<BusinessItemConfig> configs = new ArrayList<BusinessItemConfig>(config.getBusinessItemConfigs().values());

		Collections.sort(configs, new Comparator<BusinessItemConfig>() {
			@Override
			public int compare(BusinessItemConfig m1, BusinessItemConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});
		return configs;
	}

	List<CustomConfig> customConfigs(BusinessReportConfig config) {
		List<CustomConfig> configs = new ArrayList<CustomConfig>(config.getCustomConfigs().values());

		Collections.sort(configs, new Comparator<CustomConfig>() {
			@Override
			public int compare(CustomConfig m1, CustomConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});
		return configs;
	}

	void setConfigManager(BusinessConfigManager configManager) {
		this.businessConfigManager = configManager;
	}

	void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	void setTagConfigManager(BusinessTagConfigManager tagConfigManager) {
		this.businessTagConfigManager = tagConfigManager;
	}

	void setConfigHtmlParser(ConfigHtmlParser configHtmlParser) {
		this.configHtmlParser = configHtmlParser;
	}

	private String domain(HttpServletRequest request) {
		String domain = request.getParameter("domain");

		if (domain == null || domain.length() == 0) {
			return Constants.CAT;
		}
		return domain;
	}

	private String jsp(String action) {
		if ("tagConfig".equals(action)) {
			return "/jsp/spring/report/config/businessTag.jsp";
		}
		return "/jsp/spring/report/config/businessList.jsp";
	}
}
