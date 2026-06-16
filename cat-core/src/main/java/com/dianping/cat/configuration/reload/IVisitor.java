package com.dianping.cat.configuration.reload;

import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public interface IVisitor {

   public void visitReportPeriod(ReportPeriod reportPeriod);

   public void visitReportReloadConfig(ReportReloadConfig reportReloadConfig);

   public void visitReportType(ReportType reportType);
}
