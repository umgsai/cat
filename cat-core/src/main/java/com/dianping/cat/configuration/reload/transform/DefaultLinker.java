package com.dianping.cat.configuration.reload.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public class DefaultLinker implements ILinker {
   private boolean m_deferrable;

   private List<Runnable> m_deferedJobs = new ArrayList<Runnable>();

   public DefaultLinker(boolean deferrable) {
      m_deferrable = deferrable;
   }

   public void finish() {
      for (Runnable job : m_deferedJobs) {
         job.run();
      }
   }

   @Override
   public boolean onReportPeriod(final ReportType parent, final ReportPeriod reportPeriod) {
      parent.addReportPeriod(reportPeriod);
      return true;
   }

   @Override
   public boolean onReportType(final ReportReloadConfig parent, final ReportType reportType) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addReportType(reportType);
            }
         });
      } else {
         parent.addReportType(reportType);
      }

      return true;
   }
}
