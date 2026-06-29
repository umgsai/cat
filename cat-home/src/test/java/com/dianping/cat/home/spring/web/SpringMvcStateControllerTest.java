package com.dianping.cat.home.spring.web;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.home.spring.view.state.StateGraphBuilder;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.state.StateBuilder;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcStateControllerTest {
	@Test
	public void shouldBuildGraphModelsWithoutLegacyPayload() {
		SpringMvcStateController controller = controller();

		Map<String, Object> graph = controller.stateModel(request("graph"));
		Map<String, Object> historyGraph = controller.stateModel(request("historyGraph"));

		Assert.assertEquals("Total", graph.get("key"));
		Assert.assertTrue(((String) graph.get("graph")).contains("Total"));
		Assert.assertEquals("Total", historyGraph.get("key"));
		Assert.assertTrue(((String) historyGraph.get("pieChart")).contains("items"));
	}

	private SpringMvcStateController controller() {
		SpringMvcStateController controller = new SpringMvcStateController();

		setField(controller, "serverFilterConfigManager", new ServerFilterConfigManager());
		setField(controller, "stateBuilder", new StateBuilder());
		setField(controller, "stateReportService", new StateReportService() {
			@Override
			public StateReport queryReport(String domain, Date start, Date end) {
				return report(start.getTime(), end.getTime());
			}
		});
		setField(controller, "stateGraphBuilder", new StateGraphBuilder() {
			@Override
			public Pair<LineChart, PieChart> buildGraph(String domain, Date start, Date end, String ip, String key) {
				return Pair.of(lineChart(start, key), new PieChart());
			}

			@Override
			public Pair<LineChart, PieChart> buildGraph(String domain, String ip, String key, StateReport report) {
				return Pair.of(lineChart(report.getStartTime(), key), new PieChart());
			}
		});
		setField(controller, "stateModelService", new ModelService<StateReport>() {
			@Override
			public String getName() {
				return "state";
			}

			@Override
			public ModelResponse<StateReport> invoke(ModelRequest request) {
				ModelResponse<StateReport> response = new ModelResponse<StateReport>();

				response.setModel(report(request.getStartTime(), request.getStartTime() + 60 * 60 * 1000 - 1));
				return response;
			}

			@Override
			public boolean isEligible(ModelRequest request) {
				return true;
			}
		});
		return controller;
	}

	private LineChart lineChart(Date start, String key) {
		LineChart chart = new LineChart();

		chart.setStart(start).setSize(1).setTitle(key).setStep(60 * 1000);
		chart.add(key, new Double[] { 1.0 });
		return chart;
	}

	private HttpServletRequest request(String action) {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("op", action);
		parameters.put("domain", "cat");
		parameters.put("ip", "All");
		parameters.put("date", "2026062720");
		parameters.put("reportType", "day");
		parameters.put("key", "Total");

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
							return "SpringMvcStateControllerTestRequest";
						}
						return null;
					}
				});
	}

	private StateReport report(long start, long end) {
		StateReport report = new StateReport("cat");

		report.setStartTime(new Date(start));
		report.setEndTime(new Date(end));
		return report;
	}

	private void setField(Object target, String field, Object value) {
		try {
			Field declaredField = target.getClass().getDeclaredField(field);

			declaredField.setAccessible(true);
			declaredField.set(target, value);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
