package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

public class SpringMvcPluginControllerTest {
	@Test
	public void shouldBuildPluginModelWithLegacyDownloadLinks() {
		SpringMvcPluginController controller = new SpringMvcPluginController();
		Map<String, Object> model = controller.pluginModel(request("/cat"));

		Assert.assertEquals("spring-mvc-migration", model.get("runtime"));
		Assert.assertEquals("/cat/s/plugin", model.get("legacyPluginUrl"));
		Assert.assertEquals("/cat/mvc/s/plugin/chrome", model.get("chromeExtensionUrl"));
		Assert.assertEquals("/cat/mvc/s/plugin/chrome?source=true", model.get("chromeSourceUrl"));
		Assert.assertEquals("/cat/mvc/s/plugin/chrome?mapping=true", model.get("chromeMappingUrl"));
		Assert.assertEquals("/cat/s/plugin/chrome", model.get("legacyChromeExtensionUrl"));
		Assert.assertEquals("/cat/mvc/r/home", model.get("homeUrl"));
	}

	@Test
	public void shouldValidateDocFileName() {
		SpringMvcPluginController controller = new SpringMvcPluginController();

		Assert.assertTrue(controller.isValidDocFile("basemonitor-android.pdf"));
		Assert.assertFalse(controller.isValidDocFile("../secret.pdf"));
		Assert.assertFalse(controller.isValidDocFile("dir/secret.pdf"));
		Assert.assertFalse(controller.isValidDocFile("secret.txt"));
		Assert.assertFalse(controller.isValidDocFile(null));
	}

	@Test
	public void shouldExposeServerMappingJson() {
		SpringMvcPluginController controller = new SpringMvcPluginController();
		String json = controller.serverMappingJson();

		Assert.assertTrue(json.contains("10.1.6.37:8080"));
		Assert.assertTrue(json.contains("cat.dianpingoa.com"));
		Assert.assertTrue(json.contains("192.168.7.70:8080"));
	}

	private HttpServletRequest request(String contextPath) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getContextPath".equals(method.getName())) {
							return contextPath;
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcPluginControllerTestRequest";
						}
						return null;
					}
				});
	}
}
