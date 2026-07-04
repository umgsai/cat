package com.dianping.cat.consumer.problem.model.transform;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;

public interface ILinker {

   boolean onDuration(Entry parent, Duration duration);

   boolean onDuration(Entity parent, Duration duration);

   boolean onEntity(Machine parent, Entity entity);

   boolean onEntry(Machine parent, Entry entry);

   boolean onGraphTrend(Entity parent, GraphTrend graphTrend);

   boolean onMachine(ProblemReport parent, Machine machine);

   boolean onSegment(JavaThread parent, Segment segment);

   boolean onThread(Entry parent, JavaThread thread);

   boolean onThread(Entity parent, JavaThread thread);
}
