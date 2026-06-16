package com.dianping.cat.consumer.event.model;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.entity.StatusCode;

public interface IVisitor {

   public void visitEventReport(EventReport eventReport);

   public void visitGraphTrend(GraphTrend graphTrend);

   public void visitMachine(Machine machine);

   public void visitName(EventName name);

   public void visitRange(Range range);

   public void visitStatusCode(StatusCode statusCode);

   public void visitType(EventType type);
}
