package com.dianping.cat.home.spring.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.mybatis.data.AlertDO;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.mybatis.AlertRepository;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcAlertControllerTest {
	@Test
	public void shouldBuildAlertViewModel() throws Exception {
		SpringMvcAlertController controller = controller();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		controller.setAlertRepository(new AlertRepository() {
			@Override
			public java.util.List<AlertDO> queryAlertsByTimeDomainCategories(Date startTime, Date endTime, String domain,
					String[] categories) {
				Assert.assertEquals("2026-06-27 20:00", format.format(startTime));
				Assert.assertEquals("2026-06-27 21:00", format.format(endTime));
				Assert.assertEquals("cat", domain);
				Assert.assertArrayEquals(new String[] { "business", "system" }, categories);
				return Collections.singletonList(alert("cat", "business", "warning", startTime));
			}
		});

		Map<String, Object> model = controller.alertModel(viewRequest());

		Assert.assertEquals("view", model.get("action"));
		Assert.assertEquals("cat", model.get("domain"));
		Assert.assertTrue(model.get("alertMinutes") instanceof Map);
		Assert.assertEquals(1, ((Map<?, ?>) model.get("alertMinutes")).size());
	}

	@Test
	public void shouldInsertAlert() throws Exception {
		SpringMvcAlertController controller = controller();
		ResponseRecorder response = new ResponseRecorder();

		controller.setAlertRepository(new AlertRepository() {
			@Override
			public int insert(AlertDO proto) {
				Assert.assertEquals("cat", proto.getDomain());
				Assert.assertEquals("zabbix", proto.getCategory());
				Assert.assertEquals("warning", proto.getType());
				Assert.assertEquals("cpu", proto.getMetric());
				return 1;
			}
		});

		controller.alert(insertRequest("cat"), response(response));

		Assert.assertEquals("text/html;charset=utf-8", response.contentType);
		Assert.assertEquals("{\"status\":200}", response.body.toString());
	}

	@Test
	public void shouldRejectInsertWithoutDomain() throws Exception {
		SpringMvcAlertController controller = controller();
		ResponseRecorder response = new ResponseRecorder();

		controller.alert(insertRequest(""), response(response));

		Assert.assertEquals("{\"status\":500, \"errorMessage\":\"lack domain\"}", response.body.toString());
	}

	@Test
	public void shouldSendManualAlert() throws Exception {
		SpringMvcAlertController controller = controller();
		ResponseRecorder response = new ResponseRecorder();

		controller.setSenderManager(new SenderManager() {
			@Override
			public boolean sendAlert(AlertChannel channel, SendMessageEntity message) {
				Assert.assertEquals("mail", channel.getName());
				Assert.assertEquals("default", message.getGroup());
				Assert.assertEquals("call", message.getType());
				Assert.assertEquals("u1,u2", message.getReceiverString());
				return true;
			}
		});

		controller.alert(sendRequest("u1,u2", "mail"), response(response));

		Assert.assertEquals("{\"status\":200}", response.body.toString());
	}

	@Test
	public void shouldRejectManualAlertWithoutReceivers() throws Exception {
		SpringMvcAlertController controller = controller();
		ResponseRecorder response = new ResponseRecorder();

		controller.alert(sendRequest("", "mail"), response(response));

		Assert.assertEquals("{\"status\":500, \"errorMessage\":\"lack receivers\"}", response.body.toString());
	}

	@Test
	public void shouldReportInvalidAlertChannel() throws Exception {
		SpringMvcAlertController controller = controller();
		ResponseRecorder response = new ResponseRecorder();

		controller.alert(sendRequest("u1", ""), response(response));

		Assert.assertEquals("{\"status\":500, \"errorMessage\":\"send failed, please check your channel argument\"}",
				response.body.toString());
	}

	private AlertDO alert(String domain, String category, String type, Date alertTime) {
		AlertDO alert = new AlertDO();

		alert.setDomain(domain);
		alert.setCategory(category);
		alert.setType(type);
		alert.setMetric("cpu");
		alert.setContent("high");
		alert.setAlertTime(alertTime);
		return alert;
	}

	private SpringMvcAlertController controller() {
		SpringMvcAlertController controller = new SpringMvcAlertController();

		controller.setAlertRepository(new AlertRepository() {
			@Override
			public java.util.List<AlertDO> queryAlertsByTimeDomain(Date startTime, Date endTime, String domain) {
				return Collections.emptyList();
			}
		});
		controller.setSenderManager(new SenderManager() {
			@Override
			public boolean sendAlert(AlertChannel channel, SendMessageEntity message) {
				return false;
			}
		});
		return controller;
	}

	private HttpServletRequest insertRequest(String domain) {
		return request(new java.util.HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("op", "insert");
				put("domain", domain);
				put("alertTime", "2026-06-27 20:00");
				put("metric", "cpu");
				put("content", "high");
			}
		});
	}

	private HttpServletRequest request(Map<String, String> parameters) {
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
							return "SpringMvcAlertControllerTestRequest";
						}
						return null;
					}
				});
	}

	private HttpServletResponse response(ResponseRecorder recorder) {
		return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("setContentType".equals(method.getName())) {
							recorder.contentType = (String) args[0];
							return null;
						}
						if ("getWriter".equals(method.getName())) {
							return new PrintWriter(recorder.body);
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcAlertControllerTestResponse";
						}
						return null;
					}
				});
	}

	private HttpServletRequest sendRequest(String receivers, String channel) {
		return request(new java.util.HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("op", "alert");
				put("channel", channel);
				put("title", "title");
				put("content", "content");
				put("receivers", receivers);
			}
		});
	}

	private HttpServletRequest viewRequest() {
		return request(new java.util.HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("op", "view");
				put("startTime", "2026-06-27 20:00");
				put("endTime", "2026-06-27 21:00");
				put("domain", "cat");
				put("count", "8");
				put("alertType", "business,system,");
			}
		});
	}

	private static class ResponseRecorder {
		private final StringWriter body = new StringWriter();

		private String contentType;
	}
}
