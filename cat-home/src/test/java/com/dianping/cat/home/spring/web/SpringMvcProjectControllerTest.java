package com.dianping.cat.home.spring.web;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.mybatis.data.ProjectDO;
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

	@Test
	public void shouldInsertProjectWhenDomainIsMissing() {
		SpringMvcProjectController controller = new SpringMvcProjectController();
		StubProjectService service = new StubProjectService();

		controller.setProjectService(service);

		String result = controller.updateProject(request("projectUpdate", "project.cmdbDomain", "cmdb-cat", "project.level", "1",
				"project.bu", "platform", "project.cmdbProductline", "arch", "project.owner", "owner", "project.email",
				"owner@example.com", "project.phone", "123"));

		Assert.assertEquals("{\"status\":200, \"info\":\"success\"}", result);
		Assert.assertEquals("cat", service.getInserted().getDomain());
		Assert.assertEquals("cmdb-cat", service.getInserted().getCmdbDomain());
		Assert.assertEquals(Integer.valueOf(1), service.getInserted().getLevel());
		Assert.assertEquals("platform", service.getInserted().getBu());
		Assert.assertEquals("arch", service.getInserted().getCmdbProductline());
		Assert.assertEquals("owner", service.getInserted().getOwner());
		Assert.assertEquals("owner@example.com", service.getInserted().getEmail());
		Assert.assertEquals("123", service.getInserted().getPhone());
	}

	@Test
	public void shouldUpdateProjectWhenDomainExists() {
		SpringMvcProjectController controller = new SpringMvcProjectController();
		StubProjectService service = new StubProjectService("cat");

		controller.setProjectService(service);

		String result = controller.updateProject(request("projectUpdate", "project.domain", "cat", "project.cmdbDomain",
				"cmdb-cat", "project.level", "2"));

		Assert.assertEquals("{\"status\":200, \"info\":\"success\"}", result);
		Assert.assertNull(service.getInserted());
		Assert.assertEquals("cat", service.getUpdated().getDomain());
		Assert.assertEquals("cmdb-cat", service.getUpdated().getCmdbDomain());
		Assert.assertEquals(Integer.valueOf(2), service.getUpdated().getLevel());
	}

	@Test
	public void shouldReturnInternalErrorWhenProjectUpdateFails() {
		SpringMvcProjectController controller = new SpringMvcProjectController();
		StubProjectService service = new StubProjectService();

		service.setFailInsert(true);
		controller.setProjectService(service);

		String result = controller.updateProject(request("projectUpdate", "project.domain", "cat"));

		Assert.assertEquals("{\"status\":500, \"info\":\"internal error\"}", result);
	}

	private HttpServletRequest request(String action, String... parameters) {
		ConcurrentHashMap<String, String> values = new ConcurrentHashMap<String, String>();

		for (int i = 0; i < parameters.length; i += 2) {
			values.put(parameters[i], parameters[i + 1]);
		}
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getParameter".equals(method.getName())) {
							if ("op".equals(args[0])) {
								return action;
							}
							return values.get(args[0]);
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

		private boolean m_failInsert;

		private ProjectDO m_inserted;

		private ProjectDO m_updated;

		StubProjectService(String... domains) {
			for (String domain : Arrays.asList(domains)) {
				m_domains.put(domain, domain);
			}
		}

		ProjectDO getInserted() {
			return m_inserted;
		}

		ProjectDO getUpdated() {
			return m_updated;
		}

		@Override
		public java.util.Set<String> findAllDomains() {
			return m_domains.keySet();
		}

		@Override
		public ProjectDO findByDomain(String domainName) {
			if (!m_domains.containsKey(domainName)) {
				return null;
			}

			ProjectDO project = new ProjectDO();

			project.setDomain(domainName);
			return project;
		}

		@Override
		public boolean insert(ProjectDO project) {
			if (m_failInsert) {
				throw new RuntimeException("insert failed");
			}
			m_inserted = project;
			m_domains.put(project.getDomain(), project.getDomain());
			return true;
		}

		void setFailInsert(boolean failInsert) {
			m_failInsert = failInsert;
		}

		@Override
		public boolean update(ProjectDO project) {
			m_updated = project;
			return true;
		}
	}
}
