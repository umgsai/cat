package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcPluginController {
	private final Map<String, String> m_serverMapping = new LinkedHashMap<String, String>();

	public SpringMvcPluginController() {
		// Production
		m_serverMapping.put("10.1.6.37:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.8.64:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.102:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.108:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.126:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.128:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.145:8080", "cat.dianpingoa.com");

		// QATE
		m_serverMapping.put("192.168.7.70:8080", "cat.qa.dianpingoa.com");
	}

	@GetMapping("/s/plugin/chrome")
	public void chrome(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (isEnabled(request, "mapping")) {
			writeServerMapping(response);
		} else if (isEnabled(request, "source")) {
			writeChromeSource(response);
		} else {
			writeClasspathResource(response, "/chrome/cat.crx", "application/octet-stream", "cat.crx");
		}
	}

	@GetMapping("/s/plugin")
	public void plugin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ("doc".equals(request.getParameter("op"))) {
			downloadDoc(request, response);
			return;
		}

		Map<String, Object> model = pluginModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/system/plugin/plugin.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> pluginModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String contextPath = request.getContextPath();

		model.put("runtime", "spring-mvc-migration");
		model.put("legacyPluginUrl", contextPath + "/s/plugin");
		model.put("chromeExtensionUrl", contextPath + "/mvc/s/plugin/chrome");
		model.put("chromeSourceUrl", contextPath + "/mvc/s/plugin/chrome?source=true");
		model.put("chromeMappingUrl", contextPath + "/mvc/s/plugin/chrome?mapping=true");
		model.put("legacyChromeExtensionUrl", contextPath + "/s/plugin/chrome");
		model.put("homeUrl", contextPath + "/mvc/r/home");

		return model;
	}

	private void addResourceFiles(ZipOutputStream output, String baseDir, String... paths) throws IOException {
		for (String path : paths) {
			String resource = baseDir + "/" + path;

			output.putNextEntry(new ZipEntry(path));
			try (InputStream in = getClass().getResourceAsStream(resource)) {
				if (in == null) {
					throw new IOException("Missing classpath resource: " + resource);
				}
				IOUtils.copy(in, output);
			}
			output.closeEntry();
		}
	}

	private void downloadDoc(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String file = request.getParameter("file");

		if (!isValidDocFile(file)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		writeClasspathResource(response, "/doc/" + file, "application/octet-stream", file);
	}

	boolean isEnabled(HttpServletRequest request, String name) {
		String value = request.getParameter(name);

		return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
	}

	boolean isValidDocFile(String file) {
		if (file == null || file.length() == 0 || file.contains("/") || file.contains("\\") || file.contains("..")) {
			return false;
		}
		return file.endsWith(".pdf");
	}

	String serverMappingJson() {
		return JSON.toJSONString(m_serverMapping);
	}

	private void writeChromeSource(HttpServletResponse response) throws IOException {
		response.setContentType("application/x-zip-compressed");
		response.addHeader("Content-Disposition", "attachment;filename=cat.zip");

		try (ZipOutputStream output = new ZipOutputStream(response.getOutputStream())) {
			addResourceFiles(output, "/chrome/cat", "manifest.json", "cat.png", "cat.js");
		}
	}

	private void writeClasspathResource(HttpServletResponse response, String resource, String contentType, String fileName)
			throws IOException {
		try (InputStream in = getClass().getResourceAsStream(resource)) {
			if (in == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			response.setContentType(contentType);
			response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
			IOUtils.copy(in, response.getOutputStream());
		}
	}

	private void writeServerMapping(HttpServletResponse response) throws IOException {
		byte[] content = serverMappingJson().getBytes(StandardCharsets.UTF_8);

		response.setContentType("application/json; charset=utf-8");
		response.setContentLength(content.length);
		response.getOutputStream().write(content);
	}
}
