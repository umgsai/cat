package com.dianping.cat.configuration.reload.transform;

import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public interface ILinker {

   public boolean onReportPeriod(ReportType parent, ReportPeriod reportPeriod);

   public boolean onReportType(ReportReloadConfig parent, ReportType reportType);
}
