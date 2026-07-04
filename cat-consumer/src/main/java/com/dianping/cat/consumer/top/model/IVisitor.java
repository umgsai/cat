package com.dianping.cat.consumer.top.model;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Machine;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;

public interface IVisitor {

   void visitDomain(Domain domain);

   void visitError(Error error);

   void visitMachine(Machine machine);

   void visitSegment(Segment segment);

   void visitTopReport(TopReport topReport);
}
