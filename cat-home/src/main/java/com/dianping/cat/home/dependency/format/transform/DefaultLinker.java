package com.dianping.cat.home.dependency.format.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.cat.home.dependency.format.entity.Domain;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;

public class DefaultLinker implements ILinker {
   @SuppressWarnings("unused")
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
   public boolean onDomain(final ProductLine parent, final Domain domain) {
      parent.addDomain(domain);
      return true;
   }

   @Override
   public boolean onProductLine(final TopoGraphFormatConfig parent, final ProductLine productLine) {
      parent.addProductLine(productLine);
      return true;
   }
}
