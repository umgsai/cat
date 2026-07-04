package com.dianping.cat.report;

import java.io.IOException;
import java.util.Date;

public interface ReportBucketFactory {

	ReportBucket createReportBucket(String name, Date timestamp, int index) throws IOException;
}
