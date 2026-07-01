package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.mybatis.data.AlertDO;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mybatis.AlertRepository;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpringMvcAlertController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcAlertController.class);

	private final SimpleDateFormat minuteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Resource
	private AlertRepository alertRepository;

	@Resource
	private SenderManager senderManager;

	@RequestMapping("/mvc/r/alert")
	public void alert(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = action(request);

		if ("alert".equals(action)) {
			writeText(response, sendAlert(request));
			return;
		} else if ("insert".equals(action)) {
			writeText(response, insertAlert(request));
			return;
		}

		Map<String, Object> model = alertModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/alert/alert.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> alertModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		Date startTime = startTime(request.getParameter("startTime"));
		Date endTime = endTime(request.getParameter("endTime"));
		String domain = emptyToNull(request.getParameter("domain"));
		String alertType = defaultString(request.getParameter("alertType"));
		List<AlertDO> alerts = new ArrayList<AlertDO>();

		try {
			if (StringUtils.isEmpty(alertType)) {
				alerts = alertRepository.queryAlertsByTimeDomain(startTime, endTime, domain);
			} else {
				alerts = alertRepository.queryAlertsByTimeDomainCategories(startTime, endTime, domain,
						splitNonEmptyArray(alertType));
			}
		} catch (RuntimeException e) {
			LOGGER.error("Unable to query alerts, startTime={}, endTime={}, domain={}, alertTypes={}.", startTime, endTime,
					domain, alertType, e);
			Cat.logError(e);
		}

		model.put("action", "view");
		model.put("activeReport", "Alert");
		model.put("alertMinutes", generateAlertMinutes(alerts));
		model.put("alertType", alertType);
		model.put("contextPath", request.getContextPath());
		model.put("count", Integer.valueOf(intParameter(request, "count", 10)));
		model.put("date", "");
		model.put("domain", domain);
		model.put("endTime", endTime);
		model.put("fullScreen", Boolean.valueOf(booleanParameter(request, "fullScreen", false)));
		model.put("ipAddress", "All");
		model.put("reportType", "day");
		model.put("startTime", startTime);
		return model;
	}

	void setAlertRepository(AlertRepository repository) {
		alertRepository = repository;
	}

	void setSenderManager(SenderManager manager) {
		senderManager = manager;
	}

	private String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		if ("insert".equals(action)) {
			return "insert";
		} else if ("view".equals(action)) {
			return "view";
		}
		return "alert";
	}

	private Date alertTime(String value) {
		if (StringUtils.isEmpty(value)) {
			LOGGER.warn("Unable to parse alertTime, value={}. Use current time.", value);
			return new Date();
		}
		try {
			synchronized (minuteFormat) {
				return minuteFormat.parse(value);
			}
		} catch (ParseException e) {
			LOGGER.warn("Unable to parse alertTime, value={}. Use current time.", value);
			return new Date();
		}
	}

	private AlertDO buildAlert(HttpServletRequest request) {
		AlertDO alert = new AlertDO();

		alert.setAlertTime(alertTime(request.getParameter("alertTime")));
		alert.setCategory(defaultValue(request.getParameter("category"), "zabbix"));
		alert.setContent(defaultString(request.getParameter("content")));
		alert.setDomain(emptyToNull(request.getParameter("domain")));
		alert.setMetric(defaultString(request.getParameter("metric")));
		alert.setType(defaultValue(request.getParameter("level"), "warning"));
		return alert;
	}

	private boolean booleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
		String value = request.getParameter(name);

		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	private String defaultString(String value) {
		return value == null ? "" : value;
	}

	private String defaultValue(String value, String defaultValue) {
		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

	private String emptyToNull(String value) {
		return value == null || value.length() == 0 ? null : value;
	}

	private Date endTime(String value) {
		if (StringUtils.isEmpty(value)) {
			return new Date();
		}
		try {
			synchronized (minuteFormat) {
				return minuteFormat.parse(value);
			}
		} catch (ParseException e) {
			LOGGER.warn("Unable to parse alert endTime, value={}. Use current time.", value);
			return new Date();
		}
	}

	private Map<String, AlertMinute> generateAlertMinutes(List<AlertDO> alerts) {
		DateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		Map<String, AlertMinute> alertMinutes = new LinkedHashMap<String, AlertMinute>();

		for (AlertDO alert : alerts) {
			String time = format.format(alert.getAlertTime());
			AlertMinute alertMinute = alertMinutes.get(time);

			if (alertMinute == null) {
				alertMinute = new AlertMinute(time);
				alertMinutes.put(time, alertMinute);
			}
			alertMinute.addAlert(alert);
		}
		return alertMinutes;
	}

	private int intParameter(HttpServletRequest request, String name, int defaultValue) {
		String value = request.getParameter(name);

		if (StringUtils.isNotEmpty(value)) {
			try {
				int result = Integer.parseInt(value);

				return result == 0 ? defaultValue : result;
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	private String insertAlert(HttpServletRequest request) {
		if (StringUtils.isEmpty(emptyToNull(request.getParameter("domain")))) {
			return "{\"status\":500, \"errorMessage\":\"lack domain\"}";
		}

		AlertDO alert = buildAlert(request);

		try {
			int count = alertRepository.insert(alert);

			if (count == 0) {
				LOGGER.warn("Manual alert insert returned zero, domain={}, category={}, metric={}.", alert.getDomain(),
						alert.getCategory(), alert.getMetric());
				return "{\"status\":500}";
			}
			return "{\"status\":200}";
		} catch (RuntimeException e) {
			LOGGER.error("Unable to insert manual alert, domain={}, category={}, metric={}.", alert.getDomain(),
					alert.getCategory(), alert.getMetric(), e);
			Cat.logError(e);
			return "{\"status\":500}";
		}
	}

	private String sendAlert(HttpServletRequest request) {
		List<String> receivers = splitNonEmpty(defaultString(request.getParameter("receivers")));
		String channel = defaultString(request.getParameter("channel"));
		String type = defaultValue(request.getParameter("type"), "call");
		String group = defaultValue(request.getParameter("group"), "default");

		if (receivers.isEmpty()) {
			LOGGER.warn("Manual alert send request lacks receivers, channel={}, type={}, group={}.", channel, type, group);
			return "{\"status\":500, \"errorMessage\":\"lack receivers\"}";
		}

		SendMessageEntity message = new SendMessageEntity(group, defaultString(request.getParameter("title")), type,
				defaultString(request.getParameter("content")), receivers);

		try {
			AlertChannel alertChannel = AlertChannel.findByName(channel);

			if (alertChannel == null) {
				throw new NullPointerException("Invalid alert channel: " + channel);
			}
			if (senderManager.sendAlert(alertChannel, message)) {
				return "{\"status\":200}";
			}
			LOGGER.warn("Manual alert send failed, channel={}, type={}, group={}, receiverCount={}.", channel, type, group,
					receivers.size());
			return "{\"status\":500, \"errorMessage\":\"send failed, please retry again\"}";
		} catch (NullPointerException ex) {
			LOGGER.error("Manual alert send failed because channel is invalid, channel={}, type={}, group={}.", channel, type,
					group, ex);
			return "{\"status\":500, \"errorMessage\":\"send failed, please check your channel argument\"}";
		}
	}

	private List<String> splitNonEmpty(String value) {
		List<String> result = new ArrayList<String>();

		if (value == null || value.length() == 0) {
			return result;
		}
		String[] items = value.split(",", -1);

		for (String item : items) {
			if (item.length() > 0) {
				result.add(item);
			}
		}
		return result;
	}

	private String[] splitNonEmptyArray(String value) {
		List<String> result = splitNonEmpty(value);

		return result.toArray(new String[result.size()]);
	}

	private Date startTime(String value) {
		if (StringUtils.isEmpty(value)) {
			LOGGER.warn("Unable to parse alert startTime, value={}. Use default 15 minutes ago.", value);
			return new Date(System.currentTimeMillis() - 15 * TimeHelper.ONE_MINUTE);
		}
		try {
			synchronized (minuteFormat) {
				return minuteFormat.parse(value);
			}
		} catch (ParseException e) {
			LOGGER.warn("Unable to parse alert startTime, value={}. Use default 15 minutes ago.", value);
			return new Date(System.currentTimeMillis() - 15 * TimeHelper.ONE_MINUTE);
		}
	}

	private void writeText(HttpServletResponse response, String body) throws IOException {
		response.setContentType("text/html;charset=utf-8");

		try (PrintWriter writer = response.getWriter()) {
			writer.write(body);
		}
	}

	public static class AlertDomain {
		private final String name;

		private final Map<String, List<AlertDO>> alertsByCategory = new HashMap<String, List<AlertDO>>();

		AlertDomain(String name) {
			this.name = name;
		}

		void addAlert(AlertDO alert) {
			String category = alert.getCategory();
			List<AlertDO> alerts = alertsByCategory.get(category);

			if (alerts == null) {
				alerts = new ArrayList<AlertDO>();
				alertsByCategory.put(category, alerts);
			}
			alerts.add(alert);
		}

		public Map<String, List<AlertDO>> getAlertCategories() {
			return alertsByCategory;
		}

		public int getCount() {
			int count = 0;

			for (List<AlertDO> alerts : alertsByCategory.values()) {
				count += alerts.size();
			}
			return count;
		}

		public String getName() {
			return name;
		}
	}

	public static class AlertMinute {
		private final Map<String, AlertDomain> domains = new HashMap<String, AlertDomain>();

		private final String time;

		AlertMinute(String time) {
			this.time = time;
		}

		void addAlert(AlertDO alert) {
			String domain = alert.getDomain();
			AlertDomain alertDomain = domains.get(domain);

			if (alertDomain == null) {
				alertDomain = new AlertDomain(domain);
				domains.put(domain, alertDomain);
			}
			alertDomain.addAlert(alert);
		}

		public List<AlertDomain> getAlertDomains() {
			List<AlertDomain> alertDomains = new ArrayList<AlertDomain>(domains.values());

			Collections.sort(alertDomains, new Comparator<AlertDomain>() {
				@Override
				public int compare(AlertDomain domain1, AlertDomain domain2) {
					return domain2.getCount() - domain1.getCount();
				}
			});
			return alertDomains;
		}

		public String getTime() {
			return time;
		}
	}
}
