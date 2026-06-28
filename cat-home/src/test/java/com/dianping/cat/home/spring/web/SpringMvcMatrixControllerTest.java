package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.report.page.matrix.DisplayMatrix;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcMatrixControllerTest {
	@Test
	public void shouldBuildHourlyMatrixModelFromRequest() throws Exception {
		SpringMvcMatrixController controller = new SpringMvcMatrixController();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");

		controller.setMatrixModelService(new ModelService<MatrixReport>() {
			@Override
			public String getName() {
				return "matrix";
			}

			@Override
			public ModelResponse<MatrixReport> invoke(ModelRequest request) {
				Assert.assertEquals("cat", request.getDomain());
				Assert.assertEquals("All", request.getProperty("ip"));
				Assert.assertEquals("2026062720", format.format(new Date(request.getStartTime())));

				ModelResponse<MatrixReport> response = new ModelResponse<MatrixReport>();
				MatrixReport report = new MatrixReport("cat");

				report.setStartTime(new Date(request.getStartTime()));
				report.setEndTime(new Date(request.getStartTime() + 3599999));
				response.setModel(report);
				return response;
			}

			@Override
			public boolean isEligable(ModelRequest request) {
				return true;
			}
		});

		Map<String, Object> model = controller.matrixModel(request("view"));

		Assert.assertEquals("/cat", model.get("contextPath"));
		Assert.assertEquals("view", model.get("action"));
		Assert.assertEquals("cat", model.get("domain"));
		Assert.assertEquals("All", model.get("ipAddress"));
		Assert.assertEquals("2026062720", model.get("date"));
		Assert.assertEquals("Count", model.get("sort"));
		Assert.assertEquals(false, model.get("historyMode"));
		Assert.assertTrue(model.get("matrix") instanceof DisplayMatrix);
	}

	@Test
	public void shouldBuildHistoryMatrixModelFromRequest() throws Exception {
		SpringMvcMatrixController controller = new SpringMvcMatrixController();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

		controller.setMatrixReportService(new MatrixReportService() {
			@Override
			public MatrixReport queryReport(String domain, Date start, Date end) {
				Assert.assertEquals("cat", domain);
				Assert.assertEquals("20260627", format.format(start));
				Assert.assertEquals("20260628", format.format(end));

				MatrixReport report = new MatrixReport(domain);

				report.setStartTime(start);
				report.setEndTime(end);
				return report;
			}
		});

		Map<String, Object> model = controller.matrixModel(request("history"));

		Assert.assertEquals("history", model.get("action"));
		Assert.assertEquals("day", model.get("reportType"));
		Assert.assertEquals("20260627", model.get("date"));
		Assert.assertEquals(true, model.get("historyMode"));
		Assert.assertEquals("&startDate=20260627&endDate=20260628", model.get("customDate"));
		Assert.assertTrue(model.get("matrix") instanceof DisplayMatrix);
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
							return "SpringMvcMatrixControllerTestRequest";
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
		if ("sort".equals(name)) {
			return "Count";
		}
		return null;
	}
}
