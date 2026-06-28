package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import com.dianping.cat.consumer.storage.builder.StorageSQLBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.mybatis.AlterationRepository;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpringMvcAlterationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcAlterationController.class);

	private static final String EMPTY = "N/A";

	private final SimpleDateFormat minuteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private final SimpleDateFormat secondFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private AlterationRepository alterationRepository;

	@RequestMapping("/mvc/r/alteration")
	public void alteration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = action(request);

		if ("insert".equals(action)) {
			writeInsertResult(request, response);
			return;
		}

		Map<String, Object> model = alterationModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/alteration/alteration.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> alterationModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		Date startTime = startTime(request.getParameter("startTime"));
		Date endTime = endTime(request.getParameter("endTime"));
		String type = emptyToNull(request.getParameter("type"));
		String domain = emptyToNull(request.getParameter("domain"));
		String hostname = emptyToNull(request.getParameter("hostname"));
		String altType = request.getParameter("altType");
		String[] altTypes = altTypeArray(altType);
		List<Alteration> alterations = new ArrayList<Alteration>();

		try {
			if (altTypes == null) {
				alterations = alterationRepository.findByDtdh(startTime, endTime, type, domain, hostname);
			} else {
				alterations = alterationRepository.findByDtdhTypes(startTime, endTime, type, domain, hostname, altTypes);
			}
		} catch (EmptyResultDataAccessException e) {
			// ignore it
		} catch (Exception e) {
			LOGGER.error("Unable to query alterations, startTime={}, endTime={}, type={}, domain={}, hostname={}.",
					startTime, endTime, type, domain, hostname, e);
			Cat.logError(e);
		}

		model.put("action", "view");
		model.put("activeReport", "Alteration");
		model.put("altType", altType);
		model.put("alterationMinutes", generateAlterationMinutes(alterations));
		model.put("contextPath", request.getContextPath());
		model.put("count", Integer.valueOf(intParameter(request, "count", 10)));
		model.put("date", "");
		model.put("domain", domain);
		model.put("endTime", endTime);
		model.put("hostname", hostname);
		model.put("ipAddress", "All");
		model.put("reportType", "day");
		model.put("startTime", startTime);
		model.put("type", type);
		return model;
	}

	void setAlterationRepository(AlterationRepository repository) {
		alterationRepository = repository;
	}

	private String action(HttpServletRequest request) {
		String action = request.getParameter("op");

		return "insert".equals(action) ? "insert" : "view";
	}

	private String[] altTypeArray(String altType) {
		if (StringUtils.isEmpty(altType)) {
			return null;
		}
		return altType.split(",");
	}

	private Alteration buildAlteration(AlterationPayload payload) {
		Alteration alt = new Alteration();

		alt.setType(payload.type);
		alt.setDomain(payload.domain);
		alt.setTitle(payload.title);
		alt.setIp(payload.ip);
		alt.setUser(payload.user);
		alt.setAltGroup(payload.group);
		alt.setContent(payload.content);
		alt.setHostname(payload.hostname);
		alt.setDate(payload.alterationDate);
		alt.setStatus(payload.status);
		try {
			alt.setUrl(URLDecoder.decode(payload.url, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Unable to decode alteration url, domain={}, type={}, title={}.", payload.domain, payload.type,
					payload.title, e);
			Cat.logError(e);
			alt.setUrl("");
		}
		return alt;
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
			LOGGER.warn("Unable to parse alteration endTime, value={}. Use current time.", value);
			return new Date();
		}
	}

	private Map<String, AlterationMinute> generateAlterationMinutes(List<Alteration> alterations) {
		Map<String, AlterationMinute> alterationMinutes = new LinkedHashMap<String, AlterationMinute>();
		DateFormat format = new SimpleDateFormat("MM-dd HH:mm");

		for (Alteration alteration : alterations) {
			Date date = alteration.getDate();
			String dateText = format.format(date);
			AlterationMinute minute = alterationMinutes.get(dateText);

			if (minute == null) {
				minute = new AlterationMinute(dateText);
				alterationMinutes.put(dateText, minute);
			}
			minute.add(alteration);
		}
		return alterationMinutes;
	}

	private int intParameter(HttpServletRequest request, String name, int defaultValue) {
		String value = request.getParameter(name);

		if (StringUtils.isNotEmpty(value)) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	private boolean isIllegalArgs(AlterationPayload payload) {
		if (StringUtils.isEmpty(payload.type)) {
			return true;
		} else if (StorageSQLBuilder.ID.equals(payload.type)) {
			return !normalizeSqlArgs(payload);
		} else {
			if (StringUtils.isEmpty(payload.title)) {
				return true;
			}
			if (StringUtils.isEmpty(payload.domain)) {
				return true;
			}
			if (StringUtils.isEmpty(payload.hostname)) {
				return true;
			}
			if (payload.alterationDate == null) {
				return true;
			}
			if (StringUtils.isEmpty(payload.user)) {
				return true;
			}
			if (StringUtils.isEmpty(payload.content)) {
				return true;
			}
			if ("puppet".equals(payload.type)) {
				return true;
			}
		}
		return false;
	}

	private boolean normalizeSqlArgs(AlterationPayload payload) {
		if (StringUtils.isEmpty(payload.title)) {
			return false;
		}
		boolean domainEmpty = StringUtils.isEmpty(payload.domain);
		boolean hostEmpty = StringUtils.isEmpty(payload.hostname);
		boolean ipEmpty = StringUtils.isEmpty(payload.ip);

		if (ipEmpty && domainEmpty && hostEmpty) {
			return false;
		}
		if (domainEmpty) {
			payload.domain = EMPTY;
		}
		if (hostEmpty) {
			payload.hostname = EMPTY;
		}
		if (ipEmpty) {
			payload.ip = EMPTY;
		}
		if (payload.alterationDate == null) {
			payload.alterationDate = new Date();
		}
		if (StringUtils.isEmpty(payload.user)) {
			payload.url = EMPTY;
		}
		if (StringUtils.isEmpty(payload.url)) {
			payload.url = EMPTY;
		}
		return StringUtils.isNotEmpty(payload.content);
	}

	private AlterationPayload payload(HttpServletRequest request) {
		AlterationPayload payload = new AlterationPayload();

		payload.alterationDate = parseAlterationDate(request.getParameter("alterationDate"));
		payload.content = request.getParameter("content");
		payload.domain = emptyToNull(request.getParameter("domain"));
		payload.group = request.getParameter("group");
		payload.hostname = emptyToNull(request.getParameter("hostname"));
		payload.ip = request.getParameter("ip");
		payload.status = intParameter(request, "status", 0);
		payload.title = title(request.getParameter("title"));
		payload.type = request.getParameter("type");
		payload.url = request.getParameter("url");
		payload.user = request.getParameter("user");
		return payload;
	}

	private Date parseAlterationDate(String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		try {
			synchronized (minuteFormat) {
				if (value.length() == 16) {
					return minuteFormat.parse(value);
				}
			}
			synchronized (secondFormat) {
				return secondFormat.parse(value);
			}
		} catch (ParseException e) {
			LOGGER.warn("Unable to parse alterationDate, value={}. Use current time.", value);
			return new Date();
		}
	}

	private Date startTime(String value) {
		if (StringUtils.isEmpty(value)) {
			return new Date(System.currentTimeMillis() - TimeHelper.ONE_HOUR / 4);
		}
		try {
			synchronized (minuteFormat) {
				return minuteFormat.parse(value);
			}
		} catch (ParseException e) {
			LOGGER.warn("Unable to parse alteration startTime, value={}. Use current time.", value);
			return new Date();
		}
	}

	private String title(String value) {
		if (value != null && value.length() > 128) {
			return value.substring(0, 128);
		}
		return value;
	}

	private void writeInsertResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
		AlterationPayload payload = payload(request);
		String result;

		if (isIllegalArgs(payload)) {
			LOGGER.warn("Illegal alteration insert request, type={}, domain={}, hostname={}, title={}.", payload.type,
					payload.domain, payload.hostname, payload.title);
			result = "{\"status\":500, \"errorMessage\":\"lack args\"}";
		} else {
			Alteration alteration = buildAlteration(payload);

			try {
				int count = alterationRepository.insert(alteration);

				if (count == 0) {
					LOGGER.warn("Alteration insert returned zero, type={}, domain={}, title={}.", alteration.getType(),
							alteration.getDomain(), alteration.getTitle());
					result = "{\"status\":500}";
				} else {
					result = "{\"status\":200}";
				}
			} catch (Exception e) {
				LOGGER.error("Unable to insert alteration, type={}, domain={}, title={}.", alteration.getType(),
						alteration.getDomain(), alteration.getTitle(), e);
				Cat.logError(e);
				result = "{\"status\":500}";
			}
		}

		response.setContentType("text/html;charset=utf-8");
		response.getWriter().write(result);
	}

	static class AlterationDomain {
		private final Map<String, List<Alteration>> alterationsByType = new HashMap<String, List<Alteration>>();

		private final String name;

		AlterationDomain(String domain) {
			name = domain;
		}

		public void add(Alteration alteration) {
			String type = alteration.getType();
			List<Alteration> alterations = alterationsByType.get(type);

			if (alterations == null) {
				alterations = new ArrayList<Alteration>();
				alterationsByType.put(type, alterations);
			}
			alterations.add(alteration);
		}

		public Map<String, List<Alteration>> getAlterationTypes() {
			return alterationsByType;
		}

		public int getCount() {
			int count = 0;

			for (List<Alteration> alterations : alterationsByType.values()) {
				count += alterations.size();
			}
			return count;
		}

		public String getName() {
			return name;
		}
	}

	static class AlterationMinute {
		private final String date;

		private final Map<String, AlterationDomain> domains = new HashMap<String, AlterationDomain>();

		AlterationMinute(String date) {
			this.date = date;
		}

		public void add(Alteration alteration) {
			String domain = alteration.getDomain();
			AlterationDomain alterationDomain = domains.get(domain);

			if (alterationDomain == null) {
				alterationDomain = new AlterationDomain(domain);
				domains.put(domain, alterationDomain);
			}
			alterationDomain.add(alteration);
		}

		public String getDate() {
			return date;
		}

		public List<AlterationDomain> getAlterationDomains() {
			List<AlterationDomain> result = new ArrayList<AlterationDomain>(domains.values());

			Collections.sort(result, new Comparator<AlterationDomain>() {
				@Override
				public int compare(AlterationDomain o1, AlterationDomain o2) {
					return o2.getCount() - o1.getCount();
				}
			});
			return result;
		}
	}

	private static class AlterationPayload {
		private Date alterationDate;

		private String content;

		private String domain;

		private String group;

		private String hostname;

		private String ip;

		private int status;

		private String title;

		private String type;

		private String url;

		private String user;
	}
}
