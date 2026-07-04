package com.dianping.cat.consumer.problem.model.transform;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;

public interface IMaker<T> {

   String buildDomain(T node);

   Duration buildDuration(T node);

   Entity buildEntity(T node);

   Entry buildEntry(T node);

   GraphTrend buildGraphTrend(T node);

   String buildIp(T node);

   Machine buildMachine(T node);

   String buildMessage(T node);

   ProblemReport buildProblemReport(T node);

   Segment buildSegment(T node);

   JavaThread buildThread(T node);
}
