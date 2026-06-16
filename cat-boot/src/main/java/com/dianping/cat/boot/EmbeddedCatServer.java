package com.dianping.cat.boot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedCatServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedCatServer.class);

	private static final String WAR_RESOURCE = "cat-home.war";

	private static final String CONTEXT_PATH = "/cat";

	public void start() throws IOException {
		int port = Integer.parseInt(System.getProperty("server.port", "8080"));
		Path baseDir = resolveBaseDir(port);
		deleteDirectory(baseDir);
		Path appBase = Files.createDirectories(baseDir.resolve("webapps"));
		Path webapp = resolveWebapp(baseDir);

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(baseDir.toString());
		tomcat.setPort(port);
		tomcat.getConnector().setURIEncoding("UTF-8");
		tomcat.getHost().setAppBase(appBase.toString());

		LOGGER.info("Starting CAT web application, contextPath={}, webapp={}.", CONTEXT_PATH, webapp);
		Context context = tomcat.addWebapp(CONTEXT_PATH, webapp.toString());
		context.setParentClassLoader(getClass().getClassLoader());

		try {
			tomcat.start();
			tomcat.getServer().await();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to start embedded CAT web application.", e);
		}
	}

	private Path copyWar(Path baseDir) throws IOException {
		Path warFile = baseDir.resolve(WAR_RESOURCE);

		try (InputStream input = getClass().getClassLoader().getResourceAsStream(WAR_RESOURCE)) {
			if (input == null) {
				throw new IllegalStateException("Missing " + WAR_RESOURCE + " on classpath.");
			}
			Files.copy(input, warFile);
		}

		return warFile;
	}

	private Path defaultExplodedWebapp() {
		Path fromRepositoryRoot = Paths.get("cat-home", "target", "cat-home").toAbsolutePath().normalize();

		if (Files.exists(fromRepositoryRoot.resolve("WEB-INF").resolve("web.xml"))) {
			return fromRepositoryRoot;
		}
		return Paths.get("..", "cat-home", "target", "cat-home").toAbsolutePath().normalize();
	}

	private void deleteDirectory(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			return;
		}

		try (java.util.stream.Stream<Path> paths = Files.walk(directory)) {
			paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
					throw new IllegalStateException("Unable to clean CAT boot directory: " + directory, e);
				}
			});
		} catch (IllegalStateException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			throw e;
		}
	}

	private Path resolveBaseDir(int port) {
		String configuredBaseDir = System.getProperty("cat.boot.baseDir");

		if (configuredBaseDir != null && configuredBaseDir.trim().length() > 0) {
			return Paths.get(configuredBaseDir).toAbsolutePath().normalize();
		}
		return Paths.get("target", "cat-boot-" + port).toAbsolutePath().normalize();
	}

	private Path resolveWebapp(Path baseDir) throws IOException {
		String configuredWebapp = System.getProperty("cat.home.webapp");

		if (configuredWebapp != null && configuredWebapp.trim().length() > 0) {
			Path webapp = Paths.get(configuredWebapp).toAbsolutePath().normalize();

			if (!Files.exists(webapp)) {
				throw new IllegalStateException("Configured CAT home webapp does not exist: " + webapp);
			}
			return webapp;
		}

		Path explodedWebapp = defaultExplodedWebapp();

		if (Files.exists(explodedWebapp.resolve("WEB-INF").resolve("web.xml"))) {
			return explodedWebapp;
		}
		return copyWar(baseDir);
	}
}
