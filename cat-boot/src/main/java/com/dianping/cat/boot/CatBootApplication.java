package com.dianping.cat.boot;

import org.springframework.boot.SpringApplication;

public class CatBootApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(CatBootApplication.class, args);
		new EmbeddedCatServer().start();
	}
}
