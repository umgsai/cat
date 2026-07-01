package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.mybatis.AlterationRepository;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcStorageControllerTest {
	@Test
	public void shouldBuildHourlyStorageModel() throws Exception {
		SpringMvcStorageController controller = controller();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");

		controller.setStorageModelService(new ModelService<StorageReport>() {
			@Override
			public String getName() {
				return "storage";
			}

			@Override
			public ModelResponse<StorageReport> invoke(ModelRequest request) {
				Assert.assertEquals("cat-SQL", request.getDomain());
				Assert.assertEquals("All", request.getProperty("ip"));
				Assert.assertEquals("2026062720", format.format(new Date(request.getStartTime())));

				ModelResponse<StorageReport> response = new ModelResponse<StorageReport>();
				response.setModel(report("cat-SQL", request.getStartTime(), request.getStartTime() + 3599999));
				return response;
			}

			@Override
			public boolean isEligible(ModelRequest request) {
				return true;
			}
		});

		Map<String, Object> model = controller.storageModel(request("view"));

		Assert.assertEquals("view", model.get("action"));
		Assert.assertEquals("cat", model.get("id"));
		Assert.assertEquals("SQL", model.get("type"));
		Assert.assertEquals("2026062720", model.get("date"));
		Assert.assertTrue(model.get("report") instanceof StorageReport);
	}

	@Test
	public void shouldBuildHistoryStorageModel() throws Exception {
		SpringMvcStorageController controller = controller();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

		controller.setStorageReportService(new StorageReportService() {
			@Override
			public StorageReport queryReport(String domain, Date start, Date end) {
				Assert.assertEquals("cat-SQL", domain);
				Assert.assertEquals("20260627", format.format(start));
				Assert.assertEquals("20260628", format.format(end));
				return report(domain, start.getTime(), end.getTime() - 1);
			}
		});

		Map<String, Object> model = controller.storageModel(request("history"));

		Assert.assertEquals("history", model.get("action"));
		Assert.assertEquals(true, model.get("historyMode"));
		Assert.assertEquals("20260627", model.get("date"));
		Assert.assertEquals("&startDate=20260627&endDate=20260628", model.get("customDate"));
		Assert.assertTrue(model.get("report") instanceof StorageReport);
	}

	@Test
	public void shouldBuildStorageModelWhenReportIsEmpty() {
		SpringMvcStorageController controller = controller();

		controller.setStorageModelService(new ModelService<StorageReport>() {
			@Override
			public String getName() {
				return "storage";
			}

			@Override
			public ModelResponse<StorageReport> invoke(ModelRequest request) {
				return new ModelResponse<StorageReport>();
			}

			@Override
			public boolean isEligible(ModelRequest request) {
				return true;
			}
		});

		Map<String, Object> model = controller.storageModel(request("view"));

		Assert.assertTrue(model.get("report") instanceof StorageReport);
		Assert.assertEquals(java.util.Arrays.asList("select", "update"), model.get("operations"));
		Assert.assertEquals(Integer.valueOf(10), model.get("operationColumnCount"));
	}

	@Test
	public void shouldBuildDashboardStorageModel() throws Exception {
		SpringMvcStorageController controller = controller();

		controller.setAlertService(new AlertService() {
			@Override
			public java.util.List<com.dianping.cat.alarm.Alert> query(Date start, Date end, String type) {
				Assert.assertEquals("SQL", type);
				return Collections.emptyList();
			}
		});
		controller.setStorageAlertInfoBuilder(new StorageAlertInfoBuilder() {
			@Override
			public Map<String, StorageAlertInfo> buildStorageAlertInfos(Date start, Date end, int minuteCounts,
					String type, java.util.List<com.dianping.cat.alarm.Alert> alerts) {
				return Collections.singletonMap("20:00", makeAlertInfo(type, start));
			}
		});

		Map<String, Object> model = controller.storageModel(request("dashboard"));

		Assert.assertEquals("dashboard", model.get("action"));
		Assert.assertEquals("数据库", model.get("storageName"));
		Assert.assertTrue(model.get("alertInfos") instanceof Map);
	}

	private SpringMvcStorageController controller() {
		SpringMvcStorageController controller = new SpringMvcStorageController();

		controller.setAlterationRepository(new AlterationRepository() {
			@Override
			public java.util.List<com.dianping.cat.mybatis.data.AlterationDO> findByTypeDruation(Date startTime,
					Date endTime, String type) {
				return Collections.emptyList();
			}
		});
		controller.setJsonBuilder(new com.dianping.cat.helper.JsonBuilder());
		controller.setStorageBuilderManager(new com.dianping.cat.consumer.storage.builder.StorageBuilderManager() {
			@Override
			public java.util.List<String> getDefaultMethods(String type) {
				return java.util.Arrays.asList("select", "update");
			}
		});
		controller.setStorageGroupConfigManager(new StorageGroupConfigManager() {
			@Override
			public Map<String, Department> queryStorageDepartments(java.util.List<String> ids, String type) {
				return Collections.emptyMap();
			}

			@Override
			public String queryLinkFormat(String type) {
				return null;
			}
		});
		controller.setStorageMergeHelper(new StorageMergeHelper());
		return controller;
	}

	private StorageReport report(String id, long start, long end) {
		StorageReport report = new StorageReport(id);
		Machine machine = report.findOrCreateMachine("All");

		report.setName("cat");
		report.setType("SQL");
		report.setStartTime(new Date(start));
		report.setEndTime(new Date(end));
		report.getIps().add("All");
		report.getIds().add("cat");
		report.getOps().add("select");
		report.getOps().add("update");
		machine.findOrCreateDomain("All").findOrCreateOperation("select").setCount(1).setAvg(1.0);
		machine.findOrCreateDomain("All").findOrCreateOperation("update").setCount(1).setAvg(1.0);
		return report;
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
							return "SpringMvcStorageControllerTestRequest";
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
		if ("id".equals(name)) {
			return "cat";
		}
		if ("ip".equals(name)) {
			return "All";
		}
		if ("type".equals(name)) {
			return "SQL";
		}
		if ("date".equals(name)) {
			return "history".equals(action) ? "2026062700" : "2026062720";
		}
		if ("reportType".equals(name)) {
			return "day";
		}
		if ("minute".equals(name)) {
			return "0";
		}
		return null;
	}
}
