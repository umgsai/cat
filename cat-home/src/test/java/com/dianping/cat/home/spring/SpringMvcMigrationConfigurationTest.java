package com.dianping.cat.home.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.dianping.cat.home.spring.web.SpringMvcHealthController;
import com.dianping.cat.home.spring.web.SpringMvcHomeController;
import com.dianping.cat.home.spring.web.SpringMvcLoginController;
import com.dianping.cat.system.page.login.service.SigninService;

public class SpringMvcMigrationConfigurationTest {
	@Test
	public void shouldRegisterSpringMvcMigrationController() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				TestConfiguration.class, SpringMvcMigrationConfiguration.class)) {
			Assert.assertNotNull(context.getBean(SpringMvcHealthController.class));
			Assert.assertNotNull(context.getBean(SpringMvcHomeController.class));
			Assert.assertNotNull(context.getBean(SpringMvcLoginController.class));
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
	}
}
