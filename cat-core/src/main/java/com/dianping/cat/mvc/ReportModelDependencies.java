package com.dianping.cat.mvc;

import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;

public class ReportModelDependencies {
	private final HostinfoService m_hostinfoService;

	private final ProjectService m_projectService;

	private final SampleConfigManager m_sampleConfigManager;

	public ReportModelDependencies(ProjectService projectService, HostinfoService hostinfoService,
	      SampleConfigManager sampleConfigManager) {
		m_projectService = requireNonNull(projectService, ProjectService.class);
		m_hostinfoService = requireNonNull(hostinfoService, HostinfoService.class);
		m_sampleConfigManager = requireNonNull(sampleConfigManager, SampleConfigManager.class);
	}

	public HostinfoService getHostinfoService() {
		return m_hostinfoService;
	}

	public ProjectService getProjectService() {
		return m_projectService;
	}

	public SampleConfigManager getSampleConfigManager() {
		return m_sampleConfigManager;
	}

	private <T> T requireNonNull(T value, Class<?> type) {
		if (value == null) {
			throw new IllegalArgumentException(type.getName() + " is required for report model dependencies.");
		}
		return value;
	}
}
