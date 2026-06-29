package com.dianping.cat.home.spring.web;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.home.spring.view.problem.GroupLevelInfo;
import com.dianping.cat.home.spring.view.problem.ThreadLevelInfo;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.problem.transform.DetailStatistics;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.sample.entity.SampleConfig;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcProblemControllerTest {
	@Test
	public void shouldBuildGroupThreadAndDetailModels() {
		RecordingProblemModelService modelService = new RecordingProblemModelService();
		SpringMvcProblemController controller = controller(modelService);

		Map<String, Object> groupModel = controller.problemModel(request("group"));
		Map<String, Object> threadModel = controller.problemModel(request("thread"));
		Map<String, Object> detailModel = controller.problemModel(request("detail"));

		Assert.assertTrue(groupModel.get("groupLevelInfo") instanceof GroupLevelInfo);
		Assert.assertTrue(threadModel.get("threadLevelInfo") instanceof ThreadLevelInfo);
		Assert.assertEquals("main", threadModel.get("groupName"));
		Assert.assertTrue(detailModel.get("detailStatistics") instanceof DetailStatistics);
		Assert.assertEquals(1, ((DetailStatistics) detailModel.get("detailStatistics")).getStatus().size());
		Assert.assertEquals("detail", modelService.getLastQueryType());
	}

	@Test
	public void shouldUseReadableHistoryGraphTitle() {
		SpringMvcProblemController controller = controller(new RecordingProblemModelService());
		Map<String, Object> model = controller.problemModel(request("historyGraph"));
		String errorsTrend = (String) model.get("errorsTrend");

		Assert.assertTrue(errorsTrend.contains("错误量 (count/min)"));
		Assert.assertFalse(errorsTrend.contains("閿"));
	}

	private SpringMvcProblemController controller(RecordingProblemModelService modelService) {
		SpringMvcProblemController controller = new SpringMvcProblemController();
		ProjectService projectService = new ProjectService() {
			@Override
			public java.util.Set<String> findAllDomains() {
				return Collections.singleton("cat");
			}

			@Override
			public Map<String, Department> findDepartments(Collection<String> domains) {
				return Collections.emptyMap();
			}
		};
		HostinfoService hostinfoService = new HostinfoService() {
			@Override
			public String queryHostnameByIp(String ip) {
				return null;
			}
		};
		SampleConfigManager sampleConfigManager = new SampleConfigManager() {
			@Override
			public SampleConfig getConfig() {
				return new SampleConfig();
			}
		};
		controller.setProblemModelService(modelService);
		controller.setProblemReportService(new ProblemReportService() {
			@Override
			public ProblemReport queryReport(String domain, Date start, Date end) {
				ProblemReport report = report(domain, start.getTime());

				report.setEndTime(end);
				return report;
			}
		});
		controller.setDomainGroupConfigManager(new DomainGroupConfigManager() {
			@Override
			public String queryDefaultGroup(String domain) {
				return "main";
			}

			@Override
			public List<String> queryDomainGroup(String domain) {
				return Collections.singletonList("main");
			}

			@Override
			public List<String> queryIpByDomainAndGroup(String domain, String group) {
				return Collections.singletonList("10.0.0.1");
			}
		});
		controller.setHostinfoService(hostinfoService);
		controller.setProjectService(projectService);
		controller.setSampleConfigManager(sampleConfigManager);
		controller.setServerConfigManager(new ServerConfigManager());
		return controller;
	}

	private HttpServletRequest request(String action) {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("op", action);
		parameters.put("domain", "cat");
		parameters.put("ip", "10.0.0.1");
		parameters.put("date", "2026062720");
		parameters.put("minute", "1");
		parameters.put("group", "main");
		parameters.put("thread", "thread-1");

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
							return "SpringMvcProblemControllerTestRequest";
						}
						return null;
					}
				});
	}

	private ProblemReport report(String domain, long date) {
		ProblemReport report = new ProblemReport(domain);
		Entity entity = new Entity("URL;/api/order").setType("URL").setStatus("ERROR");
		JavaThread thread = new JavaThread("thread-1").setGroupName("main").setName("main-thread");

		thread.addSegment(new Segment(1).setCount(3).addMessage("message-id"));
		entity.addThread(thread);
		report.setStartTime(new Date(date));
		report.setEndTime(new Date(date + 60 * 60 * 1000 - 1));
		report.addIp("10.0.0.1");
		report.findOrCreateMachine("10.0.0.1").addEntity(entity);
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

	private class RecordingProblemModelService implements ModelService<ProblemReport> {
		private String m_lastQueryType;

		@Override
		public String getName() {
			return "problem";
		}

		@Override
		public ModelResponse<ProblemReport> invoke(ModelRequest request) {
			ModelResponse<ProblemReport> response = new ModelResponse<ProblemReport>();

			m_lastQueryType = request.getProperty("queryType");
			response.setModel(report(request.getDomain(), request.getStartTime()));
			return response;
		}

		@Override
		public boolean isEligible(ModelRequest request) {
			return true;
		}

		private String getLastQueryType() {
			return m_lastQueryType;
		}
	}
}
