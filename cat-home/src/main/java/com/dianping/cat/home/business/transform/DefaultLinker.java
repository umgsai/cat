package com.dianping.cat.home.business.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

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
   public boolean onBusinessItem(final Tag parent, final BusinessItem businessItem) {
      parent.addBusinessItem(businessItem);
      return true;
   }

   @Override
   public boolean onTag(final BusinessTagConfig parent, final Tag tag) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addTag(tag);
            }
         });
      } else {
         parent.addTag(tag);
      }

      return true;
   }
}
