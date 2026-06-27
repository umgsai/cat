package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.report.page.overload.task.TableCapacityService;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcOverloadControllerTest {
	@Test
	public void shouldBuildOverloadModelFromRequest() throws Exception {
		SpringMvcOverloadController controller = new SpringMvcOverloadController();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		controller.setTableCapacityService(new TableCapacityService() {
			@Override
			public java.util.List<com.dianping.cat.report.page.overload.task.OverloadReport> queryOverloadReports(
					java.util.Date startTime, java.util.Date endTime) {
				Assert.assertEquals("2026-06-26 00:00", format.format(startTime));
				Assert.assertEquals("2026-06-27 00:00", format.format(endTime));
				return Collections.emptyList();
			}
		});

		Map<String, Object> model = controller.overloadModel(request());

		Assert.assertEquals("/cat", model.get("contextPath"));
		Assert.assertEquals("view", model.get("actionName"));
		Assert.assertEquals("Overload", model.get("activeReport"));
		Assert.assertEquals(false, model.get("showHourly"));
		Assert.assertEquals(true, model.get("showDaily"));
		Assert.assertEquals(false, model.get("fullScreen"));
		Assert.assertEquals(Collections.emptyList(), model.get("reports"));
	}

	private HttpServletRequest request() {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getContextPath".equals(method.getName())) {
							return "/cat";
						}
						if ("getParameter".equals(method.getName())) {
							return parameter((String) args[0]);
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcOverloadControllerTestRequest";
						}
						return null;
					}
				});
	}

	private String parameter(String name) {
		if ("startTime".equals(name)) {
			return "2026-06-26 00:00";
		}
		if ("endTime".equals(name)) {
			return "2026-06-27 00:00";
		}
		if ("showHourly".equals(name)) {
			return "false";
		}
		return null;
	}
}
