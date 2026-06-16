package com.dianping.cat.configuration.reload.transform;

import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public interface IMaker<T> {

   public ReportPeriod buildReportPeriod(T node);

   public ReportReloadConfig buildReportReloadConfig(T node);

   public ReportType buildReportType(T node);
}
