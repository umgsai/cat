package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.mybatis.alert.dao.data.AlertDO;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.consumer.storage.builder.StorageBuilderManager;
import com.dianping.cat.consumer.storage.builder.StorageSQLBuilder;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.mvc.HistoryNav;
import com.dianping.cat.mvc.UrlNav;
import com.dianping.cat.mybatis.AlterationRepository;
import com.dianping.cat.mybatis.data.AlterationDO;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.storage.StorageConstants;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager.Department;
import com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder;
import com.dianping.cat.report.page.storage.display.StorageSorter;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.storage.transform.HourlyLineChartVisitor;
import com.dianping.cat.report.page.storage.transform.PieChartVisitor;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.page.storage.transform.StorageOperationFilter;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcStorageController {
	private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final SimpleDateFormat hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	@Resource
	private AlertService alertService;

	@Resource
	private AlterationRepository alterationRepository;

	@Resource
	private JsonBuilder jsonBuilder;

	@Resource
	private StorageAlertInfoBuilder storageAlertInfoBuilder;

	@Resource
	private StorageBuilderManager storageBuilderManager;

	@Resource
	private StorageGroupConfigManager storageGroupConfigManager;

	@Resource
	private StorageMergeHelper storageMergeHelper;

	@Resource(name = "storageModelService")
	private ModelService<StorageReport> storageModelService;

	@Resource
	private StorageReportService storageReportService;

	@GetMapping("/mvc/r/storage")
	public void storage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = storageModel(request);
		String action = (String) model.get("action");
		String view;

		if ("hourlyGraph".equals(action)) {
			view = "/jsp/spring/report/storage/hourlyGraphs.jsp";
		} else if ("dashboard".equals(action)) {
			view = "/jsp/spring/report/storage/dashboard.jsp";
		} else {
			view = "/jsp/spring/report/storage/storage.jsp";
		}

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(view);

		dispatcher.forward(request, response);
	}

	Map<String, Object> storageModel(HttpServletRequest request) {
		Map<String, Object> model = baseModel(request);
		String action = (String) model.get("action");
		String type = (String) model.get("type");
		String id = (String) model.get("id");
		String ipAddress = (String) model.get("ipAddress");
		String operations = (String) model.get("operations");
		String sort = (String) model.get("sort");
		DateRange range = (DateRange) model.get("range");

		if ("dashboard".equals(action)) {
			buildDashboard(model, range, type);
			return model;
		}

		if (StringUtils.isEmpty(operations)) {
			List<String> defaultMethods = storageBuilderManager.getDefaultMethods(type);

			operations = StringUtils.join(defaultMethods, ";");
			model.put("operationsParam", operations);
			model.put("encodedOperations", encode(operations));
		}

		StorageReport report = "history".equals(action) ? queryHistoryReport(id, type, range.getStart(), range.getEnd())
				: queryHourlyReport(id, type, range.getQueryTime(), ipAddress);
		StorageReport originalReport = report;
		StorageReport filteredReport = filterReport(model, report, operations);
		StorageReport mergedReport = mergeReport(filteredReport, ipAddress, sort, range);
		List<String> operationOptions = operationOptions(model, operations);

		model.put("originalReport", originalReport);
		model.put("report", mergedReport);
		model.put("machine", machine(mergedReport, ipAddress));
		model.put("ips", mergedReport == null ? new ArrayList<String>() : SortHelper.sortIpAddress(mergedReport.getIps()));
		model.put("departments", departments(mergedReport, type));
		model.put("currentOperations", currentOperations(mergedReport));
		model.put("operations", operationOptions);
		model.put("operationColumnCount", Integer.valueOf(operationOptions.size() * 4 + 2));
		model.put("reportStart", displayFormat.format(range.getDisplayStart()));
		model.put("reportEnd", displayFormat.format(range.getDisplayEnd()));

		if ("hourlyGraph".equals(action)) {
			if (filteredReport != null && Constants.ALL.equals(ipAddress)) {
				buildPieCharts(model, filteredReport);
			}
			buildLineCharts(model, mergedReport);
		}
		return model;
	}

	void setAlertService(AlertService service) {
		alertService = service;
	}

	void setAlterationRepository(AlterationRepository repository) {
		alterationRepository = repository;
	}

	void setJsonBuilder(JsonBuilder builder) {
		jsonBuilder = builder;
	}

	void setStorageAlertInfoBuilder(StorageAlertInfoBuilder builder) {
		storageAlertInfoBuilder = builder;
	}

	void setStorageBuilderManager(StorageBuilderManager manager) {
		storageBuilderManager = manager;
	}

	void setStorageGroupConfigManager(StorageGroupConfigManager manager) {
		storageGroupConfigManager = manager;
	}

	void setStorageMergeHelper(StorageMergeHelper helper) {
		storageMergeHelper = helper;
	}

	void setStorageModelService(ModelService<StorageReport> service) {
		storageModelService = service;
	}

	void setStorageReportService(StorageReportService service) {
		storageReportService = service;
	}

	private Map<String, Object> baseModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String action = normalizeAction(parameter(request, "op", "view"));
		String domain = parameter(request, "domain", Constants.CAT);
		String id = parameter(request, "id", Constants.CAT);
		String ipAddress = parameter(request, "ip", Constants.ALL);
		String reportType = normalizeReportType(parameter(request, "reportType", "day"));
		String type = parameter(request, "type", StorageSQLBuilder.ID);
		String operations = parameter(request, "operations", "");
		String project = parameter(request, "project", "");
		String sort = parameter(request, "sort", "domain");
		DateRange range = dateRange(request, action, reportType);

		model.put("action", action);
		model.put("activeReport", "Storage");
		model.put("baseUri", request.getContextPath() + "/mvc/r/storage");
		model.put("contextPath", request.getContextPath());
		model.put("customDate", range.getCustomDate());
		model.put("date", range.getDateText());
		model.put("displayDomain", domain);
		model.put("domain", domain);
		model.put("encodedDomain", encode(domain));
		model.put("encodedId", encode(id));
		model.put("encodedIpAddress", encode(ipAddress));
		model.put("encodedOperations", encode(operations));
		model.put("encodedProject", encode(project));
		model.put("encodedType", encode(type));
		model.put("frequency", intParameter(request, "frequency", 10));
		model.put("fullScreen", booleanParameter(request, "fullScreen", false));
		model.put("historyMode", "history".equals(action));
		model.put("historyNavs", HistoryNav.values());
		model.put("id", id);
		model.put("ipAddress", ipAddress);
		model.put("minuteCounts", intParameter(request, "count", 8));
		model.put("minute", request.getParameter("minute"));
		model.put("navs", UrlNav.values());
		model.put("operationsParam", operations);
		model.put("project", project);
		model.put("range", range);
		model.put("refresh", booleanParameter(request, "refresh", false));
		model.put("reportType", range.getReportType());
		model.put("sort", sort);
		model.put("type", type);
		model.put("currentNav", HistoryNav.getByName(range.getReportType()));
		return model;
	}

	private Map<String, Map<String, List<String>>> buildAlertLinks(Map<String, StorageAlertInfo> alertInfos, String type) {
		Map<String, Map<String, List<String>>> links = new LinkedHashMap<String, Map<String, List<String>>>();
		String format = storageGroupConfigManager.queryLinkFormat(type);

		if (format != null) {
			for (Entry<String, StorageAlertInfo> alertInfo : alertInfos.entrySet()) {
				String key = alertInfo.getKey();
				Map<String, List<String>> linkMap = links.get(key);

				if (linkMap == null) {
					linkMap = new LinkedHashMap<String, List<String>>();
					links.put(key, linkMap);
				}
				for (Entry<String, Storage> entry : alertInfo.getValue().getStorages().entrySet()) {
					String id = entry.getKey();
					Storage storage = entry.getValue();
					List<String> urls = linkMap.get(id);

					if (urls == null) {
						urls = new ArrayList<String>();
						linkMap.put(id, urls);
					}
					for (String ip : storage.getMachines().keySet()) {
						String url = storageGroupConfigManager.buildUrl(format, id, ip);

						if (url != null) {
							urls.add(url);
						}
					}
				}
			}
		}
		return links;
	}

	private List<AlterationDO> buildAlterations(Date start, Date end, String type) {
		List<AlterationDO> results = new LinkedList<AlterationDO>();

		try {
			results.addAll(alterationRepository.findByTypeDruation(start, end, type));
		} catch (EmptyResultDataAccessException e) {
			// ignore it
		} catch (Exception e) {
			Cat.logError(e);
		}
		return results;
	}

	private void buildDashboard(Map<String, Object> model, DateRange range, String type) {
		int minuteCounts = (Integer) model.get("minuteCounts");
		int minute = parseQueryMinute(model, range);
		long time = range.getQueryTime();
		long end = time + minute * TimeHelper.ONE_MINUTE;
		Date startDate = new Date(end - (minuteCounts - 1) * TimeHelper.ONE_MINUTE);
		Date endDate = new Date(end);
		List<AlertDO> alerts = alertService.query(new Date(startDate.getTime() + TimeHelper.ONE_MINUTE),
				new Date(endDate.getTime() + TimeHelper.ONE_MINUTE), type);
		Map<String, StorageAlertInfo> alertInfos = storageAlertInfoBuilder.buildStorageAlertInfos(startDate, endDate,
				minuteCounts, type, alerts);

		alertInfos = sortAlertInfos(alertInfos);
		model.put("alertInfos", alertInfos);
		model.put("alterations", buildAlterations(startDate, endDate, type));
		model.put("links", buildAlertLinks(alertInfos, type));
		model.put("maxMinute", maxMinute(range));
		model.put("minute", minute);
		model.put("minutes", minutes());
		model.put("reportEnd", displayFormat.format(new Date(time + TimeHelper.ONE_HOUR - 1)));
		model.put("reportStart", displayFormat.format(new Date(time)));
		model.put("storageName", storageName(type));
	}

	private void buildLineCharts(Map<String, Object> model, StorageReport report) {
		HourlyLineChartVisitor visitor = new HourlyLineChartVisitor((String) model.get("ipAddress"),
				(String) model.get("project"), report.getOps(), report.getStartTime());

		visitor.visitStorageReport(report);
		Map<String, LineChart> lineCharts = visitor.getLineChart();

		model.put("countTrend", jsonBuilder.toJson(lineCharts.get(StorageConstants.COUNT)));
		model.put("avgTrend", jsonBuilder.toJson(lineCharts.get(StorageConstants.AVG)));
		model.put("errorTrend", jsonBuilder.toJson(lineCharts.get(StorageConstants.ERROR)));
		model.put("longTrend", jsonBuilder.toJson(lineCharts.get(StorageConstants.LONG)));
	}

	private void buildPieCharts(Map<String, Object> model, StorageReport report) {
		PieChartVisitor visitor = new PieChartVisitor();

		visitor.visitStorageReport(report);
		model.put("distributionChart", visitor.getPiechartJson());
	}

	private boolean booleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
		String value = request.getParameter(name);

		return value == null || value.length() == 0 ? defaultValue : Boolean.parseBoolean(value);
	}

	private List<String> currentOperations(StorageReport report) {
		if (report == null) {
			return new ArrayList<String>();
		}

		ArrayList<String> ops = new ArrayList<String>(report.getOps());

		Collections.sort(ops);
		return ops;
	}

	private long currentStartDay() {
		long timestamp = System.currentTimeMillis();
		Date date = new Date(timestamp);
		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime(date);
		cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
		cal.set(java.util.Calendar.MINUTE, 0);
		cal.set(java.util.Calendar.SECOND, 0);
		cal.set(java.util.Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}

	private DateRange dateRange(HttpServletRequest request, String action, String reportType) {
		if ("history".equals(action)) {
			String normalizedReportType = normalizeReportType(reportType);
			long date = normalizeHistoryDate(dateParameter(request.getParameter("date")).getTime(), normalizedReportType,
					intParameter(request, "step", 0));
			Date start = historyStartDate(date, request.getParameter("startDate"));
			Date end = historyEndDate(date, normalizedReportType, request.getParameter("endDate"));

			return DateRange.history(date, normalizedReportType, start, end, dayFormat);
		}

		long queryTime = date(request.getParameter("date"), intParameter(request, "step", 0));
		Date start = new Date(queryTime);
		Date end = new Date(queryTime + TimeHelper.ONE_HOUR - 1);

		return DateRange.hourly(queryTime, start, end, hourlyFormat);
	}

	private long date(String value, int step) {
		long current = System.currentTimeMillis() - TimeHelper.ONE_MINUTE;
		long currentHour = current - current % TimeHelper.ONE_HOUR;
		long result = currentHour;

		if (value != null && value.length() > 0) {
			try {
				result = value.length() == 10 ? hourlyFormat.parse(value).getTime() : dayFormat.parse(value).getTime();
			} catch (ParseException e) {
				result = currentHour;
			}
		}
		result = result + step * TimeHelper.ONE_HOUR;
		return Math.min(result, currentHour);
	}

	private Date dateParameter(String value) {
		if (value != null && value.length() > 0) {
			try {
				return value.length() == 10 ? hourlyFormat.parse(value) : dayFormat.parse(value);
			} catch (ParseException e) {
				// fall through
			}
		}
		return TimeHelper.getCurrentDay(-1);
	}

	private Map<String, Department> departments(StorageReport report, String type) {
		if (report == null) {
			return Collections.emptyMap();
		}
		return storageGroupConfigManager.queryStorageDepartments(SortHelper.sortDomain(report.getIds()), type);
	}

	private String encode(String value) {
		return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private StorageReport filterReport(Map<String, Object> model, StorageReport report, String operations) {
		if (report != null) {
			Set<String> allOps = report.getOps();
			Set<String> ops = new HashSet<String>();

			model.put("operations", sortedOperations(allOps));
			if (operations.length() > 0) {
				String[] selectedOps = operations.split(";");

				for (String op : selectedOps) {
					if (op.length() > 0) {
						ops.add(op);
					}
				}

				StorageOperationFilter filter = new StorageOperationFilter(ops);

				filter.visitStorageReport(report);
				report = filter.getStorageReport();
			}
		}
		return report;
	}

	private Date historyEndDate(long date, String reportType, String customEnd) {
		Date custom = parseCustomDate(customEnd);

		if (custom != null) {
			return custom;
		}

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTimeInMillis(date);
		if ("month".equals(reportType)) {
			cal.add(java.util.Calendar.MONTH, 1);
		} else if ("week".equals(reportType)) {
			cal.add(java.util.Calendar.DATE, 7);
		} else {
			cal.add(java.util.Calendar.DATE, 1);
		}
		return cal.getTime();
	}

	private Date historyStartDate(long date, String customStart) {
		Date custom = parseCustomDate(customStart);

		return custom == null ? new Date(date) : custom;
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

	private Machine machine(StorageReport report, String ipAddress) {
		Machine machine = new Machine();

		if (report != null) {
			Machine found = report.getMachines().get(ipAddress);

			if (found != null) {
				return found;
			}
			if (!report.getMachines().isEmpty()) {
				return report.getMachines().values().iterator().next();
			}
		}
		return machine;
	}

	private int maxMinute(DateRange range) {
		long currentHour = date(null, 0);

		if (range.getQueryTime() == currentHour) {
			long current = (System.currentTimeMillis() - TimeHelper.ONE_MINUTE) / 1000 / 60;

			return (int) (current % 60);
		}
		return 60;
	}

	private StorageReport mergeReport(StorageReport report, String ipAddress, String sort, DateRange range) {
		if (report == null) {
			return new StorageReport().setStartTime(range.getStart()).setEndTime(range.getEnd());
		}
		StorageReport merged = storageMergeHelper.mergeReport(report, ipAddress, Constants.ALL);
		StorageSorter sorter = new StorageSorter(merged, sort);

		return sorter.getSortedReport();
	}

	private List<Integer> minutes() {
		List<Integer> minutes = new ArrayList<Integer>();

		for (int i = 0; i < 60; i++) {
			minutes.add(i);
		}
		return minutes;
	}

	private long normalizeHistoryDate(long date, String reportType, int step) {
		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTimeInMillis(date);
		cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
		cal.set(java.util.Calendar.MINUTE, 0);
		cal.set(java.util.Calendar.SECOND, 0);
		cal.set(java.util.Calendar.MILLISECOND, 0);
		date = cal.getTimeInMillis();

		if ("month".equals(reportType)) {
			cal.set(java.util.Calendar.DATE, 1);
			date = cal.getTimeInMillis();
		} else if ("week".equals(reportType)) {
			int weekOfDay = cal.get(java.util.Calendar.DAY_OF_WEEK) % 7;

			date = date - TimeHelper.ONE_DAY * (weekOfDay % 7);
			if (date > System.currentTimeMillis()) {
				date = date - 7 * TimeHelper.ONE_DAY;
			}
			cal.setTimeInMillis(date);
		}
		if (step < 0) {
			if ("month".equals(reportType)) {
				cal.add(java.util.Calendar.MONTH, step);
				date = cal.getTimeInMillis();
			} else if ("week".equals(reportType)) {
				date = date + 7 * TimeHelper.ONE_DAY * step;
			} else {
				date = date + TimeHelper.ONE_DAY * step;
			}
		} else {
			long temp;

			if ("month".equals(reportType)) {
				cal.add(java.util.Calendar.MONTH, step);
				temp = cal.getTimeInMillis();
			} else if ("week".equals(reportType)) {
				temp = date + 7 * TimeHelper.ONE_DAY * step;
			} else {
				temp = date + TimeHelper.ONE_DAY * step;
			}
			if (temp <= currentStartDay()) {
				date = temp;
			}
		}
		if ("day".equals(reportType) && date == currentStartDay()) {
			date = date - TimeHelper.ONE_DAY;
		}
		return date;
	}

	private String normalizeAction(String action) {
		if ("view".equals(action) || "hourlyGraph".equals(action) || "history".equals(action)
				|| "dashboard".equals(action)) {
			return action;
		}
		return "view";
	}

	private String normalizeReportType(String reportType) {
		if ("month".equals(reportType) || "week".equals(reportType)) {
			return reportType;
		}
		return "day";
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		return value == null || value.length() == 0 ? defaultValue : value;
	}

	@SuppressWarnings("unchecked")
	private List<String> operationOptions(Map<String, Object> model, String operations) {
		Object value = model.get("operations");

		if (value instanceof List) {
			return (List<String>) value;
		}
		return selectedOperations(operations);
	}

	private Date parseCustomDate(String value) {
		if (value != null && value.length() > 0) {
			try {
				if (value.length() == 10) {
					return hourlyFormat.parse(value);
				} else if (value.length() == 8) {
					return dayFormat.parse(value);
				}
			} catch (ParseException e) {
				// ignore invalid custom date
			}
		}
		return null;
	}

	private int parseQueryMinute(Map<String, Object> model, DateRange range) {
		Object minuteValue = model.get("minute");
		String value = minuteValue == null ? null : String.valueOf(minuteValue);

		if (StringUtils.isEmpty(value)) {
			long current = range.getQueryTime() == date(null, 0) ? (System.currentTimeMillis() - TimeHelper.ONE_MINUTE)
					: range.getQueryTime();

			return (int) ((current / 1000 / 60) % 60);
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private StorageReport queryHistoryReport(String id, String type, Date start, Date end) {
		return storageReportService.queryReport(id + "-" + type, start, end);
	}

	private StorageReport queryHourlyReport(String id, String type, long date, String ipAddress) {
		ModelRequest request = new ModelRequest(id + "-" + type, date).setProperty("ip", ipAddress);

		if (storageModelService.isEligible(request)) {
			ModelResponse<StorageReport> response = storageModelService.invoke(request);

			return response.getModel();
		}
		throw new IllegalStateException("No eligible storage model service registered for " + request + ".");
	}

	private List<String> sortedOperations(Set<String> operations) {
		ArrayList<String> result = new ArrayList<String>(operations);

		Collections.sort(result);
		return result;
	}

	private List<String> selectedOperations(String operations) {
		List<String> result = new ArrayList<String>();

		if (StringUtils.isNotEmpty(operations)) {
			for (String operation : operations.split(";")) {
				if (operation.length() > 0) {
					result.add(operation);
				}
			}
		}
		Collections.sort(result);
		return result;
	}

	private Map<String, StorageAlertInfo> sortAlertInfos(Map<String, StorageAlertInfo> alertInfos) {
		Map<String, StorageAlertInfo> results = new LinkedHashMap<String, StorageAlertInfo>();

		for (Entry<String, StorageAlertInfo> entry : alertInfos.entrySet()) {
			StorageAlertInfo alertInfo = entry.getValue();
			List<Entry<String, Storage>> entries = new ArrayList<Entry<String, Storage>>(alertInfo.getStorages().entrySet());

			Collections.sort(entries, new Comparator<Map.Entry<String, Storage>>() {
				@Override
				public int compare(Map.Entry<String, Storage> o1, Map.Entry<String, Storage> o2) {
					int gap = o2.getValue().getLevel() - o1.getValue().getLevel();

					return gap == 0 ? o2.getValue().getCount() - o1.getValue().getCount() : gap;
				}
			});

			StorageAlertInfo result = storageAlertInfoBuilder.makeAlertInfo(alertInfo.getId(), alertInfo.getStartTime());
			Map<String, Storage> storages = result.getStorages();

			for (Entry<String, Storage> storage : entries) {
				storages.put(storage.getKey(), storage.getValue());
			}
			results.put(entry.getKey(), result);
		}
		return SortHelper.sortMap(results, new MinuteComparator());
	}

	private String storageName(String type) {
		if ("Cache".equals(type)) {
			return "缓存";
		}
		if ("RPC".equals(type)) {
			return "服务";
		}
		return "数据库";
	}

	static class MinuteComparator implements Comparator<Map.Entry<String, StorageAlertInfo>> {
		@Override
		public int compare(Map.Entry<String, StorageAlertInfo> o1, Map.Entry<String, StorageAlertInfo> o2) {
			String key1 = o1.getKey();
			String key2 = o2.getKey();
			String hour1 = key1.substring(0, 2);
			String hour2 = key2.substring(0, 2);

			if (!hour1.equals(hour2)) {
				int hour1Value = Integer.parseInt(hour1);
				int hour2Value = Integer.parseInt(hour2);

				if (hour1Value == 0 && hour2Value == 23) {
					return -1;
				} else if (hour1Value == 23 && hour2Value == 0) {
					return 1;
				}
				return hour2Value - hour1Value;
			}

			String first = key1.substring(3, 5);
			String end = key2.substring(3, 5);

			return Integer.parseInt(end) - Integer.parseInt(first);
		}
	}

	private static class DateRange {
		private final String m_customDate;

		private final String m_dateText;

		private final Date m_displayEnd;

		private final Date m_displayStart;

		private final Date m_end;

		private final long m_queryTime;

		private final String m_reportType;

		private final Date m_start;

		private DateRange(long queryTime, String reportType, Date start, Date end, Date displayStart, Date displayEnd,
				String dateText, String customDate) {
			m_queryTime = queryTime;
			m_reportType = reportType;
			m_start = start;
			m_end = end;
			m_displayStart = displayStart;
			m_displayEnd = displayEnd;
			m_dateText = dateText;
			m_customDate = customDate;
		}

		private static DateRange history(long queryTime, String reportType, Date start, Date end,
				SimpleDateFormat dayFormat) {
			String customDate = "&startDate=" + dayFormat.format(start) + "&endDate=" + dayFormat.format(end);

			return new DateRange(queryTime, reportType, start, end, start, new Date(end.getTime() - 1000),
					dayFormat.format(new Date(queryTime)), customDate);
		}

		private static DateRange hourly(long queryTime, Date start, Date end, SimpleDateFormat hourlyFormat) {
			return new DateRange(queryTime, "day", start, end, start, end, hourlyFormat.format(new Date(queryTime)), "");
		}

		private String getCustomDate() {
			return m_customDate;
		}

		private String getDateText() {
			return m_dateText;
		}

		private Date getDisplayEnd() {
			return m_displayEnd;
		}

		private Date getDisplayStart() {
			return m_displayStart;
		}

		private Date getEnd() {
			return m_end;
		}

		private long getQueryTime() {
			return m_queryTime;
		}

		private String getReportType() {
			return m_reportType;
		}

		private Date getStart() {
			return m_start;
		}
	}
}
