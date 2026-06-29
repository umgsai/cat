package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.SampleConfig;
import com.dianping.cat.service.ProjectService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcTopControllerTest {
	@Test
	public void shouldBuildViewModel() throws Exception {
		SpringMvcTopController controller = controller();
		Map<String, Object> model = controller.topModel(request("view"));

		Assert.assertEquals("cat", model.get("domain"));
		Assert.assertEquals("All", model.get("ipAddress"));
		Assert.assertNotNull(model.get("topMetric"));
		Assert.assertEquals("/cat/mvc/r/top", model.get("baseUri"));
	}

	@Test
	public void shouldReturnTopMetricJsonForApi() throws Exception {
		SpringMvcTopController controller = controller();
		String json = controller.topResponse(request("api"), "api");

		Assert.assertTrue(json.contains("\"error\""));
		Assert.assertTrue(json.contains("\"result\""));
	}

	@Test
	public void shouldReturnDomainInfoJsonForHealth() throws Exception {
		SpringMvcTopController controller = controller();
		String json = controller.topResponse(request("health"), "health");

		Assert.assertTrue(json.contains("\"metrics\""));
	}

	private SpringMvcTopController controller() {
		SpringMvcTopController controller = new SpringMvcTopController();

		controller.setJsonBuilder(new JsonBuilder());
		controller.setTopModelService(new StubModelService<TopReport>() {
			@Override
			TopReport report(ModelRequest request) {
				return new TopReport("cat").setStartTime(new Date(request.getStartTime()))
						.setEndTime(new Date(request.getStartTime() + 3599999));
			}
		});
		controller.setTopReportService(new TopReportService() {
			@Override
			public TopReport queryReport(String domain, Date start, Date end) {
				return new TopReport(domain).setStartTime(start).setEndTime(end);
			}
		});
		controller.setTransactionModelService(new StubModelService<TransactionReport>() {
			@Override
			TransactionReport report(ModelRequest request) {
				return new TransactionReport(request.getDomain()).setStartTime(new Date(request.getStartTime()))
						.setEndTime(new Date(request.getStartTime() + 3599999));
			}
		});
		controller.setProblemModelService(new StubModelService<ProblemReport>() {
			@Override
			ProblemReport report(ModelRequest request) {
				return new ProblemReport(request.getDomain()).setStartTime(new Date(request.getStartTime()))
						.setEndTime(new Date(request.getStartTime() + 3599999));
			}
		});
		controller.setTransactionMergeHelper(new TransactionMergeHelper());
		setField(controller, "exceptionRuleConfigManager", new ExceptionRuleConfigManager());
		setField(controller, "domainGroupConfigManager", new DomainGroupConfigManager() {
			@Override
			public java.util.List<String> queryDomainGroup(String domain) {
				return Collections.emptyList();
			}
		});
		setField(controller, "projectService", new ProjectService() {
			@Override
			public java.util.Set<String> findAllDomains() {
				return Collections.singleton("cat");
			}

			@Override
			public Map<String, Department> findDepartments(java.util.Collection<String> domains) {
				return Collections.emptyMap();
			}
		});
		setField(controller, "sampleConfigManager", new SampleConfigManager() {
			@Override
			public SampleConfig getConfig() {
				return new SampleConfig();
			}
		});
		setField(controller, "stateBuilder", new com.dianping.cat.report.page.state.StateBuilder() {
			@Override
			public String buildStateMessage(long date, String ip) {
				return "";
			}
		});
		return controller;
	}

	private HttpServletRequest request(String action) throws Exception {
		Map<String, String> parameters = new java.util.HashMap<String, String>();

		parameters.put("op", action);
		parameters.put("domain", "cat");
		parameters.put("ip", "All");
		parameters.put("date", new SimpleDateFormat("yyyyMMddHH").format(new Date(System.currentTimeMillis() - 3600000)));
		parameters.put("minute", "0");
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getContextPath".equals(method.getName())) {
							return "/cat";
						}
						if ("getParameter".equals(method.getName())) {
							return parameters.get(args[0]);
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcTopControllerTestRequest";
						}
						return null;
					}
				});
	}

	private void setField(Object target, String field, Object value) {
		try {
			java.lang.reflect.Field declaredField = target.getClass().getDeclaredField(field);

			declaredField.setAccessible(true);
			declaredField.set(target, value);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private abstract static class StubModelService<T> implements ModelService<T> {
		@Override
		public String getName() {
			return "stub";
		}

		@Override
		public ModelResponse<T> invoke(ModelRequest request) {
			ModelResponse<T> response = new ModelResponse<T>();

			response.setModel(report(request));
			return response;
		}

		@Override
		public boolean isEligible(ModelRequest request) {
			return true;
		}

		abstract T report(ModelRequest request);
	}
}
