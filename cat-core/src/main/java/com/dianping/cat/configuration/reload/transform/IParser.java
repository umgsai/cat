package com.dianping.cat.configuration.reload.transform;

import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public interface IParser<T> {
   public ReportReloadConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForReportPeriod(IMaker<T> maker, ILinker linker, ReportPeriod parent, T node);

   public void parseForReportType(IMaker<T> maker, ILinker linker, ReportType parent, T node);
}
