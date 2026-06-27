package com.dianping.cat.home.spring.storage;

import java.io.IOException;
import java.util.Date;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.report.LocalReportBucket;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketFactory;

@Component("reportBucketFactory")
public class SpringReportBucketFactory implements ReportBucketFactory {
	@Resource(name = "pathBuilder")
	private PathBuilder pathBuilder;

	@Resource(name = "serverConfigManager")
	private ServerConfigManager serverConfigManager;

	@Override
	public ReportBucket createReportBucket(String name, Date timestamp, int index) throws IOException {
		LocalReportBucket bucket = new LocalReportBucket();

		bucket.setPathBuilder(pathBuilder);
		bucket.setConfigManager(serverConfigManager);
		bucket.initialize(name, timestamp, index);
		return bucket;
	}
}
