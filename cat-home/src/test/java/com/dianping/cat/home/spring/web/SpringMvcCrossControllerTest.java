package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.cross.display.MethodQueryInfo;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.SampleConfig;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcCrossControllerTest {
	@Test
	public void shouldBuildMethodQueryInfoFromHourlyReport() {
		SpringMvcCrossController controller = controller();

		Map<String, Object> model = controller.crossModel(request("query", "2026062720"));
		MethodQueryInfo info = (MethodQueryInfo) model.get("queryInfo");

		Assert.assertEquals("query", model.get("action"));
		Assert.assertEquals(false, model.get("historyMode"));
		Assert.assertNotNull(info);
		Assert.assertEquals(1, info.getItems().size());
		Assert.assertEquals("orderService", info.getItems().get(0).getMethod());
		Assert.assertEquals("remote-app", info.getItems().get(0).getDomain());
		Assert.assertEquals("10.0.0.2", info.getItems().get(0).getIp());
		Assert.assertEquals(12L, info.getItems().get(0).getTotalCount());
	}

	@Test
	public void shouldBuildMethodQueryInfoFromHistoryReportWhenDateIsDay() {
		SpringMvcCrossController controller = controller();

		Map<String, Object> model = controller.crossModel(request("query", "20260627"));
		MethodQueryInfo info = (MethodQueryInfo) model.get("queryInfo");

		Assert.assertEquals("query", model.get("action"));
		Assert.assertEquals(true, model.get("historyMode"));
		Assert.assertEquals("20260627", model.get("date"));
		Assert.assertNotNull(info);
		Assert.assertEquals(1, info.getItems().size());
	}

	private SpringMvcCrossController controller() {
		SpringMvcCrossController controller = new SpringMvcCrossController();

		controller.setCrossModelService(new ModelService<CrossReport>() {
			@Override
			public String getName() {
				return "cross";
			}

			@Override
			public ModelResponse<CrossReport> invoke(ModelRequest request) {
				ModelResponse<CrossReport> response = new ModelResponse<CrossReport>();

				response.setModel(report(request.getDomain(), request.getStartTime()));
				return response;
			}

			@Override
			public boolean isEligible(ModelRequest request) {
				return true;
			}
		});
		controller.setCrossReportService(new CrossReportService() {
			@Override
			public CrossReport queryReport(String domain, Date start, Date end) {
				try {
					Assert.assertEquals("cat", domain);
					Assert.assertEquals("20260627", new SimpleDateFormat("yyyyMMdd").format(start));
					Assert.assertEquals("20260628", new SimpleDateFormat("yyyyMMdd").format(end));
				} catch (RuntimeException e) {
					throw e;
				}
				return report(domain, start.getTime());
			}
		});
		controller.setDomainGroupConfigManager(new DomainGroupConfigManager() {
			@Override
			public List<String> queryDomainGroup(String domain) {
				return Collections.emptyList();
			}
		});
		controller.setHostinfoService(new HostinfoService() {
			@Override
			public String queryHostnameByIp(String ip) {
				return null;
			}
		});
		controller.setProjectService(new ProjectService() {
			@Override
			public java.util.Set<String> findAllDomains() {
				return Collections.singleton("cat");
			}

			@Override
			public Map<String, Department> findDepartments(Collection<String> domains) {
				return Collections.emptyMap();
			}
		});
		controller.setSampleConfigManager(new SampleConfigManager() {
			@Override
			public SampleConfig getConfig() {
				return new SampleConfig();
			}
		});
		return controller;
	}

	private CrossReport report(String domain, long date) {
		CrossReport report = new CrossReport(domain);
		Remote remote = new Remote("10.0.0.2:2080").setRole("PigeonCall").setApp("remote-app")
				.setType(new Type().addName(new Name("orderService").setTotalCount(12).setFailCount(2).setSum(36)));

		report.setStartTime(new Date(date));
		report.setEndTime(new Date(date + 60 * 60 * 1000 - 1));
		report.addIp("10.0.0.1");
		report.findOrCreateLocal("10.0.0.1").addRemote(remote);
		return report;
	}

	private HttpServletRequest request(String action, String date) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getContextPath".equals(method.getName())) {
							return "/cat";
						}
						if ("getParameter".equals(method.getName())) {
							return parameter(action, date, (String) args[0]);
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcCrossControllerTestRequest";
						}
						return null;
					}
				});
	}

	private String parameter(String action, String date, String name) {
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
			return date;
		}
		if ("method".equals(name)) {
			return "order";
		}
		if ("reportType".equals(name)) {
			return "day";
		}
		return null;
	}
}
