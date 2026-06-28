package com.dianping.cat.home.spring;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import com.dianping.cat.home.spring.web.SpringMvcHealthController;
import com.dianping.cat.home.spring.web.SpringMvcBusinessController;
import com.dianping.cat.home.spring.web.SpringMvcCacheController;
import com.dianping.cat.home.spring.web.SpringMvcHomeController;
import com.dianping.cat.home.spring.web.SpringMvcLoginController;
import com.dianping.cat.home.spring.web.SpringMvcMatrixController;
import com.dianping.cat.home.spring.web.SpringMvcModelController;
import com.dianping.cat.home.spring.web.SpringMvcPluginController;
import com.dianping.cat.home.spring.web.SpringMvcProjectController;
import com.dianping.cat.home.spring.web.SpringMvcRouterController;

public class SpringMvcMigrationConfigurationTest {
	@Test
	public void shouldFindSpringMvcMigrationControllerByDefaultComponentScan() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
		Set<String> beanClassNames = scanner.findCandidateComponents("com.dianping.cat.home.spring.web").stream()
				.map(BeanDefinition::getBeanClassName).collect(Collectors.toSet());

		Assert.assertTrue(beanClassNames.contains(SpringMvcBusinessController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcCacheController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcHealthController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcHomeController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcLoginController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcMatrixController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcModelController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcPluginController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcProjectController.class.getName()));
		Assert.assertTrue(beanClassNames.contains(SpringMvcRouterController.class.getName()));
	}

	@Test
	public void shouldExposeHealthPayloadForMigrationPath() {
		SpringMvcHealthController controller = new SpringMvcHealthController();

		Assert.assertEquals("UP", controller.health().get("status"));
		Assert.assertEquals("spring-mvc-migration", controller.health().get("runtime"));
	}
}
