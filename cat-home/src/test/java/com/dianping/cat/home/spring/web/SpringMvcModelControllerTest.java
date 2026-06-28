package com.dianping.cat.home.spring.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import org.junit.Assert;
import org.junit.Test;

public class SpringMvcModelControllerTest {
	@Test
	public void shouldRenderXmlFromLocalModelService() {
		SpringMvcModelController controller = new SpringMvcModelController();
		StubLocalModelService service = new StubLocalModelService("transaction");

		controller.setLocalServices(Collections.<String, LocalModelService>singletonMap("transaction", service));

		String xml = controller.render(new SpringMvcModelController.PathParts("transaction", "cat", "CURRENT"),
				new SpringMvcModelController.ModelApiPayload());

		Assert.assertEquals("<report domain=\"cat\" period=\"CURRENT\"/>", xml);
		Assert.assertEquals("cat", service.domain);
		Assert.assertEquals(ModelPeriod.CURRENT, service.period);
		Assert.assertEquals("cat", service.request.getDomain());
	}

	@Test
	public void shouldWriteGzipXmlResponse() throws Exception {
		SpringMvcModelController controller = new SpringMvcModelController();
		StubLocalModelService service = new StubLocalModelService("transaction");
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		HeaderRecorder headers = new HeaderRecorder();

		controller.setLocalServices(Collections.<String, LocalModelService>singletonMap("transaction", service));
		controller.model(request("/r/model/transaction/cat/CURRENT"), response(body, headers));

		Assert.assertEquals("application/xml;charset=utf-8", headers.contentType);
		Assert.assertEquals("gzip", headers.contentEncoding);
		Assert.assertEquals("<report domain=\"cat\" period=\"CURRENT\"/>", unzip(body.toByteArray()));
	}

	private HttpServletRequest request(String pathInfo) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getPathInfo".equals(method.getName())) {
							return pathInfo;
						}
						if ("getParameter".equals(method.getName())) {
							if ("cdn".equals(args[0])) {
								return "ALL";
							}
							return null;
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcModelControllerTestRequest";
						}
						return null;
					}
				});
	}

	private HttpServletResponse response(ByteArrayOutputStream body, HeaderRecorder headers) {
		return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getOutputStream".equals(method.getName())) {
							return output(body);
						}
						if ("setContentType".equals(method.getName())) {
							headers.contentType = (String) args[0];
							return null;
						}
						if ("addHeader".equals(method.getName())) {
							if ("Content-Encoding".equals(args[0])) {
								headers.contentEncoding = (String) args[1];
							}
							return null;
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcModelControllerTestResponse";
						}
						return null;
					}
				});
	}

	private ServletOutputStream output(ByteArrayOutputStream body) {
		return new ServletOutputStream() {
			@Override
			public void write(int b) {
				body.write(b);
			}
		};
	}

	private String unzip(byte[] bytes) throws Exception {
		GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(bytes));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int count;

		while ((count = input.read(buffer)) >= 0) {
			output.write(buffer, 0, count);
		}
		return new String(output.toByteArray(), "UTF-8");
	}

	private static class HeaderRecorder {
		private String contentEncoding;

		private String contentType;
	}

	private static class StubLocalModelService extends LocalModelService<Object> {
		private String domain;

		private ModelPeriod period;

		private ModelRequest request;

		StubLocalModelService(String name) {
			super(name);
		}

		@Override
		public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload) {
			return null;
		}

		@Override
		public String getReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload) {
			this.request = request;
			this.period = period;
			this.domain = domain;
			return "<report domain=\"" + domain + "\" period=\"" + period + "\"/>";
		}
	}
}
