package com.dianping.cat.configuration.reload;

import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public interface IVisitor {

   void visitReportPeriod(ReportPeriod reportPeriod);

   void visitReportReloadConfig(ReportReloadConfig reportReloadConfig);

   void visitReportType(ReportType reportType);
}
