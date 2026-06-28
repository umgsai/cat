package com.dianping.cat.home.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

public class CatHomeSpringConfigurationTest {
	@Test
	public void shouldRefreshBeanDefinitionsWithFullComponentScan() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.getDefaultListableBeanFactory().setAllowBeanDefinitionOverriding(false);
			context.addBeanFactoryPostProcessor(beanFactory -> {
				for (String beanName : beanFactory.getBeanDefinitionNames()) {
					BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

					beanDefinition.setLazyInit(true);
				}
			});
			context.register(CatHomeSpringConfiguration.class);
			context.refresh();

			Assert.assertTrue(context.containsBeanDefinition("catHomeSpringConfiguration"));
			Assert.assertTrue(context.containsBeanDefinition("catHomeDatabaseConfiguration"));
			Assert.assertTrue(context.containsBeanDefinition("catHomeRuntimeBootstrap"));
			Assert.assertTrue(context.containsBeanDefinition("springMvcHomeController"));
			Assert.assertTrue(context.containsBeanDefinition("transactionHandler"));
			Assert.assertFalse(context.containsBeanDefinition("springMvcMigrationConfiguration"));
			Assert.assertFalse(context.containsBeanDefinition("springStorageComponentConfiguration"));
		}
	}

	@Test
	public void shouldUseOnlyRootComponentScanPackage() {
		ComponentScan componentScan = CatHomeSpringConfiguration.class.getAnnotation(ComponentScan.class);

		Assert.assertNotNull(componentScan);
		Assert.assertArrayEquals(new String[] { "com.dianping.cat" }, componentScan.value());
		Assert.assertEquals(0, componentScan.basePackages().length);
		Assert.assertEquals(0, componentScan.basePackageClasses().length);
		Assert.assertEquals(0, componentScan.includeFilters().length);
		Assert.assertEquals(0, componentScan.excludeFilters().length);
		Assert.assertTrue(componentScan.useDefaultFilters());
	}
}
