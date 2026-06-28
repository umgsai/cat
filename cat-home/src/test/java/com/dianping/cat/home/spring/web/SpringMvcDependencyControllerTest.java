package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.graph.ProductLinesDashboard;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcDependencyControllerTest {
	@Test
	public void shouldBuildLineChartModel() throws Exception {
		SpringMvcDependencyController controller = controller();
		Map<String, Object> model = controller.dependencyModel(request("lineChart"));

		Assert.assertEquals("lineChart", model.get("action"));
		Assert.assertEquals("cat", model.get("domain"));
		Assert.assertNotNull(model.get("report"));
		Assert.assertNotNull(model.get("segment"));
		Assert.assertNotNull(model.get("indexGraph"));
		Assert.assertNotNull(model.get("dependencyGraph"));
	}

	@Test
	public void shouldBuildTopologyModel() throws Exception {
		SpringMvcDependencyController controller = controller();
		Map<String, Object> model = controller.dependencyModel(request("dependencyGraph"));

		Assert.assertEquals("dependencyGraph", model.get("action"));
		Assert.assertNotNull(model.get("topologyGraph"));
		Assert.assertNotNull(model.get("segment"));
	}

	@Test
	public void shouldBuildDashboardModel() throws Exception {
		SpringMvcDependencyController controller = controller();
		Map<String, Object> model = controller.dependencyModel(request("dashboard"));

		Assert.assertEquals("dashboard", model.get("action"));
		Assert.assertNotNull(model.get("dashboardGraph"));
		Assert.assertEquals("{}", model.get("format"));
	}

	private SpringMvcDependencyController controller() throws Exception {
		SpringMvcDependencyController controller = new SpringMvcDependencyController();

		controller.setDependencyModelService(new ModelService<DependencyReport>() {
			@Override
			public String getName() {
				return "dependencyModelService";
			}

			@Override
			public ModelResponse<DependencyReport> invoke(ModelRequest request) {
				ModelResponse<DependencyReport> response = new ModelResponse<DependencyReport>();

				response.setModel(report(request.getDomain(), request.getStartTime()));
				return response;
			}

			@Override
			public boolean isEligable(ModelRequest request) {
				return true;
			}
		});
		controller.setTopologyGraphManager(new TopologyGraphManager() {
			@Override
			public ProductLinesDashboard buildDependencyDashboard(long time) {
				return new ProductLinesDashboard();
			}

			@Override
			public TopologyGraph buildTopologyGraph(String domain, long time) {
				TopologyGraph graph = new TopologyGraph();

				graph.setId(domain);
				graph.addTopologyNode(new TopologyNode("remote").setType("project"));
				return graph;
			}
		});
		controller.setTopoGraphFormatConfigManager(new TopoGraphFormatConfigManager() {
			@Override
			public String buildFormatJson() {
				return "{}";
			}
		});
		return controller;
	}

	private DependencyReport report(String domain, long date) {
		DependencyReport report = new DependencyReport(domain);
		Segment segment = new Segment(0);

		segment.addIndex(new Index("Service").setTotalCount(10).setErrorCount(1).setAvg(12.3));
		segment.addDependency(new Dependency("PigeonCall:remote").setType("PigeonCall").setTarget("remote")
				.setTotalCount(8).setErrorCount(0).setAvg(5.5));
		report.addSegment(segment);
		report.setStartTime(new Date(date));
		report.setEndTime(new Date(date + 60 * 60 * 1000 - 1));
		return report;
	}

	private HttpServletRequest request(String action) throws Exception {
		Map<String, String> parameters = new java.util.HashMap<String, String>();

		parameters.put("op", action);
		parameters.put("domain", "cat");
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
							return "SpringMvcDependencyControllerTestRequest";
						}
						return null;
					}
				});
	}
}
