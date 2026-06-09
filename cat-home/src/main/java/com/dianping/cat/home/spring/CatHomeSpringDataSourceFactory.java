package com.dianping.cat.home.spring;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

final class CatHomeSpringDataSourceFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeSpringDataSourceFactory.class);

	private static final String CAT_DATA_SOURCE_ID = "cat";

	private CatHomeSpringDataSourceFactory() {
	}

	static DriverManagerDataSource createCatDataSource() {
		Element dataSource = findCatDataSource(loadDataSourcesXml());
		Properties connectionProperties = new Properties();
		String rawConnectionProperties = childText(properties(dataSource), "connectionProperties");

		if (!rawConnectionProperties.isBlank()) {
			for (String item : rawConnectionProperties.split("&")) {
				int index = item.indexOf('=');

				if (index > 0) {
					connectionProperties.setProperty(item.substring(0, index), item.substring(index + 1));
				}
			}
		}

		DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();

		driverManagerDataSource.setDriverClassName(childText(properties(dataSource), "driver"));
		driverManagerDataSource.setUrl(childText(properties(dataSource), "url"));
		driverManagerDataSource.setUsername(childText(properties(dataSource), "user"));
		driverManagerDataSource.setPassword(childText(properties(dataSource), "password"));
		driverManagerDataSource.setConnectionProperties(connectionProperties);
		return driverManagerDataSource;
	}

	private static Element findCatDataSource(Document document) {
		NodeList dataSources = document.getElementsByTagName("data-source");

		for (int i = 0; i < dataSources.getLength(); i++) {
			Element dataSource = (Element) dataSources.item(i);

			if (CAT_DATA_SOURCE_ID.equals(dataSource.getAttribute("id"))) {
				return dataSource;
			}
		}

		IllegalStateException error = new IllegalStateException(
				"Missing data-source id=" + CAT_DATA_SOURCE_ID + " in " + dataSourcesPath());

		LOGGER.error("Unable to find CAT datasource definition in Spring datasource config.", error);
		throw error;
	}

	private static Document loadDataSourcesXml() {
		Path path = dataSourcesPath();

		try {
			String xml = Files.readString(path);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		} catch (IOException e) {
			LOGGER.error("Unable to read Spring datasource config: {}.", path, e);
			throw new IllegalStateException("Unable to read Spring datasource config: " + path, e);
		} catch (Exception e) {
			LOGGER.error("Unable to parse Spring datasource config: {}.", path, e);
			throw new IllegalStateException("Unable to parse Spring datasource config: " + path, e);
		}
	}

	private static Path dataSourcesPath() {
		String catHome = System.getProperty("cat.home", Paths.get(System.getProperty("user.home"), ".cat").toString());

		return Paths.get(catHome, "datasources.xml");
	}

	private static Element properties(Element dataSource) {
		NodeList nodes = dataSource.getElementsByTagName("properties");

		if (nodes.getLength() == 0) {
			IllegalStateException error = new IllegalStateException(
					"Missing properties in data-source id=" + CAT_DATA_SOURCE_ID);

			LOGGER.error("CAT datasource definition is missing properties.", error);
			throw error;
		}

		return (Element) nodes.item(0);
	}

	private static String childText(Element element, String tagName) {
		NodeList nodes = element.getElementsByTagName(tagName);

		if (nodes.getLength() == 0) {
			return "";
		}

		String text = nodes.item(0).getTextContent();

		return text == null ? "" : text.trim();
	}
}
