package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcStatisticsControllerTest {
	@Test
	public void shouldBuildDefaultServiceReportModel() throws Exception {
		SpringMvcStatisticsController controller = new SpringMvcStatisticsController();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");

		controller.setServiceReportService(new ServiceReportService() {
			@Override
			public ServiceReport queryReport(String domain, Date start, Date end) {
				Assert.assertEquals("cat", domain);
				Assert.assertEquals("2026062720", format.format(start));
				Assert.assertEquals("2026062721", format.format(end));

				ServiceReport report = new ServiceReport(domain);

				report.setStartTime(start);
				report.setEndTime(end);
				return report;
			}
		});

		Map<String, Object> model = controller.statisticsModel(request("service"));

		Assert.assertEquals("/cat", model.get("contextPath"));
		Assert.assertEquals("service", model.get("action"));
		Assert.assertEquals("2026062720", model.get("date"));
		Assert.assertEquals(false, model.get("historyMode"));
		Assert.assertTrue(model.get("serviceReport") instanceof ServiceReport);
	}

	@Test
	public void shouldBuildHistoryServiceReportModel() throws Exception {
		SpringMvcStatisticsController controller = new SpringMvcStatisticsController();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

		controller.setServiceReportService(new ServiceReportService() {
			@Override
			public ServiceReport queryReport(String domain, Date start, Date end) {
				Assert.assertEquals("cat", domain);
				Assert.assertEquals("20260627", format.format(start));
				Assert.assertEquals("20260628", format.format(end));

				ServiceReport report = new ServiceReport(domain);

				report.setStartTime(start);
				report.setEndTime(end);
				return report;
			}
		});

		Map<String, Object> model = controller.statisticsModel(request("historyService"));

		Assert.assertEquals("historyService", model.get("action"));
		Assert.assertEquals("day", model.get("reportType"));
		Assert.assertEquals("20260627", model.get("date"));
		Assert.assertEquals(true, model.get("historyMode"));
		Assert.assertEquals("&startDate=20260627&endDate=20260628", model.get("customDate"));
		Assert.assertTrue(model.get("serviceReport") instanceof ServiceReport);
	}

	@Test
	public void shouldBuildClientReportModel() throws Exception {
		SpringMvcStatisticsController controller = new SpringMvcStatisticsController();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

		controller.setClientReportService(new ClientReportService() {
			@Override
			public ClientReport queryReport(String domain, Date start, Date end) {
				Assert.assertEquals("cat", domain);
				Assert.assertEquals("20260627", format.format(start));
				Assert.assertEquals("20260628", format.format(end));

				ClientReport report = new ClientReport(domain);

				report.setStartTime(start);
				report.setEndTime(end);
				return report;
			}
		});

		Map<String, Object> model = controller.statisticsModel(request("client"));

		Assert.assertEquals("client", model.get("action"));
		Assert.assertEquals("2026-06-27", model.get("day"));
		Assert.assertTrue(model.get("clientReport") instanceof ClientReport);
	}

	@Test
	public void shouldBuildSummaryModel() throws Exception {
		SpringMvcStatisticsController controller = new SpringMvcStatisticsController();

		controller.setAlertSummaryExecutor(new AlertSummaryExecutor() {
			@Override
			public String execute(String domain, Date date, String receiverStr) {
				Assert.assertEquals("cat", domain);
				Assert.assertEquals("a@example.com", receiverStr);
				return "<b>ok</b>";
			}
		});

		Map<String, Object> model = controller.statisticsModel(request("summary"));

		Assert.assertEquals("summary", model.get("action"));
		Assert.assertEquals("<b>ok</b>", model.get("summaryContent"));
		Assert.assertEquals("cat", model.get("summarydomain"));
	}

	private HttpServletRequest request(String action) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getContextPath".equals(method.getName())) {
							return "/cat";
						}
						if ("getParameter".equals(method.getName())) {
							return parameter(action, (String) args[0]);
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcStatisticsControllerTestRequest";
						}
						return null;
					}
				});
	}

	private String parameter(String action, String name) {
		if ("op".equals(name)) {
			return action;
		}
		if ("domain".equals(name)) {
			return "cat";
		}
		if ("ip".equals(name)) {
			return "All";
		}
		if ("date".equals(name)) {
			return "historyService".equals(action) ? "2026062700" : "2026062720";
		}
		if ("reportType".equals(name)) {
			return "day";
		}
		if ("day".equals(name)) {
			return "2026-06-27";
		}
		if ("summarydomain".equals(name)) {
			return "cat";
		}
		if ("summarytime".equals(name)) {
			return "2026-06-27 20:00";
		}
		if ("summaryemails".equals(name)) {
			return "a@example.com";
		}
		return null;
	}
}
