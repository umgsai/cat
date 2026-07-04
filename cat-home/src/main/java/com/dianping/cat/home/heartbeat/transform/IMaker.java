package com.dianping.cat.home.heartbeat.transform;

import com.dianping.cat.home.heartbeat.entity.Group;
import com.dianping.cat.home.heartbeat.entity.HeartbeatDisplayPolicy;
import com.dianping.cat.home.heartbeat.entity.Metric;

public interface IMaker<T> {

   Group buildGroup(T node);

   HeartbeatDisplayPolicy buildHeartbeatDisplayPolicy(T node);

   Metric buildMetric(T node);
}
