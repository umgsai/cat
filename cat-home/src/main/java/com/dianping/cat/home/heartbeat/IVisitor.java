package com.dianping.cat.home.heartbeat;

import com.dianping.cat.home.heartbeat.entity.Group;
import com.dianping.cat.home.heartbeat.entity.HeartbeatDisplayPolicy;
import com.dianping.cat.home.heartbeat.entity.Metric;

public interface IVisitor {

   void visitGroup(Group group);

   void visitHeartbeatDisplayPolicy(HeartbeatDisplayPolicy heartbeatDisplayPolicy);

   void visitMetric(Metric metric);
}
