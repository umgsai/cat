package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.service.ProjectService;

public class SpringMvcProjectControllerTest {
	@Test
	public void shouldDefaultToDomainsAction() {
		SpringMvcProjectController controller = new SpringMvcProjectController();

		Assert.assertEquals("domains", controller.action(request(null)));
		Assert.assertEquals("projectUpdate", controller.action(request("projectUpdate")));
	}

	@Test
	public void shouldBuildDomainsJsonLikeLegacyProjectPage() {
		SpringMvcProjectController controller = new SpringMvcProjectController();
		ProjectService service = new StubProjectService("cat", "mobile-api");

		controller.setProjectService(service);

		String json = controller.domainsJson();

		Assert.assertTrue(json.startsWith("{\"domains\":"));
		Assert.assertTrue(json.contains("\"cat\""));
		Assert.assertTrue(json.contains("\"mobile-api\""));
	}

	private HttpServletRequest request(String action) {
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getParameter".equals(method.getName()) && "op".equals(args[0])) {
							return action;
						}
						if ("toString".equals(method.getName())) {
							return "SpringMvcProjectControllerTestRequest";
						}
						return null;
					}
				});
	}

	private static class StubProjectService extends ProjectService {
		private final ConcurrentHashMap<String, String> m_domains = new ConcurrentHashMap<String, String>();

		StubProjectService(String... domains) {
			for (String domain : Arrays.asList(domains)) {
				m_domains.put(domain, domain);
			}
		}

		@Override
		public java.util.Set<String> findAllDomains() {
			return m_domains.keySet();
		}
	}
}
