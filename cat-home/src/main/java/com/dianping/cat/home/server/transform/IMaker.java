package com.dianping.cat.home.server.transform;

import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public interface IMaker<T> {

   Group buildGroup(T node);

   Item buildItem(T node);

   Segment buildSegment(T node);

   ServerMetricConfig buildServerMetricConfig(T node);
}
