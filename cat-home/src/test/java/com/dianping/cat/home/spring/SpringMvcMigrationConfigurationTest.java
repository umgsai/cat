package com.dianping.cat.home.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.home.spring.web.SpringMvcHealthController;
import com.dianping.cat.home.spring.web.SpringMvcBusinessController;
import com.dianping.cat.home.spring.web.SpringMvcHomeController;
import com.dianping.cat.home.spring.web.SpringMvcLoginController;
import com.dianping.cat.home.spring.web.SpringMvcPluginController;
import com.dianping.cat.home.spring.web.SpringMvcProjectController;
import com.dianping.cat.home.spring.web.SpringMvcRouterController;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.system.page.login.service.SigninService;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.router.service.CachedRouterConfigService;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;

public class SpringMvcMigrationConfigurationTest {
	@Test
	public void shouldRegisterSpringMvcMigrationController() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				TestConfiguration.class, SpringMvcMigrationConfiguration.class)) {
			Assert.assertNotNull(context.getBean(SpringMvcBusinessController.class));
			Assert.assertNotNull(context.getBean(SpringMvcHealthController.class));
			Assert.assertNotNull(context.getBean(SpringMvcHomeController.class));
			Assert.assertNotNull(context.getBean(SpringMvcLoginController.class));
			Assert.assertNotNull(context.getBean(SpringMvcPluginController.class));
			Assert.assertNotNull(context.getBean(SpringMvcProjectController.class));
			Assert.assertNotNull(context.getBean(SpringMvcRouterController.class));
		}
	}

	@Test
	public void shouldExposeHealthPayloadForMigrationPath() {
		SpringMvcHealthController controller = new SpringMvcHealthController();

		Assert.assertEquals("UP", controller.health().get("status"));
		Assert.assertEquals("spring-mvc-migration", controller.health().get("runtime"));
	}

	static class TestConfiguration {
		@org.springframework.context.annotation.Bean
		public SigninService signinService() {
			return new SigninService();
		}

		@org.springframework.context.annotation.Bean
		public CachedRouterConfigService cachedRouterConfigService() {
			return new CachedRouterConfigService();
		}

		@org.springframework.context.annotation.Bean
		public RouterConfigManager routerConfigManager() {
			return new RouterConfigManager();
		}

		@org.springframework.context.annotation.Bean
		public SampleConfigManager sampleConfigManager() {
			return new SampleConfigManager();
		}

		@org.springframework.context.annotation.Bean
		public ServerFilterConfigManager serverFilterConfigManager() {
			return new ServerFilterConfigManager();
		}

		@org.springframework.context.annotation.Bean
		public ProjectService projectService() {
			return new ProjectService();
		}

		@org.springframework.context.annotation.Bean
		public BusinessConfigManager businessConfigManager() {
			return new BusinessConfigManager();
		}

		@org.springframework.context.annotation.Bean
		public BusinessTagConfigManager businessTagConfigManager() {
			return new BusinessTagConfigManager();
		}
	}
}
