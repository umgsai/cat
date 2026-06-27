package com.dianping.cat.home.spring.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.report.DefaultReportBucketManager;
import com.dianping.cat.report.ReportBucketFactory;

@Component("reportBucketManager")
public class SpringReportBucketManager extends DefaultReportBucketManager {
	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Resource(name = "reportBucketFactory")
	private ReportBucketFactory bucketFactory;

	@PostConstruct
	public void configure() {
		setConfigManager(serverConfigManager);
		setBucketFactory(bucketFactory);
		initialize();
	}
}
