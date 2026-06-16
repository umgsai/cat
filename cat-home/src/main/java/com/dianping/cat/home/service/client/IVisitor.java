package com.dianping.cat.home.service.client;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public interface IVisitor {

   public void visitClientReport(ClientReport clientReport);

   public void visitDomain(Domain domain);

   public void visitMethod(Method method);
}
