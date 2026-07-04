package com.dianping.cat.consumer.problem.model.transform;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;

public interface IParser<T> {
   ProblemReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDuration(IMaker<T> maker, ILinker linker, Duration parent, T node);

   void parseForEntity(IMaker<T> maker, ILinker linker, Entity parent, T node);

   void parseForEntry(IMaker<T> maker, ILinker linker, Entry parent, T node);

   void parseForGraphTrend(IMaker<T> maker, ILinker linker, GraphTrend parent, T node);

   void parseForMachine(IMaker<T> maker, ILinker linker, Machine parent, T node);

   void parseForSegment(IMaker<T> maker, ILinker linker, Segment parent, T node);

   void parseForJavaThread(IMaker<T> maker, ILinker linker, JavaThread parent, T node);
}
