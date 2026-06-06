package com.dianping.cat.boot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class EmbeddedCatServer {

	private static final String WAR_RESOURCE = "cat-home.war";

	private static final String CONTEXT_PATH = "/cat";

	public void start() throws IOException {
		int port = Integer.parseInt(System.getProperty("server.port", "8080"));
		Path baseDir = Files.createTempDirectory("cat-boot-");
		Path appBase = Files.createDirectories(baseDir.resolve("webapps"));
		Path warFile = copyWar(baseDir);

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(baseDir.toString());
		tomcat.setPort(port);
		tomcat.getConnector().setURIEncoding("UTF-8");
		tomcat.getHost().setAppBase(appBase.toString());

		Context context = tomcat.addWebapp(CONTEXT_PATH, warFile.toString());
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
}
