package com.dianping.cat.home.spring.web;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringMvcHomeController {
	@Resource
	private TcpSocketReceiver tcpSocketReceiver;

	@Resource
	private MessageConsumer messageConsumer;

	@GetMapping({ "/mvc", "/mvc/" })
	public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(request.getContextPath() + "/mvc/r/top?op=view&domain=cat");
	}

	@GetMapping("/mvc/r/home")
	public void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = homeModel(request);

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/spring/report/home/home.jsp");

		dispatcher.forward(request, response);
	}

	Map<String, Object> homeModel(HttpServletRequest request) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String docName = docName(request);
		String action = parameter(request, "op", "view");

		if ("checkpoint".equals(action)) {
			checkpoint();
		} else if ("threadDump".equals(action)) {
			model.put("content", threadDump());
		}

		model.put("docName", docName);
		model.put("webapp", request.getContextPath());
		model.put("domain", parameter(request, "domain", "cat"));
		model.put("ipAddress", parameter(request, "ip", "All"));
		model.put("date", parameter(request, "date", ""));
		model.put("reportType", parameter(request, "reportType", "day"));
		model.put("actionName", action);
		model.put("runtime", "spring-mvc-migration");
		model.put("homeUrl", request.getContextPath() + "/mvc/r/home");
		model.put("loginUrl", request.getContextPath() + "/mvc/s/login");
		model.put("model", model);
		return model;
	}

	void checkpoint() {
		tcpSocketReceiver.destroy();
		messageConsumer.doCheckpoint();
	}

	private String docName(HttpServletRequest request) {
		String docName = request.getParameter("docName");

		if (docName == null || docName.length() == 0) {
			return "index";
		}
		return docName;
	}

	private String parameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	String threadDump() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threads = bean.dumpAllThreads(true, true);
		StringBuilder sb = new StringBuilder(32768);
		int index = 1;
		TreeMap<String, ThreadInfo> sortedThreads = new TreeMap<String, ThreadInfo>();

		for (ThreadInfo thread : threads) {
			sortedThreads.put(thread.getThreadName(), thread);
		}

		sb.append("Threads: ").append(threads.length);
		sb.append("<pre>");

		for (ThreadInfo thread : sortedThreads.values()) {
			sb.append(index++).append(": <a href=\"#").append(thread.getThreadId()).append("\">")
					.append(thread.getThreadName()).append("</a>\r\n");
		}

		sb.append("\r\n");
		sb.append("\r\n");

		index = 1;

		for (ThreadInfo thread : sortedThreads.values()) {
			sb.append("<a name=\"").append(thread.getThreadId()).append("\">").append(index++).append(": ")
					.append(thread).append("\r\n");
		}

		sb.append("</pre>");
		return sb.toString();
	}
}
