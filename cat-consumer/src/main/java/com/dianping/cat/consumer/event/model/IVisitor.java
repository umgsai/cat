package com.dianping.cat.consumer.event.model;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.entity.StatusCode;

public interface IVisitor {

   void visitEventReport(EventReport eventReport);

   void visitGraphTrend(GraphTrend graphTrend);

   void visitMachine(Machine machine);

   void visitName(EventName name);

   void visitRange(Range range);

   void visitStatusCode(StatusCode statusCode);

   void visitType(EventType type);
}
