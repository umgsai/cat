package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.page.cache.CacheReport;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcCacheControllerTest {
	@Test
	public void shouldBuildHourlyCacheModelFromRequest() throws Exception {
		SpringMvcCacheController controller = new SpringMvcCacheController();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");

		controller.setTransactionModelService(new ModelService<TransactionReport>() {
			@Override
			public String getName() {
				return "transaction";
			}

			@Override
			public ModelResponse<TransactionReport> invoke(ModelRequest request) {
				Assert.assertEquals("cat", request.getDomain());
				Assert.assertEquals("All", request.getProperty("ip"));
				Assert.assertEquals("2026062720", format.format(new Date(request.getStartTime())));

				ModelResponse<TransactionReport> response = new ModelResponse<TransactionReport>();
				TransactionReport report = new TransactionReport("cat");

				report.setStartTime(new Date(request.getStartTime()));
				report.setEndTime(new Date(request.getStartTime() + 3599999));
				response.setModel(report);
				return response;
			}

			@Override
			public boolean isEligible(ModelRequest request) {
				return true;
			}
		});
		controller.setEventModelService(new ModelService<EventReport>() {
			@Override
			public String getName() {
				return "event";
			}

			@Override
			public ModelResponse<EventReport> invoke(ModelRequest request) {
				Assert.assertEquals("cat", request.getDomain());
				Assert.assertEquals("All", request.getProperty("ip"));
				Assert.assertEquals("2026062720", format.format(new Date(request.getStartTime())));

				ModelResponse<EventReport> response = new ModelResponse<EventReport>();
				EventReport report = new EventReport("cat");

				report.setStartTime(new Date(request.getStartTime()));
				report.setEndTime(new Date(request.getStartTime() + 3599999));
				response.setModel(report);
				return response;
			}

			@Override
			public boolean isEligible(ModelRequest request) {
				return true;
			}
		});

		Map<String, Object> model = controller.cacheModel(request("view"));

		Assert.assertEquals("/cat", model.get("contextPath"));
		Assert.assertEquals("view", model.get("action"));
		Assert.assertEquals("cat", model.get("domain"));
		Assert.assertEquals("All", model.get("ipAddress"));
		Assert.assertEquals("2026062720", model.get("date"));
		Assert.assertEquals(false, model.get("historyMode"));
		Assert.assertTrue(model.get("report") instanceof CacheReport);
	}

	@Test
	public void shouldBuildHistoryCacheModelFromRequest() throws Exception {
		SpringMvcCacheController controller = new SpringMvcCacheController();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

		controller.setTransactionReportService(new TransactionReportService() {
			@Override
			public TransactionReport queryReport(String domain, Date start, Date end) {
				Assert.assertEquals("cat", domain);
				Assert.assertEquals("20260627", format.format(start));
				Assert.assertEquals("20260628", format.format(end));

				TransactionReport report = new TransactionReport(domain);

				report.setStartTime(start);
				report.setEndTime(end);
				return report;
			}
		});
		controller.setEventReportService(new EventReportService() {
			@Override
			public EventReport queryReport(String domain, Date start, Date end) {
				Assert.assertEquals("cat", domain);
				Assert.assertEquals("20260627", format.format(start));
				Assert.assertEquals("20260628", format.format(end));

				EventReport report = new EventReport(domain);

				report.setStartTime(start);
				report.setEndTime(end);
				return report;
			}
		});

		Map<String, Object> model = controller.cacheModel(request("history"));

		Assert.assertEquals("history", model.get("action"));
		Assert.assertEquals("day", model.get("reportType"));
		Assert.assertEquals("20260627", model.get("date"));
		Assert.assertEquals(true, model.get("historyMode"));
		Assert.assertEquals("&startDate=20260627&endDate=20260628", model.get("customDate"));
		Assert.assertTrue(model.get("report") instanceof CacheReport);
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
							return "SpringMvcCacheControllerTestRequest";
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
			return "history".equals(action) ? "2026062700" : "2026062720";
		}
		if ("reportType".equals(name)) {
			return "day";
		}
		return null;
	}
}
