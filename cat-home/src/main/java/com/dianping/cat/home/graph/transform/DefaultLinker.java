package com.dianping.cat.home.graph.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

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
   public boolean onItem(final Graph parent, final Item item) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addItem(item);
            }
         });
      } else {
         parent.addItem(item);
      }

      return true;
   }

   @Override
   public boolean onSegment(final Item parent, final Segment segment) {
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
