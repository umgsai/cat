package com.dianping.cat.home.spring.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpringMvcHealthController {
	@GetMapping("/health")
	@ResponseBody
	public Map<String, Object> health() {
		Map<String, Object> model = new LinkedHashMap<String, Object>();

		model.put("status", "UP");
		model.put("runtime", "spring-mvc-migration");
		return model;
	}
}
