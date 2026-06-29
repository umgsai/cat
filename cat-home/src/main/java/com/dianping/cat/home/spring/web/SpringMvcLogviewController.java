package com.dianping.cat.home.spring.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.tree.MessageId;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcLogviewController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcLogviewController.class);

	@Resource
	private ServerConfigManager serverConfigManager;

	@Resource(name = "logviewModelService")
	private ModelService<String> logviewModelService;

	@GetMapping("/mvc/r/m/{messageId}")
	public void logview(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String messageId = messageId(request);
		boolean waterfall = booleanParameter(request, "waterfall", false);
		boolean showHeader = !"no".equals(request.getParameter("header"));
		String domain = parameter(request, "domain", Constants.CAT);
		String logView = logViewModel(request, domain, messageId, waterfall);

		request.setAttribute("contextPath", request.getContextPath());
		request.setAttribute("domain", domain);
		request.setAttribute("ipAddress", Constants.ALL);
		request.setAttribute("reportType", "day");
		request.setAttribute("date", "");
		request.setAttribute("messageId", messageId);
		request.setAttribute("waterfall", waterfall);
		request.setAttribute("showHeader", showHeader);
		request.setAttribute("logView", logView);

		RequestDispatcher dispatcher = request.getRequestDispatcher(view(showHeader));

		dispatcher.forward(request, response);
	}

	String logViewModel(HttpServletRequest request, String domain, String messageId, boolean waterfall) {
		String logView = null;

		try {
			MessageId msgId = MessageId.parse(messageId);

			LOGGER.info("Handling springmvc logview, domain={}, messageId={}, waterfall={}.", domain, messageId, waterfall);

			if (checkStorageTime(msgId)) {
				logView = logView(messageId, waterfall);

				if (logView == null || logView.length() == 0) {
					LOGGER.warn("Springmvc logview not found, domain={}, messageId={}, waterfall={}.", msgId.getDomain(),
							messageId, waterfall);
					Cat.logEvent("Logview", msgId.getDomain() + ":Fail", Event.SUCCESS, messageId);
				} else {
					logView = rewriteLogViewLinks(request, logView);
					LOGGER.info("Springmvc logview loaded, domain={}, messageId={}, length={}.", msgId.getDomain(),
							messageId, logView.length());
					Cat.logEvent("Logview", "Success", Event.SUCCESS, messageId);
				}
			} else {
				LOGGER.warn("Springmvc logview message is outside storage window, domain={}, messageId={}, timestamp={}.",
						msgId.getDomain(), messageId, msgId.getTimestamp());
				Cat.logEvent("Logview", "OldMessage", Event.SUCCESS, messageId);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to load springmvc logview, messageId={}, waterfall={}.", messageId, waterfall, e);
			Cat.logError(e);
		}

		return logView;
	}

	private boolean checkStorageTime(MessageId msg) {
		long time = msg.getTimestamp();
		long current = TimeHelper.getCurrentDay().getTime();

		return time > current - TimeHelper.ONE_DAY * serverConfigManager.getHdfsMaxStorageTime();
	}

	private String logView(String messageId, boolean waterfall) {
		if (messageId == null || messageId.length() == 0) {
			return null;
		}

		MessageId id = MessageId.parse(messageId);
		long timestamp = id.getTimestamp();
		ModelRequest request = new ModelRequest(id.getDomain(), timestamp).setProperty("messageId", messageId)
				.setProperty("waterfall", String.valueOf(waterfall)).setProperty("timestamp", String.valueOf(timestamp));

		if (logviewModelService.isEligible(request)) {
			ModelResponse<String> response = logviewModelService.invoke(request);

			return response == null ? null : response.getModel();
		}
		throw new RuntimeException("Internal error: no eligible logview service registered for " + request + "!");
	}

	private boolean booleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
		String value = request.getParameter(name);

		return value == null || value.length() == 0 ? defaultValue : Boolean.parseBoolean(value);
	}

	private String messageId(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String prefix = request.getContextPath() + "/mvc/r/m/";

		if (uri.startsWith(prefix)) {
			return uri.substring(prefix.length());
		}
		return "";
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		return value == null || value.length() == 0 ? defaultValue : value;
	}

	private String rewriteLogViewLinks(HttpServletRequest request, String logView) {
		String contextPath = request.getContextPath();
		String newPrefix = contextPath + "/mvc/r/m/";

		return logView.replace(contextPath + "/r/m/", newPrefix).replace("/cat/r/m/", newPrefix);
	}

	private String view(boolean showHeader) {
		return showHeader ? "/jsp/spring/report/logview/logview.jsp" : "/jsp/spring/report/logview/logviewFragment.jsp";
	}

	void setConfigManager(ServerConfigManager configManager) {
		serverConfigManager = configManager;
	}

	void setService(ModelService<String> service) {
		logviewModelService = service;
	}
}
