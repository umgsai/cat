package com.dianping.cat.consumer.business.model.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

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
   public boolean onBusinessItem(final BusinessReport parent, final BusinessItem businessItem) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addBusinessItem(businessItem);
            }
         });
      } else {
         parent.addBusinessItem(businessItem);
      }

      return true;
   }

   @Override
   public boolean onSegment(final BusinessItem parent, final Segment segment) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addSegment(segment);
            }
         });
      } else {
         parent.addSegment(segment);
      }

      return true;
   }
}
