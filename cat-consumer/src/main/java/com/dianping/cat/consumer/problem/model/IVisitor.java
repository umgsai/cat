package com.dianping.cat.consumer.problem.model;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;

public interface IVisitor {

   void visitDuration(Duration duration);

   void visitEntity(Entity entity);

   void visitEntry(Entry entry);

   void visitGraphTrend(GraphTrend graphTrend);

   void visitMachine(Machine machine);

   void visitProblemReport(ProblemReport problemReport);

   void visitSegment(Segment segment);

   void visitThread(JavaThread thread);
}
