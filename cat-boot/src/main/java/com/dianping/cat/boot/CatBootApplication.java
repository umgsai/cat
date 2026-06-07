package com.dianping.cat.boot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@ComponentScan(basePackages = "com.dianping.cat.boot")
public class CatBootApplication {

	public static void main(String[] args) throws Exception {
		applyDefaultCatProperties();
		routeJavaUtilLoggingToSlf4j();
		SpringApplication.run(CatBootApplication.class, args);
		new EmbeddedCatServer().start();
	}

	private static void applyDefaultCatProperties() throws Exception {
		String catHome = System.getProperty("cat.home");
		String legacyCatHome = System.getProperty("CAT_HOME");

		if (catHome == null || catHome.isBlank()) {
			catHome = legacyCatHome == null || legacyCatHome.isBlank()
					? Paths.get(System.getProperty("user.home"), ".cat").toString()
					: legacyCatHome;
			System.setProperty("cat.home", catHome);
		}

		if (legacyCatHome == null || legacyCatHome.isBlank()) {
			System.setProperty("CAT_HOME", catHome);
		}

		if (System.getProperty("cat.log.path") == null || System.getProperty("cat.log.path").isBlank()) {
			System.setProperty("cat.log.path", Paths.get(catHome, "logs").toString());
		}

		Files.createDirectories(Path.of(System.getProperty("cat.log.path")));
	}

	private static void routeJavaUtilLoggingToSlf4j() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}
}
