package com.dianping.cat.home.heartbeat.transform;

import com.dianping.cat.home.heartbeat.entity.Group;
import com.dianping.cat.home.heartbeat.entity.HeartbeatDisplayPolicy;
import com.dianping.cat.home.heartbeat.entity.Metric;

public interface ILinker {

   boolean onGroup(HeartbeatDisplayPolicy parent, Group group);

   boolean onMetric(Group parent, Metric metric);
}
