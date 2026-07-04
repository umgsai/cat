package com.dianping.cat.home.service;

import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;

public interface IVisitor {

   void visitDomain(Domain domain);

   void visitServiceReport(ServiceReport serviceReport);
}
