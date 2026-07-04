package com.dianping.cat.configuration.reload.transform;

import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public interface ILinker {

   boolean onReportPeriod(ReportType parent, ReportPeriod reportPeriod);

   boolean onReportType(ReportReloadConfig parent, ReportType reportType);
}
