package com.dianping.cat.home.service.client;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public interface IVisitor {

   void visitClientReport(ClientReport clientReport);

   void visitDomain(Domain domain);

   void visitMethod(Method method);
}
