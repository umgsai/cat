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

import com.dianping.cat.mybatis.AlterationRepository;
import com.dianping.cat.mybatis.data.AlterationDO;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcAlterationControllerTest {
	@Test
	public void shouldBuildAlterationViewModel() throws Exception {
		SpringMvcAlterationController controller = controller();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		controller.setAlterationRepository(new AlterationRepository() {
			@Override
			public java.util.List<AlterationDO> findByDtdhTypes(Date startTime, Date endTime, String type, String domain,
					String hostname, String[] types) {
				Assert.assertEquals("2026-06-27 20:00", format.format(startTime));
				Assert.assertEquals("2026-06-27 21:00", format.format(endTime));
				Assert.assertNull(type);
				Assert.assertEquals("cat", domain);
				Assert.assertEquals("host-a", hostname);
				Assert.assertArrayEquals(new String[] { "workflow" }, types);
				return Collections.singletonList(alteration("workflow", "cat", "host-a", startTime));
			}
		});

		Map<String, Object> model = controller.alterationModel(request("view"));

		Assert.assertEquals("view", model.get("action"));
		Assert.assertEquals("cat", model.get("domain"));
		Assert.assertTrue(model.get("alterationMinutes") instanceof Map);
		Assert.assertEquals(1, ((Map<?, ?>) model.get("alterationMinutes")).size());
	}

	@Test
	public void shouldInsertAlteration() throws Exception {
		SpringMvcAlterationController controller = controller();
		ResponseRecorder response = new ResponseRecorder();

		controller.setAlterationRepository(new AlterationRepository() {
			@Override
			public int insert(AlterationDO proto) {
				Assert.assertEquals("workflow", proto.getType());
				Assert.assertEquals("cat", proto.getDomain());
				Assert.assertEquals("host-a", proto.getHostname());
				Assert.assertEquals("http://example.com/a b", proto.getUrl());
				return 1;
			}
		});

		controller.alteration(insertRequest("workflow"), response(response));

		Assert.assertEquals("text/html;charset=utf-8", response.contentType);
		Assert.assertEquals("{\"status\":200}", response.body.toString());
	}

	@Test
	public void shouldRejectIllegalInsert() throws Exception {
		SpringMvcAlterationController controller = controller();
		ResponseRecorder response = new ResponseRecorder();

		controller.alteration(insertRequest(null), response(response));

		Assert.assertEquals("{\"status\":500, \"errorMessage\":\"lack args\"}", response.body.toString());
	}

	@Test
	public void shouldNormalizeSqlInsertArgs() throws Exception {
		SpringMvcAlterationController controller = controller();
		ResponseRecorder response = new ResponseRecorder();

		controller.setAlterationRepository(new AlterationRepository() {
			@Override
			public int insert(AlterationDO proto) {
				Assert.assertEquals("SQL", proto.getType());
				Assert.assertEquals("N/A", proto.getDomain());
				Assert.assertEquals("host-a", proto.getHostname());
				Assert.assertEquals("N/A", proto.getIp());
				Assert.assertNull(proto.getUser());
				Assert.assertEquals("N/A", proto.getUrl());
				return 1;
			}
		});

		controller.alteration(sqlInsertRequest(), response(response));

		Assert.assertEquals("{\"status\":200}", response.body.toString());
	}

	private AlterationDO alteration(String type, String domain, String hostname, Date date) {
		AlterationDO alteration = new AlterationDO();

		alteration.setType(type);
		alteration.setDomain(domain);
		alteration.setHostname(hostname);
		alteration.setTitle("deploy");
		alteration.setContent("done");
		alteration.setChangeTime(date);
		return alteration;
	}

	private SpringMvcAlterationController controller() {
		SpringMvcAlterationController controller = new SpringMvcAlterationController();

		controller.setAlterationRepository(new AlterationRepository() {
			@Override
			public java.util.List<AlterationDO> findByDtdh(Date startTime, Date endTime, String type, String domain,
					String hostname) {
				return Collections.emptyList();
			}
		});
		return controller;
	}

	private HttpServletRequest insertRequest(String type) {
		return request(new java.util.HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("op", "insert");
				if (type != null) {
					put("type", type);
				}
				put("title", "deploy");
				put("domain", "cat");
				put("hostname", "host-a");
				put("alterationDate", "2026-06-27 20:00:01");
				put("user", "codex");
				put("content", "done");
				put("url", "http%3A%2F%2Fexample.com%2Fa+b");
			}
		});
	}

	private HttpServletRequest request(String action) {
		return request(new java.util.HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("op", action);
				put("startTime", "2026-06-27 20:00");
				put("endTime", "2026-06-27 21:00");
				put("domain", "cat");
				put("hostname", "host-a");
				put("count", "8");
				put("altType", "workflow");
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
							return "SpringMvcAlterationControllerTestRequest";
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
							return "SpringMvcAlterationControllerTestResponse";
						}
						return null;
					}
				});
	}

	private HttpServletRequest sqlInsertRequest() {
		return request(new java.util.HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("op", "insert");
				put("type", "SQL");
				put("title", "schema change");
				put("hostname", "host-a");
				put("content", "done");
			}
		});
	}

	private static class ResponseRecorder {
		private final StringWriter body = new StringWriter();

		private String contentType;
	}
}
