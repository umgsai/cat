package com.dianping.cat.home.service.client.transform;

import com.dianping.cat.home.service.client.IVisitor;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public abstract class BaseVisitor implements IVisitor {
   @Override
   public void visitClientReport(ClientReport clientReport) {
      for (Domain domain : clientReport.getDomains().values()) {
         visitDomain(domain);
      }
   }

   @Override
   public void visitDomain(Domain domain) {
      for (Method method : domain.getMethods().values()) {
         visitMethod(method);
      }
   }

   @Override
   public void visitMethod(Method method) {
   }
}
