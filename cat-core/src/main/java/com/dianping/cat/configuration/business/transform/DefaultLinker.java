package com.dianping.cat.configuration.business.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

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
   public boolean onBusinessItemConfig(final BusinessReportConfig parent, final BusinessItemConfig businessItemConfig) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addBusinessItemConfig(businessItemConfig);
            }
         });
      } else {
         parent.addBusinessItemConfig(businessItemConfig);
      }

      return true;
   }

   @Override
   public boolean onCustomConfig(final BusinessReportConfig parent, final CustomConfig customConfig) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addCustomConfig(customConfig);
            }
         });
      } else {
         parent.addCustomConfig(customConfig);
      }

      return true;
   }
}
