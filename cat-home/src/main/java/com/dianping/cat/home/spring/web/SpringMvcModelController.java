package com.dianping.cat.home.spring.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.message.tree.MessageId;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@SuppressWarnings({ "rawtypes" })
public class SpringMvcModelController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcModelController.class);

	@Resource(name = "localProblemService")
	private LocalModelService problemService;

	@Resource(name = "localEventService")
	private LocalModelService eventService;

	@Resource(name = "localTransactionService")
	private LocalModelService transactionService;

	@Resource(name = "localHeartbeatService")
	private LocalModelService heartbeatService;

	@Resource(name = "localCrossService")
	private LocalModelService crossService;

	@Resource(name = "localMatrixService")
	private LocalModelService matrixService;

	@Resource(name = "localDependencyService")
	private LocalModelService dependencyService;

	@Resource(name = "localTopService")
	private LocalModelService topService;

	@Resource(name = "localStateService")
	private LocalModelService stateService;

	@Resource(name = "localStorageService")
	private LocalModelService storageService;

	@Resource(name = "localBusinessService")
	private LocalModelService businessService;

	@Resource(name = "localMessageService")
	private LocalModelService messageService;

	private Map<String, LocalModelService> localServices = Collections.emptyMap();

	private volatile boolean initialized;

	@GetMapping("/mvc/r/model/**")
	public void model(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PathParts path = pathParts(request);
		String xml = render(path, payload(request));

		if (xml != null) {
			ServletOutputStream outputStream = response.getOutputStream();

			response.setContentType("application/xml;charset=utf-8");
			response.addHeader("Content-Encoding", "gzip");
			outputStream.write(compress(xml));
		}
	}

	String render(PathParts path, ModelApiPayload payload) {
		initialize();

		try {
			ModelRequest request = request(path, payload);
			LocalModelService service = localServices.get(path.getReport());

			if (service == null) {
				throw new RuntimeException("Unsupported report: " + path.getReport() + "!");
			}
			return service.getReport(request, path.getPeriod(), path.getDomain(), payload);
		} catch (Throwable e) {
			LOGGER.error("Unable to render model report, report={}, domain={}, period={}, messageId={}.",
					path.getReport(), path.getDomain(), path.getPeriod(), payload.getMessageId(), e);
			Cat.logError(e);
			return null;
		}
	}

	void setLocalServices(Map<String, LocalModelService> services) {
		localServices = services == null ? Collections.emptyMap() : services;
		initialized = true;
	}

	private void addLocalService(Map<String, LocalModelService> services, LocalModelService service) {
		if (service == null) {
			LOGGER.warn("Ignoring null local model service while building model service map.");
			return;
		}

		String name = service.getName();

		if (name == null || name.length() == 0) {
			LOGGER.warn("Ignoring local model service with empty name, serviceClass={}.", service.getClass().getName());
			return;
		}
		services.put(name, service);
	}

	private Map<String, LocalModelService> buildLocalServices() {
		Map<String, LocalModelService> result = new HashMap<String, LocalModelService>();

		addLocalService(result, problemService);
		addLocalService(result, eventService);
		addLocalService(result, transactionService);
		addLocalService(result, heartbeatService);
		addLocalService(result, crossService);
		addLocalService(result, matrixService);
		addLocalService(result, dependencyService);
		addLocalService(result, topService);
		addLocalService(result, stateService);
		addLocalService(result, storageService);
		addLocalService(result, businessService);
		addLocalService(result, messageService);
		return result;
	}

	private byte[] compress(String value) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
		GZIPOutputStream gzip = new GZIPOutputStream(out);

		gzip.write(value.getBytes(StandardCharsets.UTF_8));
		gzip.close();
		return out.toByteArray();
	}

	private synchronized void initialize() {
		if (!initialized) {
			localServices = buildLocalServices();
			LOGGER.info("Initialized Spring MVC model controller, localServiceCount={}.", localServices.size());
			initialized = true;
		}
	}

	private PathParts pathParts(HttpServletRequest request) {
		String path = request.getPathInfo();
		String prefix = "/r/model/";

		if (path == null) {
			path = "";
		}
		if (path.startsWith(prefix)) {
			path = path.substring(prefix.length());
		} else if (path.startsWith("/mvc" + prefix)) {
			path = path.substring(("/mvc" + prefix).length());
		} else if (path.startsWith(prefix.substring(0, prefix.length() - 1))) {
			path = "";
		}
		String[] parts = path.length() == 0 ? new String[0] : path.split("/");

		return new PathParts(part(parts, 0), part(parts, 1), part(parts, 2));
	}

	private String part(String[] parts, int index) {
		return parts.length > index ? parts[index] : null;
	}

	private ModelApiPayload payload(HttpServletRequest request) {
		ModelApiPayload payload = new ModelApiPayload();

		payload.setMessageId(request.getParameter("messageId"));
		payload.setIpAddress(request.getParameter("ip"));
		payload.setWaterfall("true".equalsIgnoreCase(request.getParameter("waterfall")));
		payload.setChannel(request.getParameter("channel"));
		payload.setCity(request.getParameter("city"));
		payload.setDatabase(request.getParameter("database"));
		payload.setMetricType(request.getParameter("metricType"));
		payload.setName(request.getParameter("name"));
		payload.setProvince(request.getParameter("province"));
		payload.setQueryType(request.getParameter("queryType"));
		payload.setThreadId(request.getParameter("thread"));
		payload.setType(request.getParameter("type"));
		payload.setCdn(parameter(request, "cdn", "ALL"));
		payload.setMin(intParameter(request, "min", -1));
		payload.setMax(intParameter(request, "max", -1));
		return payload;
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

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		return value == null ? defaultValue : value;
	}

	private ModelRequest request(PathParts path, ModelApiPayload payload) {
		if ("logview".equals(path.getReport())) {
			return new ModelRequest(path.getDomain(), MessageId.parse(payload.getMessageId()).getTimestamp());
		}
		return new ModelRequest(path.getDomain(), path.getPeriod().getStartTime());
	}

	static class ModelApiPayload extends ApiPayload {
		public void setMetricType(String metricType) {
			super.setMeticType(metricType);
		}
	}

	static class PathParts {
		private final String m_domain;

		private final ModelPeriod m_period;

		private final String m_report;

		PathParts(String report, String domain, String period) {
			m_report = report;
			m_domain = domain;
			m_period = period == null ? ModelPeriod.CURRENT : ModelPeriod.getByName(period, ModelPeriod.CURRENT);
		}

		public String getDomain() {
			return m_domain;
		}

		public ModelPeriod getPeriod() {
			return m_period;
		}

		public String getReport() {
			return m_report;
		}
	}
}
