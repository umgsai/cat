package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.business.Action;
import com.dianping.cat.system.page.business.Context;
import com.dianping.cat.system.page.business.Model;
import com.dianping.cat.system.page.business.Payload;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import jakarta.annotation.Resource;
import org.unidal.web.lifecycle.ActionResolver;
import org.unidal.web.lifecycle.DefaultUrlMapping;
import org.unidal.web.lifecycle.UrlMapping;
import org.unidal.web.mvc.lifecycle.RequestContext;
import org.unidal.web.mvc.payload.ParameterProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcBusinessController {
	@Resource
	private ProjectService m_projectService;

	@Resource
	private BusinessConfigManager m_configManager;

	@Resource
	private BusinessTagConfigManager m_tagConfigManager;

	@GetMapping("/s/business")
	public void business(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = action(request);

		if (!"list".equals(action)) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		Context context = businessContext(request, response);
		Model model = businessModel(context);

		SpringMvcWebResourceInitializer.initialize(request);
		request.setAttribute("ctx", context);
		request.setAttribute("payload", context.getPayload());
		request.setAttribute("model", model);

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/system/business/list.jsp");

		dispatcher.forward(request, response);
	}

	String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		if (action == null || action.length() == 0) {
			return "list";
		}
		return action;
	}

	Context businessContext(HttpServletRequest request, HttpServletResponse response) {
		Context context = new Context();
		Payload payload = new Payload();
		RequestContext requestContext = new RequestContext();
		DefaultUrlMapping urlMapping = new DefaultUrlMapping();

		urlMapping.setContextPath(request.getContextPath());
		urlMapping.setServletPath("/mvc/s");
		urlMapping.setModule("s");
		urlMapping.setAction("business");
		urlMapping.setPathInfo("/business");
		urlMapping.setQueryString(request.getQueryString());
		requestContext.setActionResolver(new SpringMvcActionResolver());
		requestContext.setUrlMapping(urlMapping);
		context.initialize(request, response);
		context.setRequestContext(requestContext);
		context.setInboundPage("business");
		context.setOutboundPage("business");
		context.setServletContext(request.getSession().getServletContext());
		payload.setPage(SystemPage.BUSINESS.getName());
		payload.setAction("list");
		payload.setDomain(domain(request));
		context.setPayload(payload);
		return context;
	}

	Model businessModel(Context context) {
		Payload payload = context.getPayload();
		Model model = new Model(context);
		String domain = payload.getDomain();
		BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);

		model.setPage(SystemPage.BUSINESS);
		model.setAction(Action.LIST);
		model.setDomains(m_projectService.findAllDomains());
		model.setConfigs(businessItemConfigs(config));
		model.setCustomConfigs(customConfigs(config));
		model.setTags(m_tagConfigManager.findTagByDomain(domain));
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
		m_configManager = configManager;
	}

	void setProjectService(ProjectService projectService) {
		m_projectService = projectService;
	}

	void setTagConfigManager(BusinessTagConfigManager tagConfigManager) {
		m_tagConfigManager = tagConfigManager;
	}

	private String domain(HttpServletRequest request) {
		String domain = request.getParameter("domain");

		if (domain == null || domain.length() == 0) {
			return Constants.CAT;
		}
		return domain;
	}

	private static class SpringMvcActionResolver implements ActionResolver {
		@Override
		public String buildUrl(ParameterProvider provider, UrlMapping mapping) {
			String contextPath = mapping.getContextPath();
			String servletPath = mapping.getServletPath();
			String action = mapping.getAction();

			return contextPath + servletPath + "/" + action;
		}

		@Override
		public UrlMapping parseUrl(ParameterProvider provider) {
			throw new UnsupportedOperationException();
		}
	}
}
