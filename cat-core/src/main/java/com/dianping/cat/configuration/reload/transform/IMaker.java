package com.dianping.cat.configuration.reload.transform;

import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public interface IMaker<T> {

   ReportPeriod buildReportPeriod(T node);

   ReportReloadConfig buildReportReloadConfig(T node);

   ReportType buildReportType(T node);
}
