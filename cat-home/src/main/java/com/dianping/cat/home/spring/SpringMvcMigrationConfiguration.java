package com.dianping.cat.home.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dianping.cat.home.spring.web.SpringMvcHealthController;
import com.dianping.cat.home.spring.web.SpringMvcHomeController;
import com.dianping.cat.home.spring.web.SpringMvcLoginController;
import com.dianping.cat.system.page.login.service.SigninService;

@Configuration
public class SpringMvcMigrationConfiguration {
	@Bean
	public SpringMvcHealthController springMvcHealthController() {
		return new SpringMvcHealthController();
	}

	@Bean
	public SpringMvcHomeController springMvcHomeController() {
		return new SpringMvcHomeController();
	}

	@Bean
	public SpringMvcLoginController springMvcLoginController(SigninService signinService) {
		return new SpringMvcLoginController(signinService);
	}
}
