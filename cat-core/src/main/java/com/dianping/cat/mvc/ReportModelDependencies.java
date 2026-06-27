package com.dianping.cat.mvc;

import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class ReportModelDependencies {
	@Resource
	private HostinfoService hostinfoService;

	@Resource
	private ProjectService projectService;

	@Resource
	private SampleConfigManager sampleConfigManager;

	public HostinfoService getHostinfoService() {
		return requireNonNull(hostinfoService, HostinfoService.class);
	}

	public ProjectService getProjectService() {
		return requireNonNull(projectService, ProjectService.class);
	}

	public SampleConfigManager getSampleConfigManager() {
		return requireNonNull(sampleConfigManager, SampleConfigManager.class);
	}

	private <T> T requireNonNull(T value, Class<?> type) {
		if (value == null) {
			throw new IllegalArgumentException(type.getName() + " is required for report model dependencies.");
		}
		return value;
	}
}
